package com.yuoi.yuaiagent.aop;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 对话顾问 Advisor 配置。
 *
 * <p>把一个 Pointcut（切点，这里用 AspectJ 表达式匹配
 * {@code AiChatService.chat} 方法）和一个 Advice（拦截器，{@link ChatAdvisorInterceptor}）
 * 组合成标准的 Spring AOP {@link Advisor} 并注册为 Bean，由 Spring 的自动代理机制
 * （AutoProxyCreator）对匹配到的目标 Bean 织入代理。</p>
 *
 * <p>如需临时关闭拦截，可在 application.yml 中加入：
 * <pre>{@code chat:
 *   advisor:
 *     enabled: false}</pre>
 * </p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "chat.advisor", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ChatAdvisorConfig {

    /**
     * 切点表达式：拦截 AiChatService 中所有名为 chat 的方法。
     */
    private static final String CHAT_METHOD_POINTCUT =
            "execution(* com.yuoi.yuaiagent.service.AiChatService.chat(..))";

    /**
     * 拦截器（Advice）：负责在方法调用前后做统一横切处理。
     */
    @Bean
    public ChatAdvisorInterceptor chatAdvisorInterceptor() {
        return new ChatAdvisorInterceptor();
    }

    /**
     * 组合 Pointcut + Advice 的 Advisor。
     *
     * @param interceptor 上面定义的拦截器
     * @return 作用于 AiChatService.chat 的顾问 Advisor
     */
    @Bean
    public Advisor chatAdvisor(ChatAdvisorInterceptor interceptor) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(CHAT_METHOD_POINTCUT);
        return new DefaultPointcutAdvisor(pointcut, interceptor);
    }
}
