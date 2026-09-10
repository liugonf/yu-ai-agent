package com.yuoi.yuaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

/**
 * 基于文件持久化的对话记忆（自定义 ChatMemory 实战）。
 *
 * <p>把 Spring AI 的 {@link Message} 列表按“对话 id 一个文件”的方式落盘，
 * 服务重启后对话记忆不丢失。核心难点是 {@link Message} 是接口、实现类字段不统一
 * 且大多没有无参构造，因此这里参考官方 InMemory 实现 + Kryo 高性能序列化
 * （关闭注册、使用 StdInstantiatorStrategy 绕过无参构造限制）。</p>
 *
 * <p>Kryo 实例非线程安全，这里用 ThreadLocal 为每个线程提供独立实例。</p>
 */
public class FileBasedChatMemory implements ChatMemory {

    private static final Logger log = LoggerFactory.getLogger(FileBasedChatMemory.class);

    private static final String FILE_SUFFIX = ".kryo";

    private final File baseDir;

    private final ThreadLocal<Kryo> kryoHolder = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(false);
        // 允许实例化没有无参构造函数的类（Message 的各个子类）
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        return kryo;
    });

    public FileBasedChatMemory(String dir) {
        this.baseDir = new File(dir);
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            throw new IllegalStateException("无法创建对话记忆目录: " + dir);
        }
    }

    @Override
    public synchronized void add(String conversationId, List<Message> messages) {
        if (conversationId == null || conversationId.isBlank() || messages == null || messages.isEmpty()) {
            return;
        }
        List<Message> all = new ArrayList<>(readAll(conversationId));
        all.addAll(messages);
        writeAll(conversationId, all);
    }

    @Override
    public synchronized List<Message> get(String conversationId) {
        return new ArrayList<>(readAll(conversationId));
    }

    @Override
    public synchronized void clear(String conversationId) {
        File file = conversationFile(conversationId);
        if (file.exists() && !file.delete()) {
            log.warn("删除对话记忆文件失败: {}", file.getAbsolutePath());
        }
    }

    private List<Message> readAll(String conversationId) {
        File file = conversationFile(conversationId);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (Input input = new Input(new FileInputStream(file))) {
            List<?> list = kryoHolder.get().readObject(input, ArrayList.class);
            List<Message> messages = new ArrayList<>();
            for (Object item : list) {
                if (item instanceof Message message) {
                    messages.add(message);
                }
            }
            return messages;
        } catch (IOException | RuntimeException e) {
            log.warn("读取对话记忆失败({}), 按空记忆处理: {}", file.getAbsolutePath(), e.getMessage());
            return new ArrayList<>();
        }
    }

    private void writeAll(String conversationId, List<Message> messages) {
        File file = conversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryoHolder.get().writeObject(output, messages);
            output.flush(); // Kryo Output 有内部缓冲，必须显式 flush 到文件
        } catch (IOException e) {
            log.error("写入对话记忆失败: {}", file.getAbsolutePath(), e);
        }
    }

    private File conversationFile(String conversationId) {
        // 对话 id 可能包含路径敏感字符，做一次清洗后再拼文件名
        String safeId = conversationId.replaceAll("[^a-zA-Z0-9._-]", "_");
        return new File(baseDir, safeId + FILE_SUFFIX);
    }
}
