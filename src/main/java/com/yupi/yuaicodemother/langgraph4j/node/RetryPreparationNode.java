package com.yupi.yuaicodemother.langgraph4j.node;

import com.yupi.yuaicodemother.langgraph4j.state.WorkflowContext;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 在重新生成前推进显式重试计数，确保质量修复循环有界。
 */
@Slf4j
public class RetryPreparationNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            context.setRetryCount(context.getRetryCount() + 1);
            context.setCurrentStep("准备第 " + context.getRetryCount() + " 次质量修复");
            log.warn("workflowRunId: {}, 开始第 {} 次质量修复",
                    context.getWorkflowRunId(), context.getRetryCount());
            return WorkflowContext.saveContext(context);
        });
    }
}
