package com.yuoi.imagesearch.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * 图片搜索工具：调用 SearchAPI.io。
 *
 * <p>优先使用专门的 Google Images 引擎（engine=google_images），出图更稳定；
 * 若没有结果，再回退到百度搜索（engine=baidu）的图片卡片 / 普通结果缩略图。</p>
 */
@Service
public class ImageSearchTool {

    private static final String API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;

    private final RestClient restClient = RestClient.create();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImageSearchTool(@Value("${searchapi.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "根据关键词从网络搜索图片，返回若干张图片的 URL 列表")
    public String searchImage(@ToolParam(description = "图片搜索关键词，例如 cat、风景") String query) {
        if (!StringUtils.hasText(apiKey)) {
            return "未配置 SearchAPI.io Key，请在服务端配置 searchapi.api-key";
        }
        try {
            List<String> images = searchMediumImages(query);
            return images.isEmpty() ? "没有搜索到相关图片" : String.join(",", images);
        } catch (Exception e) {
            return "搜索图片出错: " + e.getMessage();
        }
    }

    public List<String> searchMediumImages(String query) {
        List<String> urls = searchGoogleImages(query);
        if (urls.isEmpty()) {
            urls = searchBaiduImages(query);
        }
        return urls.stream().distinct().toList();
    }

    /** 首选：Google Images 图片引擎。 */
    private List<String> searchGoogleImages(String query) {
        List<String> urls = new ArrayList<>();
        try {
            String body = restClient.get()
                    .uri(API_URL + "?engine=google_images&q={q}&api_key={key}", query, apiKey)
                    .retrieve()
                    .body(String.class);
            JsonNode root = objectMapper.readTree(body);
            JsonNode images = root.path("images");
            if (images.isArray()) {
                for (JsonNode image : images) {
                    addText(urls, image.path("original"));
                    addText(urls, image.path("thumbnail"));
                }
            }
        } catch (Exception e) {
            // 引擎不可用或解析失败时静默降级到百度
        }
        return urls;
    }

    /** 回退：百度搜索的图片卡片 / 普通结果缩略图 / 百科缩略图。 */
    private List<String> searchBaiduImages(String query) {
        List<String> urls = new ArrayList<>();
        String body = restClient.get()
                .uri(API_URL + "?engine=baidu&q={q}&api_key={key}", query, apiKey)
                .retrieve()
                .body(String.class);
        try {
            JsonNode root = objectMapper.readTree(body);

            JsonNode inlineImages = root.path("inline_images").path("images");
            if (inlineImages.isArray()) {
                for (JsonNode image : inlineImages) {
                    addText(urls, image.path("thumbnail"));
                }
            }

            JsonNode organic = root.path("organic_results");
            if (organic.isArray()) {
                for (JsonNode item : organic) {
                    addText(urls, item.path("thumbnail"));
                }
            }

            addText(urls, root.path("knowledge_graph").path("thumbnail"));
        } catch (Exception e) {
            throw new IllegalStateException("解析百度搜索响应失败: " + e.getMessage()
                    + "，响应片段: " + abbreviate(body), e);
        }
        return urls;
    }

    private static void addText(List<String> urls, JsonNode node) {
        if (node != null && node.isTextual()) {
            urls.add(node.asText());
        }
    }

    private static String abbreviate(String body) {
        if (body == null) {
            return "<空>";
        }
        return body.length() <= 300 ? body : body.substring(0, 300) + "...";
    }
}
