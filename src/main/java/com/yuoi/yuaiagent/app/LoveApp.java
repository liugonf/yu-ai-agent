package com.yuoi.yuaiagent.app;

import com.yuoi.yuaiagent.advisor.LoveLoggerAdvisor;
import com.yuoi.yuaiagent.agent.LoveManus;
import com.yuoi.yuaiagent.rag.LoveLocalRagConfig.LoveLocalVectorStoreService;
import com.yuoi.yuaiagent.rag.LoveRagFilterAdvisorFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

/**
 * “恋爱大师”多轮对话应用（教程原版应用，含知识问答 RAG 扩展）。
 *
 * <ul>
 *   <li>多轮对话：ChatClient + 对话记忆（文件持久化）</li>
 *   <li>自定义 Advisor：日志拦截（LoveLoggerAdvisor）</li>
 *   <li>结构化输出：恋爱报告 {@link LoveReport}</li>
 *   <li>本地知识库 RAG：QuestionAnswerAdvisor + SimpleVectorStore（文档在 rag 包中构建）</li>
 *   <li>云知识库 RAG：RetrievalAugmentationAdvisor + DashScope 云知识库</li>
 * </ul>
 */
@Component
public class LoveApp {

    private static final Logger log = LoggerFactory.getLogger(LoveApp.class);

    private static final String REPORT_SYSTEM_TAIL_TEMPLATE = """
            本轮对话结束后，请基于整段对话为用户生成一份“恋爱报告”。
            要求：
            - title 字段为个性化标题，形如“{username} 的恋爱报告”；
            - suggestions 字段给出 3~5 条具体、可落地、贴合用户感情状况的建议。
            """;

    private final ChatModel chatModel;
    private final Resource personaResource;
    private final String persona;

    private final ChatMemory chatMemory;
    private final MessageChatMemoryAdvisor memoryAdvisor;
    private final LoveLoggerAdvisor loggerAdvisor;

    private final ChatClient chatClient;
    private final LoveLocalVectorStoreService localRagService;
    private final Advisor cloudRagAdvisor;
    private final ObjectProvider<ToolCallbackProvider> mcpToolCallbackProvider;
    private final ToolCallback[] allTools;

    /** 本地 RAG 用的 ChatClient（懒创建：首次调用时才向量化建库） */
    private volatile ChatClient ragLocalChatClient;
    /** 云 RAG 用的 ChatClient（懒创建） */
    private volatile ChatClient ragCloudChatClient;

    public LoveApp(ChatModel dashscopeChatModel,
            @Value("classpath:prompts/love-master-system.st") Resource personaResource,
            LoveLocalVectorStoreService localRagService,
            @Qualifier("loveRagCloudAdvisor") Advisor cloudRagAdvisor,
            @Qualifier("loveChatMemory") ChatMemory chatMemory,
            @Qualifier("mcpToolCallbacks") ObjectProvider<ToolCallbackProvider> mcpToolCallbackProvider,
            @Qualifier("allTools") ToolCallback[] allTools) throws Exception {
        this.chatModel = dashscopeChatModel;
        this.personaResource = personaResource;
        this.persona = personaResource.getContentAsString(StandardCharsets.UTF_8);
        this.localRagService = localRagService;
        this.cloudRagAdvisor = cloudRagAdvisor;
        this.mcpToolCallbackProvider = mcpToolCallbackProvider;
        this.allTools = allTools;

        // 对话记忆：默认文件持久化，mysql.enabled=true 时切换为 MySQL 持久化
        this.chatMemory = chatMemory;
        this.memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        this.loggerAdvisor = new LoveLoggerAdvisor();

        // 常规对话链：记忆 + 日志
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(personaResource)
                .defaultAdvisors(memoryAdvisor, loggerAdvisor)
                .build();
    }

    /** 多轮对话（恋爱咨询）。 */
    public String doChat(String message, String chatId) {
        String content = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();
        log.info("love doChat done, chatId={}, reply length={}", chatId, content == null ? 0 : content.length());
        return content;
    }

