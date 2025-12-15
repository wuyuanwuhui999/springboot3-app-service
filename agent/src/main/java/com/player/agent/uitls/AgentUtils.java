package com.player.agent.uitls;

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

            // 步骤1：先生成SQL（如果需要）
            return generateAndExecuteSQL(agentParamsEntity, chatClient)
                    .flatMapMany(sqlResult -> {
                        // 步骤2：基于SQL结果构建最终回答
                        return buildFinalResponse(agentParamsEntity, chatClient, systemPromptContent, sqlResult);
                    })
                    .onErrorResume(error -> {
                        String errorMessage = formatErrorMessage(error, agentParamsEntity.getLanguage());
                        return Flux.just(errorMessage);
                    });
        } catch (Exception e) {
            log.error("处理聊天时发生错误", e);
            return Flux.error(e);
        }
    }

    /**
     * 生成SQL并执行查询（仅通过 safeQuery）
     */
    private static Mono<SQLResult> generateAndExecuteSQL(AgentParamsEntity params, ChatClient chatClient) {
        return Mono.fromCallable(() -> {

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

            // 创建消息时使用更简单的方式
            Message systemMessage = new SystemMessage(SystemtConstants.SQL_GENERATOR_PROMPT);
            Message userMessageObj = new UserMessage(userMessage);

            Prompt prompt = new Prompt(List.of(systemMessage, userMessageObj));

            log.info("发送SQL生成请求 - 查询类型: {}, 用户查询: {}", params.getType(), params.getPrompt());

            String sqlResponse = chatClient.prompt(prompt).call().content();
            log.info("收到SQL生成响应: {}", sqlResponse);

            JsonNode jsonNode = objectMapper.readTree(sqlResponse);
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
        });
    }

    private static Flux<String> buildFinalResponse(
            AgentParamsEntity params,
            ChatClient chatClient,
            String systemPromptContent,
            SQLResult sqlResult
    ) {
        return Flux.create(sink -> {
            try {
                String enhancedSystemPrompt = enhanceSystemPrompt(systemPromptContent, params, sqlResult);
                String userMessageContent = buildUserMessageWithResults(params.getPrompt(), sqlResult);

                log.debug("增强后的系统提示长度: {}", enhancedSystemPrompt.length());
                log.debug("用户消息内容长度: {}", userMessageContent.length());

                // 创建系统消息 - 使用直接构造方式
                SystemMessage systemMessage = new SystemMessage(enhancedSystemPrompt);

                // 创建用户消息 - 使用直接构造方式
                UserMessage userMessage = new UserMessage(userMessageContent);

                Prompt finalPrompt = new Prompt(List.of(systemMessage, userMessage));

                chatClient.prompt(finalPrompt)
                        .advisors(advisorSpec -> {
                            advisorSpec.param("CHAT_ID", params.getChatId());
                            advisorSpec.param("USER_ID", params.getUserId());
                            if (params.getShowThink()) {
                                advisorSpec.advisors(new SimpleLoggerAdvisor());
                            }
                        })
                        .stream()
                        .content()
                        .subscribe(
                                sink::next,
                                sink::error,
                                sink::complete
                        );
            } catch (Exception e) {
                log.error("构建最终响应失败", e);
                sink.error(e);
            }
        });
    }

    private static String enhanceSystemPrompt(String basePrompt, AgentParamsEntity params, SQLResult sqlResult) {
        StringBuilder enhanced = new StringBuilder();

        // 清理basePrompt中的占位符
        String cleanedBasePrompt = basePrompt;
        if (cleanedBasePrompt.contains("{userId}") || cleanedBasePrompt.contains("{queryType}")) {
            // 替换可能的占位符
            cleanedBasePrompt = cleanedBasePrompt
                    .replace("{userId}", params.getUserId() != null ? params.getUserId() : "未知用户")
                    .replace("{queryType}", params.getType() != null ? params.getType() : "音乐查询");
        }

        enhanced.append(cleanedBasePrompt);
        enhanced.append("\n\n数据库查询结果:");

        if (sqlResult.getError() != null) {
            enhanced.append("\n查询失败: ").append(sqlResult.getError());
        } else if (sqlResult.getData().isEmpty() && sqlResult.getSql() != null && !sqlResult.getSql().isEmpty()) {
            enhanced.append("\n查询成功，但未找到相关数据");
        } else if (!sqlResult.getData().isEmpty()) {
            enhanced.append("\n查询成功，找到").append(sqlResult.getData().size()).append("条记录");

            // 显示前50条数据，确保数据被展示出来
            int displayCount = Math.min(sqlResult.getData().size(), 50);
            enhanced.append("\n详细数据（显示前").append(displayCount).append("条）:");

            for (int i = 0; i < displayCount; i++) {
                Map<String, Object> record = sqlResult.getData().get(i);
                enhanced.append("\n记录").append(i + 1).append(": ");
                appendField(record, "song_name", "歌曲", enhanced);
                appendField(record, "author_name", "歌手", enhanced);
                appendField(record, "album_name", "专辑", enhanced);
                appendField(record, "language", "语言", enhanced);
                appendField(record, "label", "标签", enhanced);
                appendField(record, "duration", "时长", enhanced);
                appendField(record, "is_hot", "热门", enhanced);
            }

            if (sqlResult.getData().size() > 50) {
                enhanced.append("\n... 还有").append(sqlResult.getData().size() - 50).append("条记录未显示");
            }
        } else {
            enhanced.append("\n无需查询数据库");
        }

        enhanced.append("\n\n用户原始查询: ").append(sqlResult.getOriginalPrompt());

        if (params.getShowThink()) {
            enhanced.append("\n思考模式: 启用 - 请展示详细的思考过程");
        }

        enhanced.append("\n回答语言: ").append("zh".equals(params.getLanguage()) ? "中文" : "英文");

        // 处理大括号，防止被误解析为模板占位符
        String result = enhanced.toString();
        result = escapeTemplateBrackets(result);

        return result;
    }

    private static void appendField(Map<String, Object> record, String key, String label, StringBuilder sb) {
        if (record.containsKey(key)) {
            Object val = record.get(key);
            if (val != null) {
                String currentText = sb.toString();
                // 检查当前是否已经有内容，如果没有才添加分隔符
                if (!currentText.trim().endsWith(":") && !currentText.trim().endsWith("：")) {
                    sb.append("; ");
                }
                sb.append(label).append(": ").append(val.toString());
            }
        }
    }

    private static String buildUserMessageWithResults(String userPrompt, SQLResult sqlResult) {
        StringBuilder message = new StringBuilder("用户查询: ").append(userPrompt);
        if (sqlResult.getSql() != null && !sqlResult.getSql().isEmpty()) {
            message.append("\n\n已生成的SQL: ").append(sqlResult.getSql());
        }
        if (!sqlResult.getData().isEmpty()) {
            message.append("\n\n查询到的数据（共").append(sqlResult.getData().size()).append("条）:");

            int displayCount = Math.min(sqlResult.getData().size(), 50);
            for (int i = 0; i < displayCount; i++) {
                Map<String, Object> record = sqlResult.getData().get(i);
                message.append("\n记录").append(i + 1).append(": ");

                boolean hasContent = false;
                if (record.containsKey("song_name")) {
                    message.append("歌曲《").append(record.get("song_name").toString()).append("》");
                    hasContent = true;
                }
                if (record.containsKey("author_name")) {
                    if (hasContent) message.append(" - ");
                    message.append(record.get("author_name").toString());
                    hasContent = true;
                }
                if (record.containsKey("album_name")) {
                    if (hasContent) message.append(" (专辑: ");
                    else message.append("专辑: ");
                    message.append(record.get("album_name").toString()).append(")");
                }

                // 可选：添加其他字段
                if (record.containsKey("language")) {
                    message.append(" [语言: ").append(record.get("language")).append("]");
                }
                if (record.containsKey("is_hot")) {
                    Object isHot = record.get("is_hot");
                    if (isHot != null && "1".equals(isHot.toString())) {
                        message.append(" [热门]");
                    }
                }
            }

            if (sqlResult.getData().size() > 50) {
                message.append("\n... 还有").append(sqlResult.getData().size() - 50).append("条记录");
            }
        } else if (sqlResult.getError() != null) {
            message.append("\n\n数据库查询失败: ").append(sqlResult.getError());
        }

        // 处理大括号，防止被误解析为模板占位符
        String result = message.toString();
        return escapeTemplateBrackets(result);
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
            return "Sorry, an error occurred: " + message;
        } else {
            return "抱歉，发生错误：" + message;
        }
    }

    /**
     * 转义模板字符串中的大括号
     * 将 { 转义为 {{，将 } 转义为 }}
     */
    private static String escapeTemplateBrackets(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 只转义单层的大括号，避免双重转义
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
     * 例如：将 \u5468\u5e38\u4eba 转换为 周杰伦
     */
    private static String decodeUnicodeInSql(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }

        log.debug("开始解码SQL中的Unicode - 原始SQL: {}", sql);

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
                log.debug("解码Unicode: \\u{} -> {}", hex, new String(chars));
            } catch (Exception e) {
                log.warn("解码Unicode失败: {}", matcher.group(), e);
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
            // 如果like内容还包含未解码的Unicode
            if (containsUnicodeEscape(likeContent)) {
                String decodedLike = decodeUnicodeString(likeContent);
                matcher.appendReplacement(finalSql, "LIKE '" + decodedLike + "'");
                log.debug("解码LIKE内容: {} -> {}", likeContent, decodedLike);
            } else {
                matcher.appendReplacement(finalSql, matcher.group());
            }
        }
        matcher.appendTail(finalSql);

        log.debug("解码后的SQL: {}", finalSql.toString());

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

        // 如果SQL中包含FROM music并且包含了user_id条件，清理它
        if (lowerSql.contains("from music") && lowerSql.contains("user_id")) {
            log.warn("检测到SQL中包含错误的user_id条件，正在进行清理");

            // 1. 移除 AND user_id = 'xxx' 的情况
            cleaned = cleaned.replaceAll("(?i)\\s+AND\\s+user_id\\s*=\\s*'[^']*'", "");
            cleaned = cleaned.replaceAll("(?i)\\s+AND\\s+user_id\\s*=\\s*\"[^\"]*\"", "");

            // 2. 处理 WHERE user_id = 'xxx' AND ... 的情况
            if (cleaned.matches("(?i).*WHERE\\s+user_id\\s*=.*")) {
                cleaned = cleaned.replaceAll("(?i)WHERE\\s+user_id\\s*=\\s*'[^']*'\\s*", "WHERE 1=1 ");
                cleaned = cleaned.replaceAll("(?i)WHERE\\s+user_id\\s*=\\s*\"[^\"]*\"\\s*", "WHERE 1=1 ");
            }

            // 3. 清理可能的双重WHERE
            cleaned = cleaned.replaceAll("(?i)\\s+WHERE\\s+1=1\\s+WHERE", " WHERE");
            cleaned = cleaned.replaceAll("(?i)\\s+WHERE\\s+WHERE", " WHERE");

            // 4. 移除末尾的空白
            cleaned = cleaned.trim();

            // 5. 如果只剩下WHERE 1=1，移除它
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