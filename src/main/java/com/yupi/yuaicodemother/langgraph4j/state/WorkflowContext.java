package com.yupi.yuaicodemother.langgraph4j.state;

import com.yupi.yuaicodemother.langgraph4j.model.ImageCollectionPlan;
import com.yupi.yuaicodemother.langgraph4j.model.ImageResource;
import com.yupi.yuaicodemother.langgraph4j.model.QualityResult;
import com.yupi.yuaicodemother.model.enums.CodeGenTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 工作流上下文 - 存储所有状态信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowContext implements Serializable {

    /**
     * WorkflowContext 在 MessagesState 中的存储key
     */
    public static final String WORKFLOW_CONTEXT_KEY = "workflowContext";

    /**
     * 当前执行步骤
     */
    private String currentStep;

    /**
     * 本次工作流运行标识，仅用于日志关联；当前未实现持久化 Checkpointer。
     */
    private String workflowRunId;

    /**
     * 真实应用 ID，由主聊天链路传入。
     */
    private Long appId;

    /**
     * 当前登录用户 ID，由主聊天链路传入。
     */
    private Long userId;

    /**
     * 用户原始输入的提示词
     */
    private String originalPrompt;

    /**
     * 图片资源字符串
     */
    private String imageListStr;

    /**
     * 图片资源列表
     */
    private List<ImageResource> imageList;

    /**
     * 增强后的提示词
     */
    private String enhancedPrompt;

    /**
     * 代码生成类型
     */
    private CodeGenTypeEnum generationType;

    /**
     * 生成的代码目录
     */
    private String generatedCodeDir;

    /**
     * 构建成功的目录
     */
    private String buildResultDir;

    /**
     * 质量检查结果
     */
    private QualityResult qualityResult;

    /**
     * 已执行的质检修复次数。
     */
    @Builder.Default
    private int retryCount = 0;

    /**
     * 允许的最大质检修复次数。
     */
    @Builder.Default
    private int maxRetries = 2;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 图片收集计划
     */
    private ImageCollectionPlan imageCollectionPlan;

    /**
     * 并发图片收集的中间结果字段
     */
    private List<ImageResource> contentImages;
    private List<ImageResource> illustrations;
    private List<ImageResource> diagrams;
    private List<ImageResource> logos;

    @Serial
    private static final long serialVersionUID = 1L;

    // ========== 上下文操作方法 ==========

    /**
     * 从 MessagesState 中获取 WorkflowContext
     */
    public static WorkflowContext getContext(MessagesState<String> state) {
        return (WorkflowContext) state.data().get(WORKFLOW_CONTEXT_KEY);
    }

    /**
     * 将 WorkflowContext 保存到 MessagesState 中
     */
    public static Map<String, Object> saveContext(WorkflowContext context) {
        return Map.of(WORKFLOW_CONTEXT_KEY, context);
    }
}
