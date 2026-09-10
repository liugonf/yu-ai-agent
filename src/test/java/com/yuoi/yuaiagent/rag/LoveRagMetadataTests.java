package com.yuoi.yuaiagent.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * 验证知识文档元信息（filename + status）与基于状态的过滤顾问（作业 2）。
 * 不调用 AI / 向量化，纯本地断言。
 */
class LoveRagMetadataTests {

    @Test
    void documentsCarryFilenameAndStatusMetadata() {
        LoveDocumentLoader loader = new LoveDocumentLoader(new PathMatchingResourcePatternResolver());
        List<Document> documents = loader.loadMarkdowns();

        assertThat(documents).isNotEmpty();
        Set<String> statuses = Set.of("单身", "恋爱", "已婚");
        for (Document document : documents) {
            assertThat(document.getMetadata()).containsKey("filename");
            assertThat(document.getMetadata()).containsKey("status");
            assertThat(document.getMetadata().get("status")).isIn(statuses);
        }
    }

    @Test
    void filterAdvisorIsBuiltForStatus() {
        VectorStore vectorStore = mock(VectorStore.class);
        assertThat(LoveRagFilterAdvisorFactory.create(vectorStore, "已婚")).isNotNull();
        assertThat(LoveRagFilterAdvisorFactory.create(vectorStore, "单身")).isNotNull();
    }
}
