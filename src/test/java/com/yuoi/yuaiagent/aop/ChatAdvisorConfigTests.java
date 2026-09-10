package com.yuoi.yuaiagent.aop;

import static org.assertj.core.api.Assertions.assertThat;

import com.yuoi.yuaiagent.service.AiChatService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * 验证 ChatAdvisor（Pointcut + 拦截器）已注册，且
 * {@link AiChatService} 被自动代理（即切点命中、织入生效）。
 */
@SpringBootTest
class ChatAdvisorConfigTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void chatAdvisorBeanIsRegistered() {
        assertThat(applicationContext.getBean("chatAdvisor"))
                .isInstanceOf(Advisor.class);
    }

    @Test
    void chatServiceIsProxiedByAdvisor() {
        AiChatService service = applicationContext.getBean(AiChatService.class);
        // 命中切点的 Bean 会被自动代理（CGLIB），说明 Advisor 织入生效
        assertThat(AopUtils.isAopProxy(service)).isTrue();
    }
}
