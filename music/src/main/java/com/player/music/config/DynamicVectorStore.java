package com.player.music.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        // 为每个文档添加用户ID作为元数据
        List<Document> docsWithUser = documents.stream()
                .map(doc -> {
                    Map<String, Object> metadata = doc.getMetadata();
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