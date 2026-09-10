package com.yuoi.yuaiagent.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 对话响应体。
 */
@Schema(description = "AI 对话响应")
public record ChatResponse(
        @Schema(description = "AI 回复内容") String reply) {
}