    /** 多轮对话（SSE 流式）：实时逐段输出 AI 回复文本。 */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    /**
     * 恋爱大师智能体（同步）：恋爱大师人格 + 自主规划 + 工具调用，
     * 遇到需要外部工具（如搜索图片）的任务时自动分步执行。
     */
    public String doAgentChat(String message, String chatId) {
        return newLoveManus(chatId).run(message);
    }

    /** 恋爱大师智能体（SSE 分步输出）。 */
    public SseEmitter doAgentChatStream(String message, String chatId) {
        return newLoveManus(chatId).runStream(message);
    }

    private LoveManus newLoveManus(String chatId) {
        LoveManus agent = new LoveManus(chatModel, allTools, persona);
        // 复用恋爱大师的对话记忆作为智能体的历史上下文
        agent.setMessageList(new ArrayList<>(chatMemory.get(chatId)));
        return agent;
    }

    /** 结构化恋爱报告。 */
    public LoveReport doChatWithReport(String message, String username, String chatId) {
        String reportTail = new PromptTemplate(REPORT_SYSTEM_TAIL_TEMPLATE)
                .render(Map.of("username", StringUtils.hasText(username) ? username : "用户"));

        LoveReport report = chatClient.prompt()
                .system(persona + "\n\n" + reportTail)
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
        log.info("love report generated: {}", report);
        return report;
    }

    /**
     * 本地知识库 RAG 问答：先向量化本地的恋爱知识文档，
     * 再用 QuestionAnswerAdvisor 检索并把命中内容拼进提问。
     */
    public String doChatWithLocalRag(String message, String chatId) {
        ChatClient ragClient = ragLocalChatClient;
        if (ragClient == null) {
            synchronized (this) {
                ragClient = ragLocalChatClient;
                if (ragClient == null) {
                    ragClient = ChatClient.builder(chatModel)
                            .defaultSystem(personaResource)
                            .defaultAdvisors(memoryAdvisor, loggerAdvisor, localRagService.questionAnswerAdvisor())
                            .build();
                    ragLocalChatClient = ragClient;
                }
            }
        }
        return callWithRag(ragClient, message, chatId, "local");
    }

    /** 云知识库 RAG 问答：从百炼云知识库检索恋爱资料回答问题。 */
    public String doChatWithCloudRag(String message, String chatId) {
            ChatClient ragClient = ragCloudChatClient;
            if (ragClient == null) {
            synchronized (this) {
                ragClient = ragCloudChatClient;
                if (ragClient == null) {
                    ragClient = ChatClient.builder(chatModel)
                            .defaultSystem(personaResource)
                            .defaultAdvisors(memoryAdvisor, loggerAdvisor, cloudRagAdvisor)
                            .build();
                    ragCloudChatClient = ragClient;
                }
            }
        }
        return callWithRag(ragClient, message, chatId, "cloud");
    }

    /**
     * MCP 工具问答：把 mcp-servers.json 中 MCP 服务提供的工具交给大模型，
     * 让 AI 按需调用（本质是工具调用，比如根据关键词搜索图片）。
     */
    public String doChatWithMcp(String message, String chatId) {
        ToolCallbackProvider provider = mcpToolCallbackProvider.getIfAvailable();
        if (provider == null) {
            return "MCP 未启用（生产环境默认关闭），图片搜索等工具暂不可用";
        }
        String content = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(provider)
                .call()
                .content();
        log.info("love mcp done, chatId={}, reply length={}", chatId, content == null ? 0 : content.length());
        return content;
    }

    /**
     * 按恋爱状态过滤的 RAG 问答（作业 2）：只在该状态（单身/恋爱/已婚）的知识切片里检索。
     */
    public String doChatWithStatusRag(String message, String chatId, String status) {
        Advisor statusAdvisor = LoveRagFilterAdvisorFactory.create(localRagService.vectorStore(), status);
        ChatClient statusClient = ChatClient.builder(chatModel)
                .defaultSystem(personaResource)
                .defaultAdvisors(memoryAdvisor, loggerAdvisor, statusAdvisor)
                .build();
        return callWithRag(statusClient, message, chatId, "status-" + status);
    }

    private String callWithRag(ChatClient ragClient, String message, String chatId, String mode) {
        String content = ragClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();
        log.info("love rag({}) done, chatId={}, reply length={}", mode, chatId,
                content == null ? 0 : content.length());
        return content;
    }
}
