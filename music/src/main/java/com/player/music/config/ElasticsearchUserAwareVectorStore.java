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
        if (idList == null || idList.isEmpty()) {
            return;
        }

        try {
            // 方案1：尝试使用.keyword后缀
            FilterExpressionBuilder.Op userFilterOp = new FilterExpressionBuilder()
                    .eq("metadata.user_id", currentUser);
            FilterExpressionBuilder.Op idFilterOp = new FilterExpressionBuilder()
                    .in("metadata.doc_id", idList.toArray(new String[0]));
            Filter.Expression filter = new FilterExpressionBuilder()
                    .and(idFilterOp, userFilterOp).build();

            log.debug("Attempting to delete with filter: {}", filter);
            super.delete(filter);
        } catch (Exception e) {
            log.error("Failed to delete documents with IDs: {}, for user: {}", idList, currentUser, e);
            throw new IllegalStateException("Failed to delete documents by filter", e);
        }
    }

    @Override
    public void delete(Filter.Expression filterExpression) {
        try {
            // 创建用户ID过滤条件 - 注意访问嵌套的metadata.user_id字段
            FilterExpressionBuilder.Op userFilterOp = new FilterExpressionBuilder()
                    .eq("metadata.user_id", currentUser);

            if (filterExpression != null) {
                // 将原始过滤器转换为Op
                FilterExpressionBuilder.Op originalFilterOp = new FilterExpressionBuilder.Op(filterExpression);
                // 组合两个条件
                FilterExpressionBuilder.Op combinedFilterOp = new FilterExpressionBuilder()
                        .and(originalFilterOp, userFilterOp);
                log.debug("Deleting documents with combined filter: {}", combinedFilterOp.build());
                super.delete(combinedFilterOp.build());
            } else {
                // 只有用户ID过滤条件
                log.debug("Deleting all documents for user: {}", currentUser);
                super.delete(userFilterOp.build());
            }
        } catch (Exception e) {
            log.error("Failed to delete documents with filter for user: {}", currentUser, e);
            throw new IllegalStateException("Failed to delete documents by filter", e);
        }
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        // 创建用户ID和app_id过滤条件
        FilterExpressionBuilder.Op userFilterOp = new FilterExpressionBuilder()
                .eq("metadata.user_id", currentUser);
        FilterExpressionBuilder.Op appFilterOp = new FilterExpressionBuilder()
                .eq("metadata.app_id", "com.player.music");

        // 组合用户ID和app_id条件
        FilterExpressionBuilder.Op baseFilterOp = new FilterExpressionBuilder()
                .and(userFilterOp, appFilterOp);

        Filter.Expression originalFilter = request.getFilterExpression();
        if (originalFilter != null) {
            // 将原始过滤器转换为Op
            FilterExpressionBuilder.Op originalFilterOp = new FilterExpressionBuilder.Op(originalFilter);
            // 组合所有条件
            FilterExpressionBuilder.Op combinedFilterOp = new FilterExpressionBuilder()
                    .and(baseFilterOp, originalFilterOp);
            Filter.Expression finalFilter = combinedFilterOp.build();

            SearchRequest filteredRequest = SearchRequest.from(request)
                    .filterExpression(finalFilter)
                    .build();
            return super.similaritySearch(filteredRequest);
        } else {
            // 只有用户ID和app_id过滤条件
            SearchRequest filteredRequest = SearchRequest.from(request)
                    .filterExpression(baseFilterOp.build())
                    .build();
            return super.similaritySearch(filteredRequest);
        }
    }

    @Override
    public List<Document> similaritySearch(String query) {
        // 创建用户ID和app_id过滤条件
        FilterExpressionBuilder.Op userFilterOp = new FilterExpressionBuilder()
                .eq("metadata.user_id", currentUser);
        FilterExpressionBuilder.Op appFilterOp = new FilterExpressionBuilder()
                .eq("metadata.app_id", "com.player.music");

        // 组合条件
        FilterExpressionBuilder.Op combinedFilterOp = new FilterExpressionBuilder()
                .and(userFilterOp, appFilterOp);

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .filterExpression(userFilterOp.build())
                .build();
        return super.similaritySearch(request);
    }
}