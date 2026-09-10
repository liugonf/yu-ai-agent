package com.yuoi.yuaiagent.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 对话请求体。
 */
@Schema(description = "AI 对话请求")
public record ChatRequest(
        @Schema(description = "用户消息", example = "你好") String message) {
}
