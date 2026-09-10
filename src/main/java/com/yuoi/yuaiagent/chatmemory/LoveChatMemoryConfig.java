package com.yuoi.yuaiagent.chatmemory;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 恋爱大师的对话记忆装配。
 *
 * <ul>
 *   <li>{@code mysql.enabled=true}：使用 MySQL（你已有的 RDS MySQL）持久化对话记忆；</li>
 *   <li>默认（false）：使用本地文件持久化（FileBasedChatMemory）。</li>
 * </ul>
 */
@Configuration
public class LoveChatMemoryConfig {

    @Bean(name = "loveChatMemory")
    @ConditionalOnProperty(prefix = "mysql", name = "enabled", havingValue = "true")
    public ChatMemory mysqlChatMemory(
            @Value("${mysql.url}") String url,
            @Value("${mysql.username}") String username,
            @Value("${mysql.password}") String password) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        MySqlChatMemory chatMemory = new MySqlChatMemory(jdbcTemplate);
        chatMemory.initSchema();
        return chatMemory;
    }

    @Bean(name = "loveChatMemory")
    @ConditionalOnProperty(prefix = "mysql", name = "enabled", havingValue = "false", matchIfMissing = true)
    public ChatMemory fileChatMemory() {
        return new FileBasedChatMemory(System.getProperty("user.dir") + "/chat-memory/love-master");
    }
}
