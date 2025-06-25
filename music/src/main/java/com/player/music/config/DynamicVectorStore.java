package com.player.music.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.web.client.ResourceAccessException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicVectorStore implements VectorStore {

    private final ChromaVectorStore delegate;
    private String currentUser;

    public DynamicVectorStore(ChromaApi chromaApi, EmbeddingModel embeddingModel, String collectionName) {
        this.delegate = ChromaVectorStore.builder(chromaApi, embeddingModel)
                .collectionName(collectionName)
                .build();
    }

    public void setCurrentUser(String userId) {
        this.currentUser = userId;
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
        delegate.add(docsWithUser);
    }

    @Override
    public void delete(List<String> idList) {
        // 可选：实现按用户ID删除文档
        delegate.delete(idList);
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
            delegate.delete(combinedFilterOp.build());
        } else {
            // 只有用户ID过滤条件
            delegate.delete(userFilterOp.build());
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

            return delegate.similaritySearch(filteredRequest);
        } else {
            // 只有用户ID过滤条件
            SearchRequest filteredRequest = SearchRequest.from(request).filterExpression(originalFilter).build();

            return delegate.similaritySearch(filteredRequest);
        }
    }

    @Override
    public List<Document> similaritySearch(String query) {
        SearchRequest request = SearchRequest.builder().query(query).filterExpression(new FilterExpressionBuilder().eq("user_id", currentUser).build()).build();
        return delegate.similaritySearch(request);
    }
}