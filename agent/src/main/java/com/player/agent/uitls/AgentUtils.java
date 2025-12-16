package com.player.agent.uitls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.agent.constants.SystemtConstants;
import com.player.agent.entity.AgentParamsEntity;
import com.player.agent.mapper.AgentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class AgentUtils {

    private static AgentMapper agentMapper;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setAgentMapper(AgentMapper agentMapper) {
        AgentUtils.agentMapper = agentMapper;
    }

    public static Flux<String> processChat(
            AgentParamsEntity agentParamsEntity,
            ChatClient chatClient,
            String systemPromptContent
    ) {
        try {
            String prompt = agentParamsEntity.getPrompt();
            String userId = agentParamsEntity.getUserId();
            String chatId = agentParamsEntity.getChatId();
            log.info("开始处理聊天 - 用户: {}, 会话ID: {}, 查询内容: {}", userId, chatId, prompt);

            try {
                // 步骤1：先生成SQL并执行查询
                SQLResult sqlResult = generateAndExecuteSQL(agentParamsEntity, chatClient);
                log.info("SQL查询完成 - 数据条数: {}, 错误: {}",
                        sqlResult.getData().size(),
                        sqlResult.getError());

                // 步骤2：基于SQL结果生成最终回答
                return generateFinalAnswer(agentParamsEntity, chatClient, systemPromptContent, sqlResult);
            } catch (Exception e) {
                log.error("处理聊天时发生错误", e);
                return Flux.just(formatErrorMessage(e, agentParamsEntity.getLanguage()));
            }
        } catch (Exception e) {
            log.error("处理聊天时发生未知错误", e);
            return Flux.error(e);
        }
    }

    /**
     * 生成SQL并执行查询
     */
    private static SQLResult generateAndExecuteSQL(AgentParamsEntity params, ChatClient chatClient) throws JsonProcessingException {
        String userMessage = String.format(
                "用户ID: %s\n" +
                        "查询类型: %s\n" +
                        "用户查询: %s\n\n" +
                        "请根据以下规则生成SQL：\n" +
                        "1. 查询类型为 '%s'，这是普通音乐查询，只需要查询music表\n" +
                        "2. music表没有user_id字段，不要添加user_id条件\n" +
                        "3. 使用模糊查询时用LIKE，精确查询时用=\n" +
                        "4. SQL中的中文字符请使用原始中文字符，不要使用Unicode转义！\n" +
                        "5. 如果查询的是一般音乐信息，不要关联用户表",
                params.getUserId() != null ? params.getUserId() : "未知用户",
                params.getType() != null ? params.getType() : "music",
                params.getPrompt(),
                params.getType() != null ? params.getType() : "music"
        );

        Message systemMessage = new SystemMessage(SystemtConstants.SQL_GENERATOR_PROMPT);
        Message userMessageObj = new UserMessage(userMessage);

        Prompt prompt = new Prompt(List.of(systemMessage, userMessageObj));

        log.info("发送SQL生成请求 - 查询类型: {}, 用户查询: {}", params.getType(), params.getPrompt());

        String sqlResponse = chatClient.prompt(prompt).call().content();
        log.info("收到SQL生成响应: {}", sqlResponse);

        // --- 新增：清理响应内容，移除可能存在的JSON代码块标记 ---
        String cleanedResponse = sqlResponse.trim();
        // 移除开头的 ```json 或 ``` 等标记
        if (cleanedResponse.startsWith("```")) {
            int firstNewline = cleanedResponse.indexOf("\n");
            if (firstNewline != -1) {
                cleanedResponse = cleanedResponse.substring(firstNewline + 1).trim();
            }
        }
        // 移除结尾的 ```
        if (cleanedResponse.endsWith("```")) {
            cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3).trim();
        }
        log.info("清理后的响应内容: {}", cleanedResponse);
        // --- 清理逻辑结束 ---

        JsonNode jsonNode = objectMapper.readTree(cleanedResponse);
        String sql = jsonNode.has("sql") ? jsonNode.get("sql").asText() : "";
        String promptDescription = jsonNode.has("prompt") ? jsonNode.get("prompt").asText() : params.getPrompt();

        SQLResult result = new SQLResult();
        result.setOriginalPrompt(promptDescription);
        result.setSql(sql);
        result.setData(Collections.emptyList());

        if (sql != null && !sql.trim().isEmpty()) {
            try {
                log.info("原始生成的SQL: {}", sql);

                // 1. 检查并解码Unicode转义字符
                if (containsUnicodeEscape(sql)) {
                    log.warn("检测到SQL中包含Unicode转义字符，正在解码");
                    sql = decodeUnicodeInSql(sql);
                    log.info("解码Unicode后的SQL: {}", sql);
                }

                // 2. 清理SQL：移除对music表的错误user_id条件
                sql = cleanSqlOfInvalidUserId(sql);
                log.info("清理错误条件后的SQL: {}", sql);

                // 3. 验证SQL安全性并执行查询
                if (isSimpleSelectQuery(sql)) {
                    Map<String, Object> conditions = parseConditionsFromSQL(sql);
                    log.info("解析出的查询条件: {}", conditions);

                    if (!conditions.isEmpty()) {
                        List<Map<String, Object>> queryData = agentMapper.safeQuery(conditions);
                        result.setData(queryData);
                        log.info("查询到数据条数: {}", queryData.size());
                    } else {
                        result.setError("无法安全执行该查询 - 未解析出有效条件");
                        log.warn("无法解析SQL条件: {}", sql);
                    }
                } else {
                    result.setError("仅支持安全的SELECT查询");
                    log.warn("不安全的SQL查询已被阻止: {}", sql);
                }
            } catch (Exception e) {
                String errorMsg = "数据库查询失败: " + e.getMessage();
                result.setError(errorMsg);
                log.error("数据库查询异常", e);
            }
        } else {
            log.info("无需生成SQL查询，用户查询可能不需要数据库数据");
        }

        log.info("SQL处理结果 - 原始描述: {}, 是否有错误: {}, 数据条数: {}",
                result.getOriginalPrompt(),
                result.getError() != null,
                result.getData().size());

        return result;
    }

    /**
     * 生成最终答案
     */
    private static Flux<String> generateFinalAnswer(
            AgentParamsEntity params,
            ChatClient chatClient,
            String systemPromptContent,
            SQLResult sqlResult
    ) {
        return Flux.create(sink -> {
            try {
                // 构建增强的系统提示
                String enhancedSystemPrompt = buildEnhancedSystemPrompt(systemPromptContent, params, sqlResult);

                // 构建用户消息（只包含用户原始查询）
                String userMessageContent = params.getPrompt();

                log.debug("增强后的系统提示长度: {}", enhancedSystemPrompt.length());
                log.debug("用户消息内容: {}", userMessageContent);

                // 创建系统消息和用户消息
                SystemMessage systemMessage = new SystemMessage(enhancedSystemPrompt);
                UserMessage userMessage = new UserMessage(userMessageContent);

                Prompt finalPrompt = new Prompt(List.of(systemMessage, userMessage));

                log.info("开始生成最终回答...");

                // 使用stream()获取流式响应
                chatClient.prompt(finalPrompt)
                        .advisors(advisorSpec -> {
                            advisorSpec.param("CHAT_ID", params.getChatId());
                            advisorSpec.param("USER_ID", params.getUserId());
                            if (params.getShowThink() != null && params.getShowThink()) {
                                advisorSpec.advisors(new SimpleLoggerAdvisor());
                            }
                        })
                        .stream()
                        .content()
                        .subscribe(
                                chunk -> {
                                    log.debug("发送响应分块: {}", chunk);
                                    sink.next(chunk);
                                },
                                error -> {
                                    log.error("生成回答时发生错误", error);
                                    sink.error(error);
                                },
                                () -> {
                                    log.info("回答生成完成");
                                    sink.complete();
                                }
                        );

            } catch (Exception e) {
                log.error("生成最终回答失败", e);
                sink.error(e);
            }
        });
    }

    /**
     * 构建增强的系统提示
     */
    private static String buildEnhancedSystemPrompt(String basePrompt, AgentParamsEntity params, SQLResult sqlResult) {
        StringBuilder enhanced = new StringBuilder();

        // 基本系统提示
        String cleanedBasePrompt = basePrompt;
        if (cleanedBasePrompt.contains("{userId}") || cleanedBasePrompt.contains("{queryType}")) {
            cleanedBasePrompt = cleanedBasePrompt
                    .replace("{userId}", params.getUserId() != null ? params.getUserId() : "未知用户")
                    .replace("{queryType}", params.getType() != null ? params.getType() : "音乐查询");
        }

        // 添加思考模式指令
        String thinkModeInstruction = "";
        if (params.getShowThink() != null && params.getShowThink()) {
            thinkModeInstruction = "请展示详细的思考过程，包括如何分析数据、如何组织回答等。";
        } else {
            thinkModeInstruction = "直接给出最终回答，不需要展示思考过程。";
        }

        if (cleanedBasePrompt.contains("{思考模式指令}")) {
            cleanedBasePrompt = cleanedBasePrompt.replace("{思考模式指令}", thinkModeInstruction);
        }

        enhanced.append(cleanedBasePrompt);
        enhanced.append("\n\n## 数据库查询结果：");

        if (sqlResult.getError() != null) {
            enhanced.append("\n查询失败: ").append(sqlResult.getError());
        } else if (sqlResult.getData().isEmpty() && sqlResult.getSql() != null && !sqlResult.getSql().isEmpty()) {
            enhanced.append("\n查询成功，但未找到相关数据");
            enhanced.append("\n生成的SQL: ").append(sqlResult.getSql());
        } else if (!sqlResult.getData().isEmpty()) {
            enhanced.append("\n查询成功，找到 ").append(sqlResult.getData().size()).append(" 条相关记录");

            // 展示部分数据作为上下文
            int displayCount = Math.min(sqlResult.getData().size(), 50);
            enhanced.append("\n\n以下是查询到的数据（显示前").append(displayCount).append("条）:");

            for (int i = 0; i < displayCount; i++) {
                Map<String, Object> record = sqlResult.getData().get(i);
                enhanced.append("\n\n记录 ").append(i + 1).append(":");

                // 格式化显示关键字段
                appendFormattedField(record, "song_name", "歌曲名称", enhanced);
                appendFormattedField(record, "author_name", "歌手", enhanced);
                appendFormattedField(record, "album_name", "专辑", enhanced);
                appendFormattedField(record, "language", "语言", enhanced);
                appendFormattedField(record, "publish_date", "发布日期", enhanced);
                appendFormattedField(record, "is_hot", "是否热门", enhanced);
                appendFormattedField(record, "label", "标签", enhanced);
                appendFormattedField(record, "lyrics", "歌词片段", enhanced);
            }

            if (sqlResult.getData().size() > displayCount) {
                enhanced.append("\n\n... 还有").append(sqlResult.getData().size() - displayCount).append("条记录未完全显示。");
            }
        } else {
            enhanced.append("\n本次查询不需要数据库数据");
        }

        enhanced.append("\n\n## 回答要求：");
        enhanced.append("\n1. 基于以上数据回答用户的问题");
        enhanced.append("\n2. 如果数据充足，请提供详细的音乐信息");
        enhanced.append("\n3. 如果数据不足，请如实告知用户");
        enhanced.append("\n4. 使用友好、专业的语气");
        enhanced.append("\n5. 语言: ").append("zh".equals(params.getLanguage()) ? "中文" : "英文");

        // 转义大括号，防止被解析为模板占位符
        String result = enhanced.toString();
        return escapeTemplateBrackets(result);
    }

    private static void appendFormattedField(Map<String, Object> record, String key, String label, StringBuilder sb) {
        if (record.containsKey(key)) {
            Object value = record.get(key);
            if (value != null) {
                sb.append("\n  - ").append(label).append(": ").append(value.toString());
            }
        }
    }

    private static void appendFormattedField(Map<String, Object> record, String key, String label, StringBuilder sb, int maxLength) {
        if (record.containsKey(key)) {
            Object value = record.get(key);
            if (value != null) {
                String text = value.toString();
                if (text.length() > maxLength) {
                    text = text.substring(0, maxLength) + "...";
                }
                sb.append("\n  - ").append(label).append(": ").append(text);
            }
        }
    }

    private static boolean isSimpleSelectQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }

        String lower = sql.toLowerCase().trim();
        return lower.startsWith("select")
                && !lower.contains("insert")
                && !lower.contains("update")
                && !lower.contains("delete")
                && !lower.contains("drop")
                && !lower.contains("truncate")
                && !lower.contains("alter")
                && !lower.contains("create")
                && !lower.contains("exec")
                && !lower.contains("call")
                && !lower.contains("union");
    }

    private static Map<String, Object> parseConditionsFromSQL(String sql) {
        Map<String, Object> conditions = new HashMap<>();

        if (sql == null || sql.trim().isEmpty()) {
            return conditions;
        }

        // 先解码SQL中的Unicode字符
        String decodedSql = decodeUnicodeInSql(sql);

        // 支持的字段映射（数据库字段名 -> Java属性名）
        Map<String, String> fieldMapping = Map.of(
                "song_name", "songName",
                "author_name", "authorName",
                "album_name", "albumName",
                "language", "language",
                "label", "label"
        );

        // 处理 LIKE 条件 - 使用解码后的SQL
        for (String dbField : fieldMapping.keySet()) {
            // 匹配 LIKE '%值%' 的模式
            String patternStr = dbField + "\\s+LIKE\\s+'%([^']*)%'";
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(decodedSql);

            if (matcher.find()) {
                String value = matcher.group(1);
                // 进一步解码值中的Unicode（如果有）
                if (containsUnicodeEscape(value)) {
                    value = decodeUnicodeString(value);
                }
                conditions.put(fieldMapping.get(dbField), value);
                log.debug("解析到LIKE条件 - 字段: {}, 值: {}", dbField, value);
            }
        }

        // 处理等值条件 (e.g., is_hot = 1)
        if (decodedSql.matches("(?i).*\\bis_hot\\s*=\\s*\\d+.*")) {
            Pattern pattern = Pattern.compile("(?i)is_hot\\s*=\\s*(\\d+)");
            Matcher matcher = pattern.matcher(decodedSql);
            if (matcher.find()) {
                try {
                    int isHot = Integer.parseInt(matcher.group(1));
                    conditions.put("isHot", isHot);
                    log.debug("解析到is_hot条件: {}", isHot);
                } catch (NumberFormatException e) {
                    log.warn("无法解析is_hot的值: {}", matcher.group(1));
                }
            }
        }

        // 处理 language = 'xxx'
        Pattern langPattern = Pattern.compile("(?i)language\\s*=\\s*'([^']+)'");
        Matcher langMatcher = langPattern.matcher(decodedSql);
        if (langMatcher.find()) {
            String language = langMatcher.group(1);
            // 解码可能的Unicode
            if (containsUnicodeEscape(language)) {
                language = decodeUnicodeString(language);
            }
            conditions.put("language", language);
            log.debug("解析到language条件: {}", language);
        }

        return conditions;
    }

    private static String formatErrorMessage(Throwable error, String language) {
        String message = error.getMessage();
        if (message == null) {
            message = "未知错误";
        }

        if ("en".equals(language)) {
            return "Sorry, an error occurred while processing your request: " + message;
        } else {
            return "抱歉，处理您的请求时发生错误：" + message;
        }
    }

    /**
     * 转义模板字符串中的大括号
     */
    private static String escapeTemplateBrackets(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        if (input.contains("{{") || input.contains("}}")) {
            return input; // 已经转义过了
        }

        return input.replace("{", "{{").replace("}", "}}");
    }

    /**
     * 检查字符串是否包含Unicode转义
     */
    private static boolean containsUnicodeEscape(String str) {
        return str != null && (str.contains("\\u") || str.contains("\\\\u"));
    }

    /**
     * 解码SQL中的Unicode转义字符
     */
    private static String decodeUnicodeInSql(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }

        // 处理双反斜杠的情况
        String normalized = sql.replace("\\\\u", "\\u");

        Pattern unicodePattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher matcher = unicodePattern.matcher(normalized);

        StringBuffer decoded = new StringBuffer();
        while (matcher.find()) {
            try {
                String hex = matcher.group(1);
                int codePoint = Integer.parseInt(hex, 16);
                char[] chars = Character.toChars(codePoint);
                matcher.appendReplacement(decoded, new String(chars));
            } catch (Exception e) {
                matcher.appendReplacement(decoded, matcher.group());
            }
        }
        matcher.appendTail(decoded);

        String decodedSql = decoded.toString();

        // 特别处理LIKE语句中的内容
        Pattern likePattern = Pattern.compile("LIKE\\s+'([^']*)'", Pattern.CASE_INSENSITIVE);
        matcher = likePattern.matcher(decodedSql);

        StringBuffer finalSql = new StringBuffer();
        while (matcher.find()) {
            String likeContent = matcher.group(1);
            if (containsUnicodeEscape(likeContent)) {
                String decodedLike = decodeUnicodeString(likeContent);
                matcher.appendReplacement(finalSql, "LIKE '" + decodedLike + "'");
            } else {
                matcher.appendReplacement(finalSql, matcher.group());
            }
        }
        matcher.appendTail(finalSql);

        return finalSql.toString();
    }

    /**
     * 解码字符串中的Unicode转义字符
     */
    private static String decodeUnicodeString(String str) {
        if (str == null || !containsUnicodeEscape(str)) {
            return str;
        }

        String normalized = str.replace("\\\\u", "\\u");
        Pattern pattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher matcher = pattern.matcher(normalized);

        StringBuffer decoded = new StringBuffer();
        while (matcher.find()) {
            try {
                String hex = matcher.group(1);
                int codePoint = Integer.parseInt(hex, 16);
                char[] chars = Character.toChars(codePoint);
                matcher.appendReplacement(decoded, new String(chars));
            } catch (Exception e) {
                matcher.appendReplacement(decoded, matcher.group());
            }
        }
        matcher.appendTail(decoded);

        return decoded.toString();
    }

    /**
     * 清理SQL，移除对music表的不正确user_id条件
     */
    private static String cleanSqlOfInvalidUserId(String sql) {
        String cleaned = sql.trim();
        String lowerSql = cleaned.toLowerCase();

        if (lowerSql.contains("from music") && lowerSql.contains("user_id")) {
            log.warn("检测到SQL中包含错误的user_id条件，正在进行清理");

            cleaned = cleaned.replaceAll("(?i)\\s+AND\\s+user_id\\s*=\\s*'[^']*'", "");
            cleaned = cleaned.replaceAll("(?i)\\s+AND\\s+user_id\\s*=\\s*\"[^\"]*\"", "");

            if (cleaned.matches("(?i).*WHERE\\s+user_id\\s*=.*")) {
                cleaned = cleaned.replaceAll("(?i)WHERE\\s+user_id\\s*=\\s*'[^']*'\\s*", "WHERE 1=1 ");
                cleaned = cleaned.replaceAll("(?i)WHERE\\s+user_id\\s*=\\s*\"[^\"]*\"\\s*", "WHERE 1=1 ");
            }

            cleaned = cleaned.replaceAll("(?i)\\s+WHERE\\s+1=1\\s+WHERE", " WHERE");
            cleaned = cleaned.replaceAll("(?i)\\s+WHERE\\s+WHERE", " WHERE");

            cleaned = cleaned.trim();

            if (cleaned.endsWith("WHERE 1=1")) {
                cleaned = cleaned.substring(0, cleaned.length() - 9).trim();
            }

            log.info("清理错误user_id条件后的SQL: {}", cleaned);
        }

        return cleaned;
    }

    private static class SQLResult {
        private String originalPrompt;
        private String sql;
        private List<Map<String, Object>> data;
        private String error;

        public String getOriginalPrompt() { return originalPrompt; }
        public void setOriginalPrompt(String originalPrompt) { this.originalPrompt = originalPrompt; }
        public String getSql() { return sql; }
        public void setSql(String sql) { this.sql = sql; }
        public List<Map<String, Object>> getData() { return data; }
        public void setData(List<Map<String, Object>> data) { this.data = data; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        @Override
        public String toString() {
            return "SQLResult{" +
                    "originalPrompt='" + originalPrompt + '\'' +
                    ", sql='" + sql + '\'' +
                    ", dataSize=" + (data != null ? data.size() : 0) +
                    ", error='" + error + '\'' +
                    '}';
        }
    }
}