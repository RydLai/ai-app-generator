package com.yupi.yuaicodemother.core.routing;

import com.yupi.yuaicodemother.ai.AiExecutionModeRoutingService;
import com.yupi.yuaicodemother.ai.AiExecutionModeRoutingServiceFactory;
import com.yupi.yuaicodemother.constant.AppConstant;
import com.yupi.yuaicodemother.model.enums.CodeGenTypeEnum;
import com.yupi.yuaicodemother.model.enums.ExecutionModeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecutionModeRouterTest {

    @Test
    void nonVueTypeIsAlwaysDirect() {
        AiExecutionModeRoutingServiceFactory factory = mock(AiExecutionModeRoutingServiceFactory.class);
        ExecutionModeRouter router = new ExecutionModeRouter(factory);

        Assertions.assertEquals(ExecutionModeEnum.DIRECT,
                router.route(1L, CodeGenTypeEnum.HTML, "完整重构整个站点"));
        Assertions.assertEquals(ExecutionModeEnum.DIRECT,
                router.route(1L, CodeGenTypeEnum.MULTI_FILE, "新增多个页面和模块"));
    }

    @Test
    void firstVueGenerationUsesWorkflow() {
        AiExecutionModeRoutingServiceFactory factory = mock(AiExecutionModeRoutingServiceFactory.class);
        ExecutionModeRouter router = new ExecutionModeRouter(factory);
        long appId = ThreadLocalRandom.current().nextLong(1_000_000_000L, Long.MAX_VALUE);

        Assertions.assertEquals(ExecutionModeEnum.WORKFLOW,
                router.route(appId, CodeGenTypeEnum.VUE_PROJECT, "创建一个数据看板"));
    }

    @Test
    void existingVueProjectUsesRulesThenModelFallback() throws IOException {
        long appId = ThreadLocalRandom.current().nextLong(1_000_000_000L, Long.MAX_VALUE);
        Path projectDir = Path.of(AppConstant.CODE_OUTPUT_ROOT_DIR,
                CodeGenTypeEnum.VUE_PROJECT.getValue() + "_" + appId);
        Path packageJson = projectDir.resolve("package.json");
        Files.createDirectories(projectDir);
        Files.writeString(packageJson, "{}");
        try {
            AiExecutionModeRoutingServiceFactory factory = mock(AiExecutionModeRoutingServiceFactory.class);
            AiExecutionModeRoutingService aiRouter = mock(AiExecutionModeRoutingService.class);
            when(factory.create()).thenReturn(aiRouter);
            when(aiRouter.routeExecutionMode("优化用户体验"))
                    .thenReturn(ExecutionModeEnum.WORKFLOW);
            ExecutionModeRouter router = new ExecutionModeRouter(factory);

            Assertions.assertEquals(ExecutionModeEnum.DIRECT,
                    router.route(appId, CodeGenTypeEnum.VUE_PROJECT, "把按钮颜色改成蓝色"));
            Assertions.assertEquals(ExecutionModeEnum.WORKFLOW,
                    router.route(appId, CodeGenTypeEnum.VUE_PROJECT, "新增多个页面并重构状态管理"));
            Assertions.assertEquals(ExecutionModeEnum.WORKFLOW,
                    router.route(appId, CodeGenTypeEnum.VUE_PROJECT, "优化用户体验"));
        } finally {
            Files.deleteIfExists(packageJson);
            Files.deleteIfExists(projectDir);
        }
    }
}
