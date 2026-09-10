package com.yuoi.yuaiagent.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * 云知识库文档检索器：对接“阿里云百炼专属网关”的知识库检索接口。
 *
 * <p>你实测可用的接口形态（区别于 spring-ai-alibaba 老版公共端点的 pipeline API）：</p>
 * <pre>
 * POST {base-url}/api/v1/indices/knowledge/search
 * Authorization: Bearer {api-key}
 * {"query": "...", "agent_id": "aid-xxx"}
 * </pre>
 * 返回的 {@code data.nodes[]} 中每个节点含 {@code text}（文档名/标题/正文）与
 * {@code metadata}（doc_name、title、score 等），这里统一映射为 Spring AI 的
 * {@link Document}，供 {@link RetrievalAugmentationAdvisor} 拼入上下文。
 */
public class DashScopeKnowledgeRetriever implements DocumentRetriever {

    private static final Logger log = LoggerFactory.getLogger(DashScopeKnowledgeRetriever.class);

    private static final String SEARCH_PATH = "/api/v1/indices/knowledge/search";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String agentId;

    public DashScopeKnowledgeRetriever(String baseUrl, String apiKey, String agentId) {
        this.agentId = agentId;
        this.objectMapper = new ObjectMapper();
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setBearerAuth(apiKey);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .build();
    }

    @Override
    public List<Document> retrieve(Query query) {
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(
                    Map.of("query", query.text() == null ? "" : query.text(), "agent_id", agentId));
        } catch (Exception e) {
            throw new IllegalStateException("构造知识库检索请求失败", e);
        }

        String body = restClient.post()
                .uri(SEARCH_PATH)
                .body(requestBody)
                .retrieve()
                .body(String.class);
        return parseDocuments(body);
    }

    private List<Document> parseDocuments(String body) {
        List<Document> documents = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode nodes = root.path("data").path("nodes");
            for (JsonNode node : nodes) {
                JsonNode metadataNode = node.path("metadata");
                String text = node.path("text").asText(null);
                if (text == null || text.isBlank()) {
                    text = metadataNode.path("content").asText(null);
                }
                if (text == null || text.isBlank()) {
                    continue;
                }
                Map<String, Object> metadata = new java.util.HashMap<>();
                metadataNode.fields().forEachRemaining(entry ->
                        metadata.put(entry.getKey(), entry.getValue().asText()));
                metadata.putIfAbsent("filename", metadataNode.path("doc_name").asText(""));
                if (node.hasNonNull("score")) {
                    metadata.put("score", node.get("score").asDouble());
                }
                documents.add(new Document(text, metadata));
            }
            log.info("云知识库检索命中 {} 条", documents.size());
        } catch (Exception e) {
            throw new IllegalStateException("解析知识库检索结果失败: " + e.getMessage()
                    + ", 原始响应: " + abbreviate(body), e);
        }
        return documents;
    }

    private static String abbreviate(String body) {
        if (body == null) {
            return "<空>";
        }
        return body.length() <= 300 ? body : body.substring(0, 300) + "...";
    }
}
