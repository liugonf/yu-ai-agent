package com.yuoi.yuaiagent.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;
import com.yuoi.yuaiagent.config.DashScopeProperties;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * AI 对话服务：封装 DashScope（阿里云百炼）文本生成调用。
 */
@Service
public class AiChatService {

    private final DashScopeProperties properties;

    public AiChatService(DashScopeProperties properties) {
        this.properties = properties;
    }

    /**
     * 使用百炼专属版（专属部署）时，需要把默认服务地址替换为专属地址。
     * 该方法在应用启动时执行一次即可。
     */
    @PostConstruct
    public void init() {
        if (StringUtils.hasText(properties.getBaseUrl())) {
            Constants.baseHttpApiUrl = properties.getBaseUrl();
        }
    }

    /**
     * 发送一条用户消息并返回 AI 回复。
     *
     * @param userMessage 用户消息
     * @return AI 回复内容
     */
    public String chat(String userMessage) {
        if (!StringUtils.hasText(userMessage)) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (!StringUtils.hasText(properties.getApiKey())) {
            throw new IllegalStateException("未配置 dashscope.api-key，请在 application.yml 中填写");
        }

        List<Message> messages = Arrays.asList(
                Message.builder()
                        .role(Role.SYSTEM.getValue())
                        .content(properties.getSystemPrompt())
                        .build(),
                Message.builder()
                        .role(Role.USER.getValue())
                        .content(userMessage)
                        .build());

        GenerationParam param = GenerationParam.builder()
                .apiKey(properties.getApiKey())
                .model(properties.getModel())
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();

        try {
            GenerationResult result = new Generation().call(param);
            return extractReply(result);
        } catch (NoApiKeyException e) {
            throw new IllegalStateException("API Key 无效或未提供: " + e.getMessage(), e);
        } catch (InputRequiredException e) {
            throw new IllegalStateException("请求参数有误: " + e.getMessage(), e);
        } catch (ApiException e) {
            throw new IllegalStateException("调用 AI 服务失败: " + e.getMessage(), e);
        }
    }

    private String extractReply(GenerationResult result) {
        GenerationOutput output = result.getOutput();
        if (output == null) {
            throw new IllegalStateException("AI 服务返回为空: " + result.getMessage());
        }

        List<GenerationOutput.Choice> choices = output.getChoices();
        if (choices != null && !choices.isEmpty()) {
            Message message = choices.get(0).getMessage();
            if (message != null && StringUtils.hasText(message.getContent())) {
                return message.getContent();
            }
        }

        if (StringUtils.hasText(output.getText())) {
            return output.getText();
        }

        throw new IllegalStateException("AI 服务无返回内容: " + result.getMessage());
    }
}
