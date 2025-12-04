package com.player.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TextUtils.java - 简单文本分割工具类
public class TextUtils {

    /**
     * 基础文档分割方法（按段落分割）
     */
    public static List<String> splitDocument(String document) {
        // 简单按空行分割段落
        return Arrays.stream(document.split("\\n\\n+"))
                .filter(chunk -> !chunk.isBlank())
                .toList();
    }

    /**
     * 带字符限制的分割方法（最大长度500字符）
     */
    public static List<String> splitByLength(String text, int maxLength) {
        List<String> chunks = new ArrayList<>();
        int index = 0;

        while (index < text.length()) {
            int end = Math.min(index + maxLength, text.length());
            chunks.add(text.substring(index, end));
            index = end;
        }

        return chunks;
    }

    /**
     * 句子感知分割（需要更复杂的NLP处理）
     */
    public static List<String> splitIntoSentences(String text) {
        // 此处可集成NLP库进行实际句子分割
        return Arrays.stream(text.split("[.!?]\\s+"))
                .filter(s -> !s.isBlank())
                .toList();
    }
}