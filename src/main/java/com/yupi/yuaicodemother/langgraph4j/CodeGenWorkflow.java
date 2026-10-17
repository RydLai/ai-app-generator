package com.yupi.yuaicodemother.langgraph4j;

import cn.hutool.json.JSONUtil;
import com.yupi.yuaicodemother.ai.model.message.AiResponseMessage;
import com.yupi.yuaicodemother.exception.BusinessException;
import com.yupi.yuaicodemother.exception.ErrorCode;
import com.yupi.yuaicodemother.langgraph4j.model.QualityResult;
import com.yupi.yuaicodemother.langgraph4j.node.CodeGeneratorNode;
import com.yupi.yuaicodemother.langgraph4j.node.CodeQualityCheckNode;
import com.yupi.yuaicodemother.langgraph4j.node.ImageCollectorNode;
import com.yupi.yuaicodemother.langgraph4j.node.ProjectBuilderNode;
import com.yupi.yuaicodemother.langgraph4j.node.PromptEnhancerNode;
import com.yupi.yuaicodemother.langgraph4j.node.RetryPreparationNode;
import com.yupi.yuaicodemother.langgraph4j.node.WorkflowFailureNode;
import com.yupi.yuaicodemother.langgraph4j.state.WorkflowContext;
import com.yupi.yuaicodemother.model.enums.CodeGenTypeEnum;
import com.yupi.yuaicodemother.monitor.MonitorContext;
import com.yupi.yuaicodemother.monitor.MonitorContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.UUID;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;

/**
 * 复杂 Vue 任务的 LangGraph4j 工作流。
 *
 * <p>应用类型已经在创建应用时确定，本图只接收固定的 VUE_PROJECT，
 * 不再包含生成类型路由节点。</p>
 */
@Component
@Slf4j
public class CodeGenWorkflow {

    private static final int DEFAULT_MAX_RETRIES = 2;

    /**
     * 创建生产工作流：素材收集 -> Prompt 增强 -> 生成 -> 质检 -> 有界修复/构建。
     */
    public CompiledGraph<MessagesState<String>> createWorkflow() {
        try {
            CompiledGraph<MessagesState<String>> workflow = new MessagesStateGraph<String>()
                    .addNode("image_collector", ImageCollectorNode.create())
                    .addNode("prompt_enhancer", PromptEnhancerNode.create())
                    .addNode("code_generator", CodeGeneratorNode.create())
                    .addNode("code_quality_check", CodeQualityCheckNode.create())
                    .addNode("retry_preparation", RetryPreparationNode.create())
                    .addNode("workflow_failure", WorkflowFailureNode.create())
                    .addNode("project_builder", ProjectBuilderNode.create())
                    .addEdge(START, "image_collector")
                    .addEdge("image_collector", "prompt_enhancer")
                    .addEdge("prompt_enhancer", "code_generator")
                    .addEdge("code_generator", "code_quality_check")
                    .addConditionalEdges("code_quality_check",
                            edge_async(this::routeAfterQualityCheck),
                            Map.of(
                                    "build", "project_builder",
                                    "retry", "retry_preparation",
                                    "fail", "workflow_failure"
                            ))
                    .addEdge("retry_preparation", "code_generator")
                    .addEdge("workflow_failure", END)
                    .addEdge("project_builder", END)
                    .compile();
            // 显式业务重试之外再加图级硬上限，防止未来修改边时意外形成无界循环。
            workflow.setMaxIterations(30);
            return workflow;
        } catch (GraphStateException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流创建失败");
        }
    }

