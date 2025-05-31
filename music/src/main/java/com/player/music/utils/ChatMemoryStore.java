package com.player.music.utils;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatMemoryStore {
    private static final Map<String, ChatMemory> memories = new ConcurrentHashMap<>();
    private static final int MAX_MESSAGES = 20;

    public static ChatMemory getOrCreate(String chatId) {
        return memories.computeIfAbsent(chatId, id -> MessageWindowChatMemory.withMaxMessages(MAX_MESSAGES));
    }
}