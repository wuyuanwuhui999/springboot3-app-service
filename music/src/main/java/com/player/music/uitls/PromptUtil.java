package com.player.music.uitls;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PromptUtil {
    public static String buildContext(List<Document> documents) {
        StringBuilder context = new StringBuilder();
        context.append("相关参考内容：\n");

        for (Document doc : documents) {
            context.append("- ").append(doc.getFormattedContent()).append("\n");
            context.append("  来源：").append(doc.getMetadata().get("source")).append("\n");
            context.append("\n");
        }

        return context.toString();
    }

    public static String buildPrompt(String query, String context) {
        return String.format("""
            基于以下参考内容回答问题。如果参考内容不足以回答问题，请如实告知。
            
            %s
            
            问题：%s
            
            请给出专业、准确的回答：
            """, context, query);
    }

    public static List<Document> convertToDocument(MultipartFile file,String docId) throws IOException {
        List<Document> documents = new ArrayList<>();
        if (file.getContentType().equals("application/pdf")) {
            // PDF文件处理
            try ( PDDocument pdfDocument = Loader.loadPDF(file.getBytes())) {
                PDFTextStripper stripper = new PDFTextStripper();
                for (int page = 1; page <= pdfDocument.getNumberOfPages(); page++) {
                    stripper.setStartPage(page);
                    stripper.setEndPage(page);
                    String text = stripper.getText(pdfDocument);

                    documents.add(new Document(
                            file.getOriginalFilename() + "-page-" + page,
                            text,
                            Map.of(
                                    "doc_id", docId,
                                    "type", "pdf",
                                    "page", page,
                                    "filename", file.getOriginalFilename()
                            )
                    ));
                }
            }
        } else {
            // 文本文件处理
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            documents.add(new Document(
                    file.getOriginalFilename(),
                    content,
                    Map.of(
                            "type", "text",
                            "filename", file.getOriginalFilename()
                    )
            ));
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
