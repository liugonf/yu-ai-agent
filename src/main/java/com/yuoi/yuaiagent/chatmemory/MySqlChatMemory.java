package com.yuoi.yuaiagent.chatmemory;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 基于 MySQL 的对话记忆（JDBC 持久化）。
 *
 * <p>把每轮对话消息以“会话 id + 消息角色 + 文本”的形式存入 MySQL 的
 * {@code chat_message} 表，服务重启后对话记忆不丢失。适合已有 MySQL 关系库、
 * 又不想引入 Redis / 文件存储的场景。</p>
 */
public class MySqlChatMemory implements ChatMemory {

    private static final Logger log = LoggerFactory.getLogger(MySqlChatMemory.class);

    private static final String TABLE_NAME = "chat_message";

    private final JdbcTemplate jdbcTemplate;

    public MySqlChatMemory(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 建表（幂等，失败仅告警不阻断启动）。 */
    public void initSchema() {
        try {
            jdbcTemplate.execute("""
                    CREATE TABLE IF NOT EXISTS %s (
                        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        conversation_id VARCHAR(128) NOT NULL,
                        msg_type VARCHAR(16) NOT NULL,
                        content TEXT,
                        create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        KEY idx_conversation (conversation_id)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                    """.formatted(TABLE_NAME));
            log.info("已确保 MySQL 对话记忆表 {} 存在", TABLE_NAME);
        } catch (Exception e) {
            // 数据库只读 / 写锁 / 网络抖动时，不阻断应用启动，记忆降级为“不落库”
            log.warn("初始化 MySQL 对话记忆表失败（可能实例处于只读/写锁状态），记忆将暂不持久化: {}",
                    e.getMessage());
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (conversationId == null || conversationId.isBlank() || messages == null || messages.isEmpty()) {
            return;
        }
        try {
            for (Message message : messages) {
                jdbcTemplate.update(
                        "INSERT INTO " + TABLE_NAME + " (conversation_id, msg_type, content) VALUES (?, ?, ?)",
                        conversationId, message.getMessageType().name(), message.getText());
            }
        } catch (Exception e) {
            log.warn("写入 MySQL 对话记忆失败（忽略）: {}", e.getMessage());
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        try {
            return jdbcTemplate.query(
                    "SELECT msg_type, content FROM " + TABLE_NAME
                            + " WHERE conversation_id = ? ORDER BY id ASC",
                    (rs, rowNum) -> toMessage(rs.getString("msg_type"), rs.getString("content")),
                    conversationId);
        } catch (Exception e) {
            log.warn("读取 MySQL 对话记忆失败（返回空）: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void clear(String conversationId) {
        try {
            jdbcTemplate.update("DELETE FROM " + TABLE_NAME + " WHERE conversation_id = ?", conversationId);
        } catch (Exception e) {
            log.warn("清空 MySQL 对话记忆失败（忽略）: {}", e.getMessage());
        }
    }

    private Message toMessage(String msgType, String content) {
        MessageType type;
        try {
            type = MessageType.valueOf(msgType);
        } catch (IllegalArgumentException e) {
            type = MessageType.ASSISTANT;
        }
        String text = content == null ? "" : content;
        return switch (type) {
            case USER -> new UserMessage(text);
            case SYSTEM -> new SystemMessage(text);
            case ASSISTANT, TOOL -> new AssistantMessage(text);
        };
    }
}
