package com.yuoi.yuaiagent.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

/**
 * 可调用工具的智能体：在 ReAct 基础上实现“手动控制工具调用”。
 *
 * <p>think 负责和大模型交互、拿到需要调用的工具列表；act 负责真正执行工具并把
 * 结果回填到对话上下文；如此循环，直到调用终止工具或达到最大步数。</p>
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class ToolCallAgent extends ReActAgent {

    /** 可用的工具回调列表 */
    private final ToolCallback[] availableTools;

    /** 最近一次“思考”得到的大模型响应 */
    private ChatResponse toolCallChatResponse;

    /** 工具调用执行器 */
    private final ToolCallingManager toolCallingManager;

    /** 对话选项：关闭 Spring AI 托管工具执行，改为手动执行 */
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }

    @Override
    public boolean think() {
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            getMessageList().add(new UserMessage(getNextStepPrompt()));
        }
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();

            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String text = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCalls = assistantMessage.getToolCalls();

            log.info("[{}] 思考: {}", getName(), text == null ? "" : text);
            log.info("[{}] 选择了 {} 个工具", getName(), toolCalls.size());
            if (!toolCalls.isEmpty()) {
                log.info("[{}] 工具调用:\n{}", getName(), toolCalls.stream()
                        .map(c -> String.format("  工具=%s, 参数=%s", c.name(), c.arguments()))
                        .collect(Collectors.joining("\n")));
            }

            if (toolCalls.isEmpty()) {
                // 没有工具调用，说明这是最终回答
                getMessageList().add(assistantMessage);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("[{}] 思考过程出错: {}", getName(), e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        if (toolCallChatResponse == null || !toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }

        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult executionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // conversationHistory 已包含助手消息 + 工具返回消息，直接替换上下文，避免重复添加
        setMessageList(executionResult.conversationHistory());

        Message last = getMessageList().get(getMessageList().size() - 1);
        String results;
        boolean terminateCalled = false;
        if (last instanceof ToolResponseMessage toolResponseMessage) {
            results = toolResponseMessage.getResponses().stream()
                    .map(r -> "工具 " + r.name() + " 完成，结果: " + r.responseData())
                    .collect(Collectors.joining("\n"));
            terminateCalled = toolResponseMessage.getResponses().stream()
                    .anyMatch(r -> "doTerminate".equals(r.name()));
        } else {
            results = last.getText();
        }

        if (terminateCalled) {
            setState(com.yuoi.yuaiagent.agent.model.AgentState.FINISHED);
        }
        log.info("[{}] 行动结果:\n{}", getName(), results);
        return results;
    }
}
