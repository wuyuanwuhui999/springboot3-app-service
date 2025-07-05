package com.player.ai.utils;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.nomic.NomicEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class PromptUtil {
    public static String buildContext(EmbeddingModel nomicEmbeddingModel, ElasticsearchEmbeddingStore elasticsearchEmbeddingStore, String query) {
        Embedding queryEmbedding = nomicEmbeddingModel.embed(query).content();
        EmbeddingSearchResult<TextSegment> relevant = elasticsearchEmbeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .build());
        if (relevant.matches().isEmpty()) {
            return "没有找到相关文档";
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

    public static String buildPrompt(String query, String context) {
        return String.format("""
            基于以下参考内容回答问题。如果参考内容不足以回答问题，请如实告知。
            
            %s
            
            问题：%s
            
            请给出专业、准确的回答：
            """, context, query);
    }

    public static List<Document> convertToDocument(MultipartFile file) throws IOException {
        List<Document> documents = new ArrayList<>();
        if (file.getContentType().equals("application/pdf")) {
            // PDF文件处理
            try ( PDDocument pdfDocument = Loader.loadPDF(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                for (int page = 1; page <= pdfDocument.getNumberOfPages(); page++) {
                    stripper.setStartPage(page);
                    stripper.setEndPage(page);
                    String text = stripper.getText(pdfDocument);


                }
            }
        } else {
            // 文本文件处理
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        }
        return documents;
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

