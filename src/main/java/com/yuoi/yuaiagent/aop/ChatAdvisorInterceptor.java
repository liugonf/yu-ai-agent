package com.yuoi.yuaiagent.aop;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI 对话顾问拦截器（Advice 部分）。
 *
 * <p>这是一个标准的 AOP Alliance {@link MethodInterceptor}：在目标方法
 * {@code AiChatService.chat(...)} 调用前后进行横切处理，当前实现为统一日志——
 * 记录调用方、入参摘要与耗时，调用成功/异常分别输出 info / warn 日志。
 * 抛出的异常会原样向上传播，不影响既有 {@code GlobalExceptionHandler} 的异常处理。</p>
 */
public class ChatAdvisorInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(ChatAdvisorInterceptor.class);

    /** 单个入参打印时的最大长度，避免完整内容刷屏或泄敏。 */
    private static final int MAX_ARG_LENGTH = 200;

    /** 最多打印的入参数目。 */
    private static final int MAX_ARG_COUNT = 5;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        String target = describeTarget(invocation);
        String argsPreview = previewArgs(invocation.getArguments());
        long startNanos = System.nanoTime();
        boolean succeeded = false;
        try {
            Object result = invocation.proceed();
            succeeded = true;
            return result;
        } finally {
            long costMs = (System.nanoTime() - startNanos) / 1_000_000;
            if (succeeded) {
                log.info("[ChatAdvisor] 调用成功 {}.{}() 耗时 {} ms，入参={}",
                        target, method.getName(), costMs, argsPreview);
            } else {
                log.warn("[ChatAdvisor] 调用异常 {}.{}() 耗时 {} ms，入参={}",
                        target, method.getName(), costMs, argsPreview);
            }
        }
    }

    private String describeTarget(MethodInvocation invocation) {
        Object target = invocation.getThis();
        return target == null ? "?" : target.getClass().getName();
    }

    private String previewArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        return Arrays.stream(args)
                .limit(MAX_ARG_COUNT)
                .map(ChatAdvisorInterceptor::abbreviate)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String abbreviate(Object arg) {
        String text = arg == null ? "null" : String.valueOf(arg);
        return text.length() <= MAX_ARG_LENGTH
                ? text
                : text.substring(0, MAX_ARG_LENGTH) + "...(截断)";
    }
}
