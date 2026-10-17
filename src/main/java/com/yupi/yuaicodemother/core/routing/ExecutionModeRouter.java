package com.yupi.yuaicodemother.core.routing;

import cn.hutool.core.util.StrUtil;
import com.yupi.yuaicodemother.ai.AiExecutionModeRoutingServiceFactory;
import com.yupi.yuaicodemother.constant.AppConstant;
import com.yupi.yuaicodemother.model.enums.CodeGenTypeEnum;
import com.yupi.yuaicodemother.model.enums.ExecutionModeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.regex.Pattern;

/**
 * 单轮执行模式路由。
 *
 * <p>HTML 和 MULTI_FILE 固定走 DIRECT。只有 VUE_PROJECT 才使用
 * “确定性规则优先、轻量模型兜底”的方式按本轮任务选择执行模式。</p>
 */
@Component
@Slf4j
public class ExecutionModeRouter {

    private static final Pattern COMPLEX_TASK_PATTERN = Pattern.compile(
            "从零|完整(实现|开发|重构|升级)?|多页面|多模块|跨文件|全站|工程化|架构调整|"
                    + "新增.{0,8}(页面|模块|业务流程)|路由系统|状态管理|权限系统|接口联调|"
                    + "依赖(安装|升级|冲突)|构建失败|编译失败|批量修复|全面检查|素材收集|"
                    + "生成.{0,6}(图片|插画|Logo|架构图)|直到.{0,8}(通过|成功)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern SIMPLE_TASK_PATTERN = Pattern.compile(
            "修改.{0,12}(文案|标题|颜色|字号|间距|圆角|按钮|背景|图标|链接)|"
                    + "替换.{0,12}(文案|标题|图片|颜色|图标)|"
                    + "调整.{0,12}(颜色|字号|间距|圆角|宽度|高度|位置|样式)|"
                    + "删除.{0,12}(文案|按钮|图片|模块)|"
                    + "把.{0,12}(改成|换成)",
            Pattern.CASE_INSENSITIVE);

    private final AiExecutionModeRoutingServiceFactory routingServiceFactory;

    public ExecutionModeRouter(AiExecutionModeRoutingServiceFactory routingServiceFactory) {
        this.routingServiceFactory = routingServiceFactory;
    }

    public ExecutionModeEnum route(Long appId, CodeGenTypeEnum codeGenType, String userPrompt) {
        if (codeGenType != CodeGenTypeEnum.VUE_PROJECT) {
            return ExecutionModeEnum.DIRECT;
        }
        if (StrUtil.isBlank(userPrompt)) {
            return ExecutionModeEnum.DIRECT;
        }
        // Vue 首次生成需要完整的素材、质检和构建闭环，不能由局部修改链路处理。
        File packageJson = new File(AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator
                + CodeGenTypeEnum.VUE_PROJECT.getValue() + "_" + appId + File.separator + "package.json");
        if (!packageJson.isFile()) {
            return ExecutionModeEnum.WORKFLOW;
        }
        if (COMPLEX_TASK_PATTERN.matcher(userPrompt).find()) {
            return ExecutionModeEnum.WORKFLOW;
        }
        if (SIMPLE_TASK_PATTERN.matcher(userPrompt).find()) {
            return ExecutionModeEnum.DIRECT;
        }
        try {
            ExecutionModeEnum mode = routingServiceFactory.create().routeExecutionMode(userPrompt);
            return mode == null ? ExecutionModeEnum.DIRECT : mode;
        } catch (Exception e) {
            // 路由不可用时优先保证主链可用，避免因辅助模型故障阻断代码生成。
            log.warn("Vue 执行模式路由失败，降级为 DIRECT: {}", e.getMessage());
            return ExecutionModeEnum.DIRECT;
        }
    }
}
