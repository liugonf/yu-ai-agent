package com.yuoi.yuaiagent.agent;

import com.yuoi.yuaiagent.tools.TerminateTool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智能体工具装配：把 MCP 工具（若启用）与本地终止工具合并为 allTools。
 */
@Configuration
public class AgentConfig {

    @Bean
    public ToolCallback[] allTools(
            @Qualifier("mcpToolCallbacks") ObjectProvider<ToolCallbackProvider> mcpTools,
            TerminateTool terminateTool) {
        List<ToolCallback> all = new ArrayList<>();
        ToolCallbackProvider provider = mcpTools.getIfAvailable();
        if (provider != null) {
            all.addAll(Arrays.asList(provider.getToolCallbacks()));
        }
        all.addAll(Arrays.asList(MethodToolCallbackProvider.builder()
                .toolObjects(terminateTool)
                .build()
                .getToolCallbacks()));
        return all.toArray(ToolCallback[]::new);
    }
}
