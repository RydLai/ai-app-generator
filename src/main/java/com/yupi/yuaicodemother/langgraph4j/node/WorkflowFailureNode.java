package com.yupi.yuaicodemother.langgraph4j.node;

import com.yupi.yuaicodemother.langgraph4j.model.QualityResult;
import com.yupi.yuaicodemother.langgraph4j.state.WorkflowContext;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 将达到重试上限的失败状态写回工作流上下文。
 */
public class WorkflowFailureNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            QualityResult qualityResult = context.getQualityResult();
            List<String> errors = qualityResult == null ? null : qualityResult.getErrors();
            String detail = errors == null || errors.isEmpty()
                    ? "代码质量检查未通过"
                    : String.join("；", errors);
            context.setCurrentStep("工作流失败");
            if (context.getErrorMessage() == null || context.getErrorMessage().isBlank()) {
                context.setErrorMessage("达到最大修复次数（" + context.getMaxRetries() + "）：" + detail);
            }
            return WorkflowContext.saveContext(context);
        });
    }
}
