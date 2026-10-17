package com.yupi.yuaicodemother.model.enums;

/**
 * 单轮代码生成的执行模式。
 *
 * <p>该枚举与应用级 {@link CodeGenTypeEnum} 相互独立：codeGenType 在应用创建时固定，
 * executionMode 仅在 Vue 工程的每轮对话中动态选择。</p>
 */
public enum ExecutionModeEnum {

    /** 复用现有代码生成链路，包含 Vue Tool Calling。 */
    DIRECT,

    /** 使用 LangGraph4j 多节点工作流完成复杂 Vue 任务。 */
    WORKFLOW
}
