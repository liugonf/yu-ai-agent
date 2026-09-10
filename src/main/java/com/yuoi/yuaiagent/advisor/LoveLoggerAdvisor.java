package com.yuoi.yuaiagent.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import reactor.core.publisher.Flux;

/**
 * 恋爱大师日志 Advisor（参考官方 {@code SimpleLoggerAdvisor} 精简实现）。
 *
 * <p>默认打印 info 级别日志，且只输出单次请求的用户提示词与 AI 回复文本，
 * 比官方 Debug 级别的完整 JSON 日志更精简、更适合业务观察。
 * 同时实现 {@link CallAdvisor} 与 {@link StreamAdvisor}，保证同步/流式两条链路都能生效。</p>
 */
public class LoveLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Logger log = LoggerFactory.getLogger(LoveLoggerAdvisor.class);

    /** 日志中单条文本的最大长度，避免超长内容刷屏。 */
    private static final int MAX_TEXT_LENGTH = 300;

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /** 取值较大，保证它“最后”处理请求：此时对话记忆已生效。 */
    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        before(request);
        ChatClientResponse response = chain.nextCall(request);
        after(response);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        before(request);
        // 流式响应需要先聚合为完整内容再记录日志（聚合是只读观察，不修改响应）
        return new ChatClientMessageAggregator().aggregateChatClientResponse(
                chain.nextStream(request), this::after);
    }

    private void before(ChatClientRequest request) {
        Message userMessage = request.prompt().getUserMessage();
        log.info("AI Request: {}", userMessage == null ? "<无用户消息>" : abbreviate(userMessage.getText()));
    }

    private void after(ChatClientResponse response) {
        if (response == null || response.chatResponse() == null
                || response.chatResponse().getResult() == null) {
            log.warn("AI Response: <空响应>");
            return;
        }
        log.info("AI Response: {}", abbreviate(response.chatResponse().getResult().getOutput().getText()));
    }

    private static String abbreviate(String text) {
        if (text == null) {
            return "<空>";
        }
        return text.length() <= MAX_TEXT_LENGTH ? text : text.substring(0, MAX_TEXT_LENGTH) + "...(截断)";
    }
}
