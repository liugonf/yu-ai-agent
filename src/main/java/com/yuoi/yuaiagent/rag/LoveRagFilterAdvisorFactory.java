package com.yuoi.yuaiagent.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 恋爱大师 RAG 过滤顾问工厂（作业 2：基于元信息的过滤）。
 *
 * <p>文档在加载时已打上 {@code status}（单身 / 恋爱 / 已婚）元信息，这里按传入状态
 * 构造带过滤表达式的 {@link VectorStoreDocumentRetriever}，再包进
 * {@link RetrievalAugmentationAdvisor}，让检索只在该状态的知识切片里进行。</p>
 */
public final class LoveRagFilterAdvisorFactory {

    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.5;

    private static final int DEFAULT_TOP_K = 3;

    private LoveRagFilterAdvisorFactory() {
    }

    public static Advisor create(VectorStore vectorStore, String status) {
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression)
                .similarityThreshold(DEFAULT_SIMILARITY_THRESHOLD)
                .topK(DEFAULT_TOP_K)
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .order(10)
                .build();
    }
}
