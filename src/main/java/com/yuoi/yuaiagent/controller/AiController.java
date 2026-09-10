package com.yuoi.yuaiagent.controller;

import com.yuoi.yuaiagent.app.LoveApp;
import com.yuoi.yuaiagent.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

/**
 * AI 服务化接口：恋爱大师（同步 / SSE 流式）。
 */
@Tag(name = "AI 服务化接口")
@RestController
@RequestMapping("/ai")
public class AiController {

    private final LoveApp loveApp;

    public AiController(LoveApp loveApp) {
        this.loveApp = loveApp;
    }

    @Operation(summary = "恋爱大师 - 同步接口（一次性返回）")
    @GetMapping("/love_app/chat/sync")
    public BaseResponse<String> doChatWithLoveAppSync(
            @RequestParam String message, @RequestParam(required = false) String chatId) {
        String reply = loveApp.doChat(message, chatId == null ? "default" : chatId);
        return BaseResponse.ok(reply);
    }

    @Operation(summary = "恋爱大师 - SSE 流式接口（Flux）")
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSse(
            @RequestParam String message, @RequestParam(required = false) String chatId) {
        return loveApp.doChatByStream(message, chatId == null ? "default" : chatId);
    }

    @Operation(summary = "恋爱大师 - SSE 流式接口（SseEmitter）")
    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(
            @RequestParam String message, @RequestParam(required = false) String chatId) {
        SseEmitter emitter = new SseEmitter(180000L);
        loveApp.doChatByStream(message, chatId == null ? "default" : chatId)
                .subscribe(
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete);
        return emitter;
    }
}
