package com.player.music.config;

import org.springframework.ai.vectorstore.VectorStore;

public interface UserAwareVectorStore extends VectorStore {
    void setCurrentUser(String userId);
    String getCurrentUser();
}