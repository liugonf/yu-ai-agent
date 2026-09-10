package com.yuoi.yuaiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 恋爱大师 - 云知识库 RAG 配置。
 *
 * <p>实测你的账号是“百炼专属网关 + 云知识库(agent_id=aid-xxx)”模式，因此这里
 * 使用自定义的 {@link DashScopeKnowledgeRetriever} 直连专属网关的
 * {@code /api/v1/indices/knowledge/search} 接口，再用
 * {@link RetrievalAugmentationAdvisor} 在问答前自动检索并拼入上下文。</p>
 */
@Configuration
public class LoveCloudRagConfig {

    @Bean
    public Advisor loveRagCloudAdvisor(
            @Value("${love.rag.base-url}") String baseUrl,
            @Value("${love.rag.api-key}") String apiKey,
            @Value("${love.rag.agent-id}") String agentId) {
        DocumentRetriever documentRetriever = new DashScopeKnowledgeRetriever(baseUrl, apiKey, agentId);

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .order(10)
                .build();
    }
}