    /**
     * 同步执行入口，主要用于测试和调试。
     */
    public WorkflowContext executeWorkflow(Long appId, Long userId,
                                           CodeGenTypeEnum fixedCodeGenType, String originalPrompt) {
        validateWorkflowInput(appId, userId, fixedCodeGenType, originalPrompt);
        MonitorContextHolder.setContext(MonitorContext.builder()
                .userId(userId.toString())
                .appId(appId.toString())
                .build());
        try {
            CompiledGraph<MessagesState<String>> workflow = createWorkflow();
            WorkflowContext finalContext = null;
            for (NodeOutput<MessagesState<String>> step : workflow.stream(
                    Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY,
                            createInitialContext(appId, userId, fixedCodeGenType, originalPrompt)))) {
                WorkflowContext currentContext = WorkflowContext.getContext(step.state());
                if (currentContext != null) {
                    finalContext = currentContext;
                }
            }
            ensureWorkflowSucceeded(finalContext);
            return finalContext;
        } finally {
            MonitorContextHolder.clearContext();
        }
    }

    /**
     * 以与 Vue DIRECT 链路相同的 JSON 消息协议输出工作流进度，供统一流处理器和 SSE 入口复用。
     */
    public Flux<String> executeWorkflowWithFlux(Long appId, Long userId,
                                                CodeGenTypeEnum fixedCodeGenType, String originalPrompt) {
        validateWorkflowInput(appId, userId, fixedCodeGenType, originalPrompt);
        return Flux.create(sink -> Thread.startVirtualThread(() -> {
            MonitorContextHolder.setContext(MonitorContext.builder()
                    .userId(userId.toString())
                    .appId(appId.toString())
                    .build());
            try {
                CompiledGraph<MessagesState<String>> workflow = createWorkflow();
                WorkflowContext initialContext = createInitialContext(
                        appId, userId, fixedCodeGenType, originalPrompt);
                sink.next(toAiMessage("已进入复杂任务工作流。\n"));

                GraphRepresentation graph = workflow.getGraph(GraphRepresentation.Type.MERMAID);
                log.info("workflowRunId: {}, 工作流图:\n{}", initialContext.getWorkflowRunId(), graph.content());

                WorkflowContext finalContext = null;
                String lastEmittedStep = null;
                for (NodeOutput<MessagesState<String>> step : workflow.stream(
                        Map.of(WorkflowContext.WORKFLOW_CONTEXT_KEY, initialContext))) {
                    if (sink.isCancelled()) {
                        log.info("workflowRunId: {}, 客户端已取消", initialContext.getWorkflowRunId());
                        return;
                    }
                    WorkflowContext currentContext = WorkflowContext.getContext(step.state());
                    if (currentContext != null) {
                        finalContext = currentContext;
                        String currentStep = currentContext.getCurrentStep();
                        boolean isBoundaryNode = START.equals(step.node()) || END.equals(step.node());
                        if (!isBoundaryNode && !currentStep.equals(lastEmittedStep)) {
                            sink.next(toAiMessage("[" + currentStep + "] 已完成。\n"));
                            lastEmittedStep = currentStep;
                        }
                    }
                }
                ensureWorkflowSucceeded(finalContext);
                sink.next(toAiMessage("复杂任务工作流执行完成，Vue 构建产物已就绪。"));
                sink.complete();
            } catch (Exception e) {
                log.error("Vue 工作流执行失败，appId: {}", appId, e);
                sink.error(e);
            } finally {
                MonitorContextHolder.clearContext();
            }
        }));
    }

    private WorkflowContext createInitialContext(Long appId, Long userId,
                                                  CodeGenTypeEnum fixedCodeGenType, String originalPrompt) {
        validateWorkflowInput(appId, userId, fixedCodeGenType, originalPrompt);
        return WorkflowContext.builder()
                .workflowRunId(UUID.randomUUID().toString())
                .appId(appId)
                .userId(userId)
                .originalPrompt(originalPrompt)
                .generationType(fixedCodeGenType)
                .retryCount(0)
                .maxRetries(DEFAULT_MAX_RETRIES)
                .currentStep("初始化")
                .build();
    }

    private void validateWorkflowInput(Long appId, Long userId,
                                       CodeGenTypeEnum fixedCodeGenType, String originalPrompt) {
        if (appId == null || appId <= 0 || userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "工作流缺少有效的应用或用户上下文");
        }
        if (fixedCodeGenType != CodeGenTypeEnum.VUE_PROJECT) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "WORKFLOW 当前仅支持 VUE_PROJECT");
        }
        if (originalPrompt == null || originalPrompt.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "工作流提示词不能为空");
        }
    }

    private String routeAfterQualityCheck(MessagesState<String> state) {
        WorkflowContext context = WorkflowContext.getContext(state);
        QualityResult qualityResult = context.getQualityResult();
        if (context.getErrorMessage() != null && !context.getErrorMessage().isBlank()) {
            return "fail";
        }
        if (qualityResult != null && Boolean.TRUE.equals(qualityResult.getIsValid())) {
            return "build";
        }
        if (context.getRetryCount() < context.getMaxRetries()) {
            return "retry";
        }
        return "fail";
    }

    private void ensureWorkflowSucceeded(WorkflowContext finalContext) {
        if (finalContext == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流未返回最终状态");
        }
        if (finalContext.getErrorMessage() != null && !finalContext.getErrorMessage().isBlank()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, finalContext.getErrorMessage());
        }
        if (finalContext.getBuildResultDir() == null || finalContext.getBuildResultDir().isBlank()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "工作流未生成有效的 Vue 构建产物");
        }
    }

    private String toAiMessage(String content) {
        return JSONUtil.toJsonStr(new AiResponseMessage(content));
    }
}
