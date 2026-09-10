package com.yuoi.yuaiagent.agent;

import com.yuoi.yuaiagent.advisor.LoveLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

/**
 * 恋爱大师智能体：把超级智能体的自主规划 + 工具调用能力，融合进“恋爱大师”人格。
 *
 * <p>保留恋爱咨询的系统提示词，同时具备 ReAct 循环与工具调用能力，
 * 遇到需要外部工具（如搜索图片）的任务时会自主规划、分步执行，最后调用终止工具结束。</p>
 */
public class LoveManus extends ToolCallAgent {

    private static final String NEXT_STEP_PROMPT = """
            请结合用户当前的感情诉求，主动判断是否需要调用工具：
            - 需要搜索图片 / 找约会灵感 / 找礼物时，可调用 searchImage 工具；
            - 完成用户需求、或确认无需进一步操作时，调用 doTerminate 工具结束。
            每次调用工具后，基于结果继续给出贴合用户感情状况的建议。
            """;

    public LoveManus(ChatModel chatModel, ToolCallback[] tools, String lovePersona) {
        super(tools);
        setName("恋爱大师智能体");
        setSystemPrompt(lovePersona + "\n\n"
                + "你同时具备自主规划与工具调用能力，能一步步完成用户的情感相关任务。");
        setNextStepPrompt(NEXT_STEP_PROMPT);
        setMaxSteps(15);

        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new LoveLoggerAdvisor())
                .build();
        setChatClient(chatClient);
    }
}
