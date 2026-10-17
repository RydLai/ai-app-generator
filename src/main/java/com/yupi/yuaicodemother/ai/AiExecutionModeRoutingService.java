package com.yupi.yuaicodemother.ai;

import com.yupi.yuaicodemother.model.enums.ExecutionModeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * Vue 工程单轮执行模式路由服务。
 */
public interface AiExecutionModeRoutingService {

    /**
     * 为规则无法确定的 Vue 请求选择 DIRECT 或 WORKFLOW。
     */
    @SystemMessage(fromResource = "prompt/execution-mode-routing-system-prompt.txt")
    ExecutionModeEnum routeExecutionMode(String userPrompt);
}
