package com.yuoi.yuaiagent.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 恋爱大师对话响应体。
 */
@Schema(description = "恋爱大师对话响应")
public record LoveChatResponse(
        @Schema(description = "AI 回复内容") String reply) {
}
