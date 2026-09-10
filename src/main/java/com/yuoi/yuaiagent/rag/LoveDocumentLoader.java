package com.yuoi.yuaiagent.rag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * 恋爱知识库文档加载器（本地 RAG 的“文档读取”环节）。
 *
 * <p>读取 {@code classpath:document/*.md} 下的 Markdown 知识文档，转成
 * {@link Document} 列表，并把文件名写入元信息 {@code filename}，便于检索时溯源。</p>
 */
@Component
public class LoveDocumentLoader {

    private static final Logger log = LoggerFactory.getLogger(LoveDocumentLoader.class);

    private static final String DOCUMENT_PATH_PATTERN = "classpath:document/*.md";

    private final ResourcePatternResolver resourcePatternResolver;

    public LoveDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources(DOCUMENT_PATH_PATTERN);
            for (Resource resource : resources) {
                MarkdownDocumentReader reader = new MarkdownDocumentReader(
                        resource, MarkdownDocumentReaderConfig.defaultConfig());
                List<Document> documents = reader.get();
                String fileName = resource.getFilename();
                String status = resolveStatus(fileName);
                for (Document document : documents) {
                    // 追加“来源文件名”与“恋爱状态”元信息，便于检索溯源与按状态过滤
                    document.getMetadata().put("filename", fileName);
                    document.getMetadata().put("status", status);
                }
                allDocuments.addAll(documents);
                log.info("已加载知识文档 {}（状态={}），切分为 {} 个文档块", fileName, status, documents.size());
            }
        } catch (IOException e) {
            log.error("Markdown 知识文档加载失败", e);
        }
        return allDocuments;
    }

    /**
     * 根据文件名判断文档所属的恋爱状态，作为后续元信息过滤的标签。
     * 取值：单身 / 恋爱 / 已婚。
     */
    private static String resolveStatus(String fileName) {
        if (fileName == null) {
            return "未知";
        }
        String name = fileName.toLowerCase();
        if (name.contains("single") || name.contains("单身")) {
            return "单身";
        }
        if (name.contains("married") || name.contains("已婚")) {
            return "已婚";
        }
        if (name.contains("dating") || name.contains("恋爱")) {
            return "恋爱";
        }
        return "未知";
    }
}
