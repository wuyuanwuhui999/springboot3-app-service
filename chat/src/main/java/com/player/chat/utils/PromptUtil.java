package com.player.chat.utils;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import opennlp.tools.util.StringUtil;
import org.springframework.web.multipart.MultipartFile;

public class PromptUtil {
    public static String buildContext(EmbeddingModel nomicEmbeddingModel, ElasticsearchEmbeddingStore elasticsearchEmbeddingStore, String query,String userId,String directoryId) {
        // 创建过滤条件
        Embedding queryEmbedding = nomicEmbeddingModel.embed(query).content();
        IsEqualTo userIdFilter = new IsEqualTo("metadata.user_id", userId);
        Filter filter;
        if(directoryId != null && !StringUtil.isEmpty(directoryId)){
            IsEqualTo directoryFilter = new IsEqualTo("metadata.directory_id", directoryId);
            filter = Filter.and(directoryFilter, userIdFilter);
        }else{
            filter = userIdFilter;
        }
        EmbeddingSearchResult<TextSegment> relevant = elasticsearchEmbeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .filter(filter)
                        .build());
        if (relevant.matches().isEmpty()) {
            return "";
        }
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("以下是一些相关的文档摘录，可能有助于回答您的问题:\n\n");

        for (EmbeddingMatch<TextSegment> match : relevant.matches()) {
            TextSegment segment = match.embedded();
            String filename = segment.metadata().getString("filename");
            String page = segment.metadata().getString("page");

            contextBuilder.append("文档来源: ").append(filename)
                    .append(", 第").append(page).append("页\n")
                    .append("内容: ").append(segment.text()).append("\n\n");
        }

        return contextBuilder.toString();
    }

    public static String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "";
        }

        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == originalFilename.length() - 1) {
            return ""; // 没有后缀或以 . 结尾
        }

        return originalFilename.substring(dotIndex + 1); // 返回后缀，如 "jpg"
    }
}

