package com.yuoi.yuaiagent.controller;

import java.util.List;

import com.yuoi.yuaiagent.app.LoveApp;
import com.yuoi.yuaiagent.app.LoveReport;
import com.yuoi.yuaiagent.controller.dto.LoveChatRequest;
import com.yuoi.yuaiagent.controller.dto.LoveChatResponse;
import com.yuoi.yuaiagent.controller.dto.LoveFilterChatRequest;
import com.yuoi.yuaiagent.controller.dto.LoveReportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * “恋爱大师”接口：多轮恋爱咨询、恋爱报告、本地/云知识库恋爱知识问答（RAG）。
 */
@Tag(name = "恋爱大师接口")
@RestController
@RequestMapping("/ai/love-master")
public class LoveMasterController {

    private static final String DEFAULT_CONVERSATION_ID = "default";

    private final LoveApp loveApp;

    public LoveMasterController(LoveApp loveApp) {
        this.loveApp = loveApp;
    }

    @Operation(summary = "多轮恋爱咨询（带对话记忆）")
    @PostMapping("/chat")
    public LoveChatResponse chat(@RequestBody LoveChatRequest request) {
        requireText(request.message(), "消息内容不能为空");
        String reply = loveApp.doChat(request.message(), resolveConversationId(request.conversationId()));
        return new LoveChatResponse(reply);
    }

    @Operation(summary = "生成结构化恋爱报告（映射为 LoveReport 对象）")
    @PostMapping("/report")
    public LoveReport report(@RequestBody LoveReportRequest request) {
        requireText(request.message(), "消息内容不能为空");
        return loveApp.doChatWithReport(
                request.message(), request.username(), resolveConversationId(request.conversationId()));
    }

    @Operation(summary = "恋爱知识问答 - 本地知识库 RAG（基于恋爱知识文档向量库检索）")
    @PostMapping("/chat/rag")
    public LoveChatResponse chatWithLocalRag(@RequestBody LoveChatRequest request) {
        requireText(request.message(), "消息内容不能为空");
        String reply = loveApp.doChatWithLocalRag(request.message(), resolveConversationId(request.conversationId()));
        return new LoveChatResponse(reply);
    }

    @Operation(summary = "恋爱知识问答 - 云知识库 RAG（对接阿里云百炼云知识库检索）")
    @PostMapping("/chat/rag-cloud")
    public LoveChatResponse chatWithCloudRag(@RequestBody LoveChatRequest request) {
        requireText(request.message(), "消息内容不能为空");
        String reply = loveApp.doChatWithCloudRag(request.message(), resolveConversationId(request.conversationId()));
        return new LoveChatResponse(reply);
    }

    @Operation(summary = "按恋爱状态过滤的知识问答（基于元信息过滤，status=单身/恋爱/已婚）")
    @PostMapping("/chat/rag/filter")
    public LoveChatResponse chatWithStatusRag(@RequestBody LoveFilterChatRequest request) {
        requireText(request.message(), "消息内容不能为空");
        requireStatus(request.status());
        String reply = loveApp.doChatWithStatusRag(request.message(),
                resolveConversationId(request.conversationId()), request.status());
        return new LoveChatResponse(reply);
    }

    @Operation(summary = "MCP 工具问答（把 MCP 服务提供的工具交给 AI，如搜索图片）")
    @PostMapping("/chat/mcp")
    public LoveChatResponse chatWithMcp(@RequestBody LoveChatRequest request) {
        requireText(request.message(), "消息内容不能为空");
        String reply = loveApp.doChatWithMcp(request.message(), resolveConversationId(request.conversationId()));
        return new LoveChatResponse(reply);
    }

    @Operation(summary = "恋爱大师智能体 - 同步（自主规划 + 工具调用）")
    @PostMapping("/agent/chat")
    public LoveChatResponse agentChat(@RequestBody LoveChatRequest request) {
        requireText(request.message(), "消息内容不能为空");
        String reply = loveApp.doAgentChat(request.message(), resolveConversationId(request.conversationId()));
        return new LoveChatResponse(reply);
    }

    @Operation(summary = "恋爱大师智能体 - SSE 分步输出")
    @GetMapping("/agent/chat/sse")
    public SseEmitter agentChatSse(
            @RequestParam String message, @RequestParam(required = false) String conversationId) {
        return loveApp.doAgentChatStream(message, resolveConversationId(conversationId));
    }

    private static String resolveConversationId(String conversationId) {
        return (conversationId == null || conversationId.isBlank())
                ? DEFAULT_CONVERSATION_ID
                : conversationId.trim();
    }

    private static void requireText(String text, String errorMessage) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static void requireStatus(String status) {
        if (status == null || !List.of("单身", "恋爱", "已婚").contains(status)) {
            throw new IllegalArgumentException("status 仅支持：单身 / 恋爱 / 已婚");
        }
    }
}
