package com.player.common.utils;

import java.util.HashMap;
import java.util.Map;

public class FileTypeUtil {
    private static final Map<String, String> MIME_TYPE_TO_EXTENSION = new HashMap<>();

    static {
        // 常见 MIME 类型映射
        MIME_TYPE_TO_EXTENSION.put("image/jpeg", ".jpg");
        MIME_TYPE_TO_EXTENSION.put("image/png", ".png");
        MIME_TYPE_TO_EXTENSION.put("image/gif", ".gif");
        MIME_TYPE_TO_EXTENSION.put("application/pdf", ".pdf");
        MIME_TYPE_TO_EXTENSION.put("audio/mpeg", ".mp3");
        MIME_TYPE_TO_EXTENSION.put("video/mp4", ".mp4");
        MIME_TYPE_TO_EXTENSION.put("text/plain", ".txt");
        MIME_TYPE_TO_EXTENSION.put("application/msword", ".doc");
        MIME_TYPE_TO_EXTENSION.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
        MIME_TYPE_TO_EXTENSION.put("application/vnd.ms-excel", ".xls");
        MIME_TYPE_TO_EXTENSION.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx");
        // 可以继续添加更多类型
    }

    public static String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return ".dat"; // 默认扩展名
        }

        // 处理可能包含字符集的情况，如 "text/plain; charset=utf-8"
        String cleanMimeType = mimeType.split(";")[0].trim();

        return MIME_TYPE_TO_EXTENSION.getOrDefault(cleanMimeType.toLowerCase(), ".dat");
    }

    public static String getExtensionFromBase64Header(String base64Header) {
        if (base64Header == null || !base64Header.contains(":")) {
            return ".dat";
        }

        // 从类似 "data:image/png;base64" 的字符串中提取 MIME 类型
        String mimeType = base64Header.split(":")[1].split(";")[0];
        return getExtensionFromMimeType(mimeType);
    }

    public static String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return ""; // 如果路径为空，返回空字符串
        }

        int lastDotIndex = filePath.lastIndexOf('.');
        int lastSeparatorIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));

        // 确保 '.' 在文件名部分而不是路径部分
        if (lastDotIndex > lastSeparatorIndex && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex).toLowerCase(); // 返回扩展名（包含 "."）
        }

        return ""; // 如果没有找到有效的扩展名，返回空字符串
    }

}
