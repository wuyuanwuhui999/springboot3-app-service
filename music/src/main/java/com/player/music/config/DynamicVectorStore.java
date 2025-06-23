package com.player.music.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicVectorStore implements VectorStore {
    private final EmbeddingModel embeddingModel;
    private final Map<String, SimpleVectorStore> userVectorStores = new ConcurrentHashMap<>();
    private final String baseDir = "G:\\static\\ai\\";
    private final ThreadLocal<String> currentUserId = new ThreadLocal<>();
    private final FilterExpressionTextParser filterParser = new FilterExpressionTextParser();

    public DynamicVectorStore(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 设置当前用户ID
     */
    public void setCurrentUser(String userId) {
        currentUserId.set(userId);
        // 确保用户向量存储已初始化
        getVectorStore(userId);
    }

    /**
     * 获取指定用户的向量存储
     */
    private SimpleVectorStore getVectorStore(String userId) {
        return userVectorStores.computeIfAbsent(userId, id -> {
            String filePath = baseDir + id + "_vector_store.json";
            File file = new File(filePath);
            SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
            try {
                if (file.exists()) {
                    store.load(file);
                    System.out.println("Loaded vector store for user: " + id);
                }
            } catch (Exception e) {
                System.err.println("Error loading vector store for user " + id + ": " + e.getMessage());
            }
            return store;
        });
    }

    /**
     * 获取当前用户的向量存储
     */
    private SimpleVectorStore getCurrentVectorStore() {
        String userId = currentUserId.get();
        if (userId == null) {
            throw new IllegalStateException("No current user set. Call setCurrentUser() first.");
        }
        return getVectorStore(userId);
    }

    /**
     * 清除当前用户设置
     */
    public void clearCurrentUser() {
        currentUserId.remove();
    }

    /**
     * 保存所有用户的向量存储
     */
    public void saveAllVectorStores() {
        userVectorStores.forEach((userId, store) -> {
            String filePath = baseDir + userId + "_vector_store.json";
            File file = new File(filePath);
            try {
                store.save(file);
                System.out.println("Vector store data saved successfully for user: " + userId);
            } catch (Exception e) {
                System.err.println("Failed to save vector store data for user: " + userId + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void add(List<Document> documents) {
        SimpleVectorStore store = getCurrentVectorStore();
        // 确保所有文档都有用户ID元数据
        documents.forEach(doc -> doc.getMetadata().putIfAbsent("userId", currentUserId.get()));
        store.add(documents);
    }

    @Override
    public void delete(List<String> idList) {
        getCurrentVectorStore().delete(idList);
    }

    @Override
    public void delete(Filter.Expression filterExpression) {

    }


    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        // 在原有过滤条件上增加用户过滤
        String userFilter = "userId == '" + currentUserId.get() + "'";
        String finalFilter = request.getFilterExpression() == null ?
                userFilter :
                "(" + request.getFilterExpression() + ") && " + userFilter;

        SearchRequest userRequest = SearchRequest
                .builder()
                .similarityThreshold(request.getSimilarityThreshold())
                .filterExpression(finalFilter)
                .topK(request.getTopK())
                .build();

        return getCurrentVectorStore().similaritySearch(userRequest);
    }
}