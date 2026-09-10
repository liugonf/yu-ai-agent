package com.yuoi.yuaiagent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DashScope（阿里云百炼）相关配置。
 *
 * <p>对应 application.yml 中的 {@code dashscope} 配置段，部署时可灵活修改，
 * 无需改动代码即可切换模型、API Key 与服务地址。</p>
 */
@Data
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeProperties {

    /**
     * DashScope / 百炼 API Key。
     */
    private String apiKey;

    /**
     * 模型名称，例如 qwen-turbo、qwen-plus、qwen-max 等。
     */
    private String model = "qwen-turbo";

    /**
     * 服务地址。使用百炼专属版（专属部署）时改为对应的专属地址。
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/api/v1";

    /**
     * 系统提示词，用于设定助手角色。
     */

    private String systemPrompt = "You are a helpful assistant.";
}
