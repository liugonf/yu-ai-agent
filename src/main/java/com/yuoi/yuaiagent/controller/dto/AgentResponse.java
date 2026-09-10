package com.yuoi.yuaiagent.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 智能体执行结果响应体。
 */
@Schema(description = "AI 超级智能体执行结果")
public record AgentResponse(
        @Schema(description = "智能体执行过程与最终结果") String result) {
}
