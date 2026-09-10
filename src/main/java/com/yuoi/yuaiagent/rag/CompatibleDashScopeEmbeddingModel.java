package com.yuoi.yuaiagent.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * 兼容 OpenAI 协议的 DashScope EmbeddingModel。
 *
 * <p>百炼的 embedding 目前建议走 OpenAI 兼容端点：{baseUrl}/compatible-mode/v1/embeddings
 * （body: {"model": "...", "input": [...]}）。区别于 spring-ai-alibaba 内置实现依赖的
 * 旧版 {@code /api/v1/services/embeddings/...} 路由（对你账号实测为 404）。</p>
 */
public class CompatibleDashScopeEmbeddingModel extends AbstractEmbeddingModel {

    private static final Logger log = LoggerFactory.getLogger(CompatibleDashScopeEmbeddingModel.class);

    private static final String EMBEDDINGS_PATH = "/compatible-mode/v1/embeddings";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public CompatibleDashScopeEmbeddingModel(String baseUrl, String apiKey, String model) {
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.restClient = RestClient.builder()
                .defaultHeaders(headers -> {
                    headers.setBearerAuth(apiKey);
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                })
                .build();
        this.baseUrl = baseUrl;
    }

    private final String baseUrl;

    @Override
    public float[] embed(Document document) {
        List<Embedding> embeddings = doEmbed(List.of(document.getFormattedContent(MetadataMode.EMBED)));
        if (embeddings.isEmpty()) {
            throw new IllegalStateException("embedding 返回为空");
        }
        return embeddings.get(0).getOutput();
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = doEmbed(request.getInstructions());
        return new EmbeddingResponse(embeddings);
    }

    private List<Embedding> doEmbed(List<String> texts) {
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(Map.of("model", model, "input", texts));
        } catch (Exception e) {
            throw new IllegalStateException("构造 embedding 请求失败", e);
        }

        String body = restClient.post()
                .uri(trimTrailingSlash(baseUrl) + EMBEDDINGS_PATH)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        List<Embedding> embeddings = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode data = root.path("data");
            for (JsonNode item : data) {
                int index = item.path("index").asInt(-1);
                List<Float> values = new ArrayList<>();
                for (JsonNode v : item.path("embedding")) {
                    values.add((float) v.asDouble());
                }
                float[] vector = new float[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    vector[i] = values.get(i);
                }
                embeddings.add(new Embedding(vector, index));
            }
            log.debug("embedding 成功: {} 条, 维度={}", embeddings.size(),
                    embeddings.isEmpty() ? 0 : embeddings.get(0).getOutput().length);
        } catch (Exception e) {
            throw new IllegalStateException("解析 embedding 响应失败: " + e.getMessage()
                    + ", 原始响应: " + abbreviate(body), e);
        }
        return embeddings;
    }

    private static String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String abbreviate(String body) {
        if (body == null) {
            return "<空>";
        }
        return body.length() <= 300 ? body : body.substring(0, 300) + "...";
    }
}
