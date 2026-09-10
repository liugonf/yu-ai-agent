package com.yuoi.yuaiagent.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 恋爱大师对话请求体。
 */
@Schema(description = "恋爱大师对话请求")
public record LoveChatRequest(
        @Schema(description = "用户消息", example = "你好，我单身很久了，不知道怎么扩大社交圈")
        String message,
        @Schema(description = "对话 id（用于隔离多轮对话记忆，同一 id 共享上下文）", example = "love-1")
        String conversationId) {
}
