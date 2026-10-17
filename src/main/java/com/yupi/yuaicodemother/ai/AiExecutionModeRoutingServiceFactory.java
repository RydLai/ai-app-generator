package com.yupi.yuaicodemother.ai;

import com.yupi.yuaicodemother.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Component;

/**
 * 创建无共享会话状态的执行模式路由服务。
 */
@Component
public class AiExecutionModeRoutingServiceFactory {

    public AiExecutionModeRoutingService create() {
        ChatModel chatModel = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiExecutionModeRoutingService.class)
                .chatModel(chatModel)
                .build();
    }
}
