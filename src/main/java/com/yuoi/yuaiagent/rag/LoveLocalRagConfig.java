package com.yuoi.yuaiagent.rag;

import java.io.File;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 恋爱大师 - 本地知识库 RAG 配置。
 *
 * <p>实现教程“Spring AI + 本地知识库”的完整链路：</p>
 * <ol>
 *   <li>向量化模型：DashScope text-embedding-v3（走百炼公共端点）</li>
 *   <li>向量存储：内存版 {@link SimpleVectorStore}（带本地文件缓存，重启不重复向量化）</li>
 *   <li>问答增强：{@link QuestionAnswerAdvisor}（自动检索并把文档拼进用户提问）</li>
 * </ol>
 *
 * <p>向量化是懒加载的：只有第一次真正走 RAG 问答时才执行，避免启动/测试时产生费用。</p>
 */
@Configuration
public class LoveLocalRagConfig {

    private static final Logger log = LoggerFactory.getLogger(LoveLocalRagConfig.class);

    /** 向量库缓存文件：存在则直接加载，避免每次重启都重新调用 Embedding。 */
    private static final String STORE_CACHE_FILE =
            System.getProperty("user.dir") + "/rag-store/love-simple-vector-store.json";

    /**
     * 向量化 EmbeddingModel（自定义实现，走百炼 OpenAI 兼容端点
     * {@code /compatible-mode/v1/embeddings}，公共域或专属网关均可配置）。
     */
    @Bean
    @Primary
    public EmbeddingModel loveEmbeddingModel(
            @Value("${embedding.api-key:${dashscope.api-key}}") String apiKey,
            @Value("${embedding.base-url:https://dashscope.aliyuncs.com}") String baseUrl,
            @Value("${embedding.model:text-embedding-v3}") String model) {
        return new CompatibleDashScopeEmbeddingModel(baseUrl, apiKey, model);
    }

    /**
     * 懒加载的本地向量库服务：首次 RAG 问答时才切分文档并向量化入库。
     */
    @Bean
    public LoveLocalVectorStoreService loveLocalVectorStoreService(
            @Qualifier("loveEmbeddingModel") EmbeddingModel embeddingModel,
            LoveDocumentLoader documentLoader) {
        return new LoveLocalVectorStoreService(embeddingModel, documentLoader, STORE_CACHE_FILE);
    }

    /**
     * 问答顾问的工厂入口：把它注入到 ChatClient 的 defaultAdvisors 即可开启本地知识库问答。
     */
    public static class LoveLocalVectorStoreService {

        private final EmbeddingModel embeddingModel;
        private final LoveDocumentLoader documentLoader;
        private final File storeFile;

        private volatile SimpleVectorStore vectorStore;
        private volatile QuestionAnswerAdvisor questionAnswerAdvisor;

        LoveLocalVectorStoreService(EmbeddingModel embeddingModel,
                LoveDocumentLoader documentLoader, String storeFile) {
            this.embeddingModel = embeddingModel;
            this.documentLoader = documentLoader;
            this.storeFile = new File(storeFile);
        }

        /**
         * 获取（必要时构建）本地向量库问答顾问。
         */
        public synchronized QuestionAnswerAdvisor questionAnswerAdvisor() {
            if (questionAnswerAdvisor == null) {
                ensureVectorStoreLoaded();
                questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);
            }
            return questionAnswerAdvisor;
        }

        /**
         * 获取（必要时构建）本地向量库，供自定义检索器 / 过滤顾问使用。
         */
        public synchronized VectorStore vectorStore() {
            ensureVectorStoreLoaded();
            return vectorStore;
        }

        private void ensureVectorStoreLoaded() {
            if (vectorStore != null) {
                return;
            }
            log.info("开始初始化本地恋爱知识库（首次使用，需要调用 Embedding 向量化）...");
            SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
            if (storeFile.exists()) {
                store.load(storeFile);
                log.info("已从缓存加载向量库: {}", storeFile.getAbsolutePath());
            } else {
                List<Document> documents = documentLoader.loadMarkdowns();
                if (documents.isEmpty()) {
                    throw new IllegalStateException("未加载到任何知识文档，请检查 classpath:document/*.md");
                }
                store.add(documents);
                if (storeFile.getParentFile() != null) {
                    storeFile.getParentFile().mkdirs();
                }
                store.save(storeFile);
                log.info("知识文档向量化完成，共 {} 个切片，已缓存到 {}", documents.size(), storeFile.getAbsolutePath());
            }
            this.vectorStore = store;
        }
    }
}
