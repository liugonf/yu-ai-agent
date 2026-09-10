package com.yuoi.yuaiagent.chatmemory;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

/**
 * 文件持久化对话记忆的读写往返测试（无需调用 AI）。
 */
class FileBasedChatMemoryTests {

    @TempDir
    Path tempDir;

    private final String conversationId = "unit-test-001";

    @Test
    void addAndGetRoundTrip() {
        FileBasedChatMemory memory = new FileBasedChatMemory(tempDir.toString());

        memory.add(conversationId, List.of(new UserMessage("你好，我是来学开发的")));
        // 模拟多轮：后续回复/提问继续追加
        memory.add(conversationId, List.of(new AssistantMessage("欢迎！你现在想做什么项目？")));
        memory.add(conversationId, new UserMessage("我想做个 AI 助手"));

        List<Message> messages = memory.get(conversationId);
        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).getText()).contains("你好");
        assertThat(messages.get(2).getText()).isEqualTo("我想做个 AI 助手");
    }

    @Test
    void survivesNewInstanceLikeRestart() {
        FileBasedChatMemory first = new FileBasedChatMemory(tempDir.toString());
        first.add(conversationId, List.of(new UserMessage("启动时说的话")));

        // 模拟“服务重启”：用同一目录新建一个记忆对象
        FileBasedChatMemory second = new FileBasedChatMemory(tempDir.toString());
        List<Message> messages = second.get(conversationId);

        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getText()).isEqualTo("启动时说的话");
    }

    @Test
    void clearRemovesConversation() {
        FileBasedChatMemory memory = new FileBasedChatMemory(tempDir.toString());
        memory.add(conversationId, List.of(new UserMessage("将被清空")));

        memory.clear(conversationId);

        assertThat(memory.get(conversationId)).isEmpty();
        assertThat(Files.exists(tempDir.resolve("unit-test-001.kryo"))).isFalse();
    }

    @Test
    void unknownConversationReturnsEmpty() {
        FileBasedChatMemory memory = new FileBasedChatMemory(tempDir.toString());
        assertThat(memory.get("不存在的会话")).isEmpty();
    }
}
