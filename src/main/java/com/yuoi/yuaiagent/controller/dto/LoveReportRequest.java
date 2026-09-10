package com.yuoi.yuaiagent.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 恋爱报告生成请求体。
 */
@Schema(description = "恋爱报告生成请求")
public record LoveReportRequest(
        @Schema(description = "用户消息/诉求", example = "帮我分析下我单身阶段的状况并给建议")
        String message,
        @Schema(description = "用户名（用于个性化报告标题）", example = "小鱼")
        String username,
        @Schema(description = "对话 id（同一 id 的聊天记录会成为报告的上下文）", example = "love-1")
        String conversationId) {
}
