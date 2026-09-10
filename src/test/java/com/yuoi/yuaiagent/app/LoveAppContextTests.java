package com.yuoi.yuaiagent.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * 验证 Spring AI 相关 Bean 装配成功：DashScope ChatModel 自动配置 + 恋爱大师 LoveApp。
 * （不会真实调用 AI）
 */
@SpringBootTest
class LoveAppContextTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void springAiBeansAreWired() {
        assertThat(applicationContext.getBean(ChatModel.class)).isNotNull();
        assertThat(applicationContext.getBean(LoveApp.class)).isNotNull();
    }
}
