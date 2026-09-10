package com.yuoi.imagesearch;

import com.yuoi.imagesearch.tool.ImageSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * 图片搜索 MCP 服务端启动类。
 *
 * <p>支持两种传输方式（通过 spring.profiles.active 切换）：</p>
 * <ul>
 *   <li>stdio：作为客户端子进程运行（application-stdio.yml）</li>
 *   <li>sse：作为独立 Web 服务运行（application-sse.yml，默认端口 8127）</li>
 * </ul>
 */
@SpringBootApplication
public class YuImageSearchMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuImageSearchMcpServerApplication.class, args);
    }

    /**
     * 把带 @Tool 注解的工具注册为 MCP 服务对外提供的工具。
     */
    @Bean
    public ToolCallbackProvider imageSearchTools(ImageSearchTool imageSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(imageSearchTool)
                .build();
    }
}
