package com.player.chat.utils;

import com.player.chat.entity.ChatParamsEntity;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import dev.langchain4j.store.embedding.filter.comparison.IsIn;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;

public class PromptUtil {
    public static String buildContext(EmbeddingModel nomicEmbeddingModel, EmbeddingStore chromaEmbeddingStore, ChatParamsEntity chatParamsEntity) {
        // 创建过滤条件（可访问范围 OR）：自己的文档 / 租户内公开 / 公司内公开
        Embedding queryEmbedding = nomicEmbeddingModel.embed(chatParamsEntity.getPrompt()).content();

        String userId = chatParamsEntity.getUserId();
        String tenantId = chatParamsEntity.getTenantId();
        String companyId = chatParamsEntity.getCompanyId();

        // 自己的文档（该租户下）
        Filter ownDocsFilter = Filter.and(
                new IsEqualTo("user_id", userId),
                new IsEqualTo("tenant_id", tenantId)
        );
        // 租户内公开
        Filter tenantPublicFilter = Filter.and(
                new IsEqualTo("permission", "tenant"),
                new IsEqualTo("tenant_id", tenantId)
        );
        Filter filter = Filter.or(ownDocsFilter, tenantPublicFilter);

        // 公司内公开（companyId 存在时）
        if (companyId != null && !companyId.isEmpty()) {
            Filter companyPublicFilter = Filter.and(
                    new IsEqualTo("permission", "company"),
                    new IsEqualTo("company_id", companyId)
            );
            filter = Filter.or(filter, companyPublicFilter);
        }

        ArrayList<String> docIds = chatParamsEntity.getDocIds();
        if(docIds != null && docIds.size() != 0){
            IsIn isIn = new IsIn("doc_id", docIds);
            filter = Filter.and(isIn, filter);
        }
        EmbeddingSearchResult<TextSegment> relevant = chromaEmbeddingStore.search(
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
            // 注意：同一个 Chroma 库里，metadata 的取值类型两端不一致（FastAPI 侧 page 存的是数字、Spring 侧存的是字符串），
            // 直接用 getString 会抛 "cannot be returned as a String"，这里用 toMap() 取原始值再转字符串
            java.util.Map<String, Object> metadataMap = segment.metadata().toMap();
            String filename = toText(metadataMap.get("filename"));
            // page：Spring 侧存的是总页数字符串，FastAPI 侧存的是当前分片序号
            String page = toText(metadataMap.get("page"));
            // 文档创建时间（老数据没写过这个字段时兜底显示「未知」，交给提示词规则按最旧处理）
            String createTime = toText(metadataMap.get("create_time"));
            if (createTime == null || createTime.trim().isEmpty()) {
                createTime = "未知";
            }

            contextBuilder.append("文档来源: ").append(filename)
                    .append(", 第").append(page).append("页")
                    .append(", 创建时间: ").append(createTime).append("\n")
                    .append("内容: ").append(segment.text()).append("\n\n");
        }

        return contextBuilder.toString();
    }

    /** metadata 原始值转文本（值为 null 时返回空串，数字/字符串都能处理） */
    private static String toText(Object value) {
        return value == null ? "" : String.valueOf(value);
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

