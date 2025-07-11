package com.player.music.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStoreOptions;
import org.elasticsearch.client.RestClient;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
public class ElasticsearchUserAwareVectorStore extends ElasticsearchVectorStore implements UserAwareVectorStore {

    private String currentUser;

    public ElasticsearchUserAwareVectorStore(RestClient restClient, EmbeddingModel embeddingModel, ElasticsearchVectorStoreOptions options) {
        super(ElasticsearchVectorStore.builder(restClient, embeddingModel)
                .options(options)
                .initializeSchema(true)
                .batchingStrategy(new TokenCountBatchingStrategy())
        );
    }

    @Override
    public void setCurrentUser(String userId) {
        this.currentUser = userId;
    }

    @Override
    public String getCurrentUser() {
        return currentUser;
    }

    @Override
    public void add(List<Document> documents) {
        // 根据内容大小动态调整批次
        int maxBatchSizeBytes = 2 * 1024 * 1024; // 2MB
        int currentBatchSize = 0;
        List<Document> currentBatch = new ArrayList<>();

        for (Document doc : documents) {
            int docSize = estimateDocumentSize(doc);

            if (currentBatchSize + docSize > maxBatchSizeBytes && !currentBatch.isEmpty()) {
                processBatch(currentBatch);
                currentBatch.clear();
                currentBatchSize = 0;
            }

            currentBatch.add(doc);
            currentBatchSize += docSize;
        }

        if (!currentBatch.isEmpty()) {
            processBatch(currentBatch);
        }
    }

    private int estimateDocumentSize(Document doc) {
        // 简单估算：内容长度 + 元数据序列化后的预估大小
        return doc.getFormattedContent().length() + doc.getMetadata().toString().length() * 2;
    }

    private void processBatch(List<Document> batch) {
        List<Document> docsWithUser = batch.stream()
                .map(doc -> {
                    Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
                    metadata.put("user_id", currentUser);
                    return new Document(doc.getFormattedContent(), metadata);
                })
                .collect(Collectors.toList());
        super.add(docsWithUser);
    }

    @Override
    public void delete(List<String> idList) {
        // 创建用户ID过滤条件
        FilterExpressionBuilder.Op userFilterOp = new FilterExpressionBuilder().eq("user_id", currentUser);

        // 创建文档ID过滤条件
        FilterExpressionBuilder.Op idFilterOp = new FilterExpressionBuilder().in("doc_id", idList);

        // 组合两个条件
        FilterExpressionBuilder.Op combinedFilterOp = new FilterExpressionBuilder().and(idFilterOp, userFilterOp);

        // 添加调试日志
        log.debug("Deleting documents with filter: {}", combinedFilterOp.build());

        super.delete(combinedFilterOp.build());
    }

    @Override
    public void delete(Filter.Expression filterExpression) {
        // 创建用户ID过滤条件
        FilterExpressionBuilder.Op userFilterOp = new FilterExpressionBuilder().eq("user_id", currentUser);

        if (filterExpression != null) {
            // 将原始过滤器转换为Op
            FilterExpressionBuilder.Op originalFilterOp = new FilterExpressionBuilder.Op(filterExpression);
            // 组合两个条件
            FilterExpressionBuilder.Op combinedFilterOp = new FilterExpressionBuilder().and(originalFilterOp, userFilterOp);
            super.delete(combinedFilterOp.build());
        } else {
            // 只有用户ID过滤条件
            super.delete(userFilterOp.build());
        }
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        // 添加用户ID过滤条件
        Filter.Expression originalFilter = request.getFilterExpression();

        // 创建用户ID过滤条件
        FilterExpressionBuilder.Op userFilterOp = new FilterExpressionBuilder().eq("user_id", currentUser);

        if (originalFilter != null) {
            // 将原始过滤器转换为Op
            FilterExpressionBuilder.Op originalFilterOp = new FilterExpressionBuilder.Op(originalFilter);
            // 组合两个条件
            FilterExpressionBuilder.Op combinedFilterOp = new FilterExpressionBuilder().and(originalFilterOp, userFilterOp);
            Filter.Expression finalFilter = combinedFilterOp.build();

            SearchRequest filteredRequest = SearchRequest.from(request).filterExpression(finalFilter)
                    .build();

            return super.similaritySearch(filteredRequest);
        } else {
            // 只有用户ID过滤条件
            SearchRequest filteredRequest = SearchRequest.from(request).filterExpression(originalFilter).build();

            return super.similaritySearch(filteredRequest);
        }
    }

    @Override
    public List<Document> similaritySearch(String query) {
        SearchRequest request = SearchRequest.builder().query(query).filterExpression(new FilterExpressionBuilder().eq("user_id", currentUser).build()).build();
        return super.similaritySearch(request);
    }
}