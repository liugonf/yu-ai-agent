package com.yuoi.yuaiagent.agent;

import com.yuoi.yuaiagent.agent.model.AgentState;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 智能体基类：定义基本信息和多步骤执行流程（Agent Loop）。
 *
 * <p>使用模板方法设计模式：父类负责 while 循环控制执行流程，
 * 单步逻辑 {@link #step()} 交给子类实现。</p>
 */
@Slf4j
@Data
public abstract class BaseAgent {

    /** 智能体名称 */
    private String name;

    /** 系统提示词 */
    private String systemPrompt;

    /** 每轮循环前追加的“下一步”提示词 */
    private String nextStepPrompt;

    /** 当前状态 */
    private AgentState state = AgentState.IDLE;

    /** 最大执行步数 */
    private int maxSteps = 10;

    /** 当前步数 */
    private int currentStep = 0;

    /** 调用大模型的客户端（由调用方注入，解耦具体模型） */
    private ChatClient chatClient;

    /** 维护的对话消息上下文 */
    private List<Message> messageList = new ArrayList<>();

    /**
     * 执行智能体主循环。
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new IllegalStateException("Cannot run agent from state: " + this.state);
        }
        if (userPrompt == null || userPrompt.isBlank()) {
            throw new IllegalArgumentException("Cannot run agent with empty user prompt");
        }

        state = AgentState.RUNNING;
        messageList.add(new UserMessage(userPrompt));

        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                currentStep = i + 1;
                log.info("[{}] 执行步骤 {}/{}", name, currentStep, maxSteps);
                String stepResult = step();
                results.add("Step " + currentStep + ": " + stepResult);
            }
            if (state != AgentState.FINISHED && currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("[{}] 执行出错", name, e);
            return "执行错误: " + e.getMessage();
        } finally {
            cleanup();
        }
    }

    /** 单步执行，由子类实现。 */
    public abstract String step();

    /**
     * 流式执行（SSE）：每完成一步就向前端推送一步结果，适合智能体这类耗时较长的任务。
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(300000L);

        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    emitter.send("错误：无法从状态运行代理: " + this.state);
                    emitter.complete();
                    return;
                }
                if (userPrompt == null || userPrompt.isBlank()) {
                    emitter.send("错误：不能使用空提示词运行代理");
                    emitter.complete();
                    return;
                }

                state = AgentState.RUNNING;
                messageList.add(new UserMessage(userPrompt));

                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    currentStep = i + 1;
                    log.info("[{}] 流式执行步骤 {}/{}", name, currentStep, maxSteps);
                    String stepResult = step();
                    emitter.send("Step " + currentStep + ": " + stepResult + "\n\n");
                }
                if (state != AgentState.FINISHED && currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    emitter.send("执行结束: 达到最大步骤 (" + maxSteps + ")");
                }
                emitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("[{}] 流式执行出错", name, e);
                try {
                    emitter.send("执行错误: " + e.getMessage());
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            } finally {
                cleanup();
            }
        });

        emitter.onTimeout(() -> {
            state = AgentState.ERROR;
            cleanup();
            log.warn("[{}] SSE 连接超时", name);
        });
        emitter.onCompletion(() -> {
            if (state == AgentState.RUNNING) {
                state = AgentState.FINISHED;
            }
            cleanup();
            log.info("[{}] SSE 连接完成", name);
        });

        return emitter;
    }

    /** 执行结束后的清理钩子。 */
    protected void cleanup() {
        // 默认无操作
    }
}
