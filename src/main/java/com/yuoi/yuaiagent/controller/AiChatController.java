package com.yuoi.yuaiagent.controller;

import com.yuoi.yuaiagent.controller.dto.ChatRequest;
import com.yuoi.yuaiagent.controller.dto.ChatResponse;
import com.yuoi.yuaiagent.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 对话接口。
 */
@Tag(name = "AI 对话接口")
@RestController
@RequestMapping("/chat")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @Operation(summary = "发送消息并获取 AI 回复")
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = aiChatService.chat(request.message());
        return new ChatResponse(reply);
    }
}














