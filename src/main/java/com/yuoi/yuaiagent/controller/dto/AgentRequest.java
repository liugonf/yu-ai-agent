package com.yuoi.yuaiagent.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 智能体任务请求体。
 */
@Schema(description = "AI 超级智能体任务请求")
public record AgentRequest(
        @Schema(description = "任务描述", example = "帮我搜索几张可爱猫咪的图片")
        String message) {
}
