package com.yuoi.yuaiagent.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 按恋爱状态过滤的知识问答请求体。
 */
@Schema(description = "按状态过滤的恋爱知识问答请求")
public record LoveFilterChatRequest(
        @Schema(description = "用户消息", example = "婚后关系不太亲密怎么办？")
        String message,
        @Schema(description = "恋爱状态过滤标签（单身/恋爱/已婚）", example = "已婚")
        String status,
        @Schema(description = "对话 id", example = "love-rag-married")
        String conversationId) {
}
