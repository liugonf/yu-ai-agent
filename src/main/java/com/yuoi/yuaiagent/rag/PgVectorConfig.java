package com.yuoi.yuaiagent.rag;

import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * PGVector 向量存储配置（阿里云 PostgreSQL Serverless + pgvector 扩展）。
 *
 * <p>默认关闭（{@code pgvector.enabled=false}），在 application.yml 填好云数据库的
 * 公网地址 / 端口 / 库名 / 账号，并把 enabled 改为 true 后，才会在启动时：
 * 1) 创建 Hikari 连接池；2) 建 {@link PgVectorStore}（自动建 vector_store 表）；
 * 3) 把本地恋爱知识文档（含 status 元信息）向量化后写入。</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "pgvector", name = "enabled", havingValue = "true")
public class PgVectorConfig {

    private static final Logger log = LoggerFactory.getLogger(PgVectorConfig.class);

    @Bean
    public VectorStore pgVectorVectorStore(
            @Value("${pgvector.url}") String url,
            @Value("${pgvector.username}") String username,
            @Value("${pgvector.password}") String password,
            @Qualifier("loveEmbeddingModel") EmbeddingModel embeddingModel,
            LoveDocumentLoader documentLoader) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)
                .vectorTableName("vector_store")
                .build();

        List<Document> documents = documentLoader.loadMarkdowns();
        vectorStore.add(documents);
        log.info("已把 {} 个知识切片写入 PGVector 向量库", documents.size());
        return vectorStore;
    }
}
