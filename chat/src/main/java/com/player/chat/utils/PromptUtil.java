package com.player.chat.utils;

import com.player.chat.entity.ChatParamsEntity;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsIn;
import opennlp.tools.util.StringUtil;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;

public class PromptUtil {
    public static String buildContext(EmbeddingModel nomicEmbeddingModel, ElasticsearchEmbeddingStore elasticsearchEmbeddingStore, ChatParamsEntity chatParamsEntity) {
        // 创建过滤条件
        Embedding queryEmbedding = nomicEmbeddingModel.embed(chatParamsEntity.getPrompt()).content();
        IsEqualTo userIdFilter = new IsEqualTo("user_id", chatParamsEntity.getUserId());
        IsEqualTo tenantIdFilter = new IsEqualTo("tenant_id", chatParamsEntity.getTenantId());
        Filter filter = Filter.and(userIdFilter, tenantIdFilter);
        ArrayList<String> docIds = chatParamsEntity.getDocIds();
        if(docIds != null && docIds.size() != 0){
            IsIn isIn = new IsIn("doc_id", docIds);
            filter = Filter.and(isIn, filter);
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

