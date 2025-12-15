package com.player.agent.uitls;

import com.player.agent.entity.AgentParamsEntity;
import com.player.agent.tool.AgentTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.*;

@Slf4j
public class AgentUtils {

    public static Flux<String> processChat(
            AgentParamsEntity agentParamsEntity,
            ChatClient chatClient,
            String systemPromptContent,
            AgentTool agentTool
    ) {
        try {
            String prompt = agentParamsEntity.getPrompt();
            String userId = agentParamsEntity.getUserId();
            String chatId = agentParamsEntity.getChatId();

            log.info("Processing chat - User: {}, ChatId: {}, Prompt: {}", userId, chatId, prompt);

            // 构建增强的系统提示词
            String enhancedSystemPrompt = enhanceSystemPrompt(systemPromptContent, agentParamsEntity);

            // 创建系统消息
            Message systemMessage = createSystemMessage(enhancedSystemPrompt, agentParamsEntity);

            // 创建用户消息（包含上下文）
            Message userMessage = createUserMessage(prompt, agentParamsEntity);

            // 构建完整的提示
            Prompt finalPrompt = new Prompt(List.of(systemMessage, userMessage));

            // 创建ChatClient请求
            ChatClient.ChatClientRequestSpec requestSpec = chatClient
                    .prompt(finalPrompt)
                    .advisors(advisorSpec -> {
                        // 添加聊天ID作为上下文
                        advisorSpec.param("CHAT_ID", chatId);
                        advisorSpec.param("USER_ID", userId);
                        advisorSpec.param("MODEL_ID", agentParamsEntity.getModelId());

                        // 添加日志顾问（可选）
                        if (log.isDebugEnabled()) {
                            advisorSpec.add(new SimpleLoggerAdvisor());
                        }
                    });

            // 添加工具
            if (agentTool != null) {
                requestSpec = requestSpec.tools(agentTool);
                log.debug("Agent tools added to chat request");
            }

            // 流式响应处理
            return requestSpec.stream()
                    .content()
                    .doOnNext(content -> {
                        if (log.isDebugEnabled()) {
                            log.debug("Stream response part: {}", content);
                        }
                    })
                    .doOnError(error -> {
                        log.error("Error in chat stream processing", error);
                    })
                    .doOnComplete(() -> {
                        log.info("Chat stream completed for user: {}, chatId: {}", userId, chatId);
                    })
                    .onErrorResume(error -> {
                        // 错误恢复：返回友好的错误信息
                        String errorMessage = formatErrorMessage(error, agentParamsEntity.getLanguage());
                        return Flux.just(errorMessage);
                    });

        } catch (Exception e) {
            log.error("Error in processChat", e);
            return Flux.error(e);
        }
    }

    /**
     * 增强系统提示词，添加更多上下文信息
     */
    private static String enhanceSystemPrompt(String basePrompt, AgentParamsEntity params) {
        StringBuilder enhanced = new StringBuilder(basePrompt);

        // 添加当前日期和时间信息
        enhanced.append("\n\n当前日期: ").append(LocalDate.now());
        enhanced.append("\n当前时间: ").append(new Date());

        // 添加用户上下文信息
        enhanced.append("\n用户ID: ").append(params.getUserId() != null ? params.getUserId() : "未登录用户");
        enhanced.append("\n聊天ID: ").append(params.getChatId());

        // 添加思考模式指示
        if (params.getShowThink()) {
            enhanced.append("\n思考模式: 启用 - 请展示详细的思考过程");
        } else {
            enhanced.append("\n思考模式: 禁用 - 直接给出最终答案");
        }

        // 添加语言偏好
        enhanced.append("\n回答语言: ").append("zh".equals(params.getLanguage()) ? "中文" : "英文");

        // 添加对话历史提示
        enhanced.append("\n\n对话历史管理:");
        enhanced.append("\n- 如果需要历史信息，请明确说明");
        enhanced.append("\n- 对于连续对话，请保持上下文连贯");

        return enhanced.toString();
    }

    /**
     * 创建系统消息
     */
    private static Message createSystemMessage(String systemPromptContent, AgentParamsEntity params) {
        String systemPromptTemplate = """
                {systemPrompt}
                
                思考模式设置: {thinkingMode}
                对话语言: {language}
                当前上下文: {context}
                """;

        Map<String, Object> systemVariables = Map.of(
                "systemPrompt", systemPromptContent,
                "thinkingMode", params.getShowThink() ?
                        "请详细解释你的思考过程，包括：1) 理解用户意图 2) 选择工具的原因 3) 处理步骤" :
                        "直接给出最终答案，不要解释思考过程。",
                "language", "zh".equals(params.getLanguage()) ? "使用中文回答" : "Use English to answer",
                "context", buildContextInfo(params)
        );

        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(systemPromptTemplate);
        return systemPrompt.createMessage(systemVariables);
    }

    /**
     * 创建用户消息
     */
    private static Message createUserMessage(String userPrompt, AgentParamsEntity params) {
        String userPromptTemplate = """
                用户查询: {message}
                
                附加信息:
                - 查询类型: {type}
                - 语言偏好: {languagePreference}
                - 是否需要工具调用: {needTools}
                """;

        Map<String, Object> userVariables = Map.of(
                "message", userPrompt,
                "type", params.getType() != null ? params.getType() : "通用查询",
                "languagePreference", "zh".equals(params.getLanguage()) ? "中文" : "英文",
                "needTools", shouldUseTools(userPrompt) ? "是，请使用合适的工具" : "否，直接回答"
        );

        PromptTemplate userTemplate = new PromptTemplate(userPromptTemplate);
        return userTemplate.createMessage(userVariables);
    }

    /**
     * 构建上下文信息
     */
    private static String buildContextInfo(AgentParamsEntity params) {
        StringBuilder context = new StringBuilder();

        // 基本上下文
        context.append("用户正在进行音乐相关的查询");

        // 根据类型添加特定上下文
        if (params.getType() != null) {
            switch (params.getType().toLowerCase()) {
                case "search":
                    context.append(" - 搜索特定音乐");
                    break;
                case "recommend":
                    context.append(" - 请求音乐推荐");
                    break;
                case "history":
                    context.append(" - 查询播放历史");
                    break;
                case "favorite":
                    context.append(" - 管理收藏");
                    break;
                default:
                    context.append(" - 通用音乐助手功能");
            }
        }

        // 添加用户偏好提示
        context.append("\n用户偏好: ");
        context.append("zh".equals(params.getLanguage()) ? "中文界面" : "英文界面");

        return context.toString();
    }

    /**
     * 判断是否需要使用工具
     */
    private static boolean shouldUseTools(String userPrompt) {
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            return false;
        }

        String lowerPrompt = userPrompt.toLowerCase();

        // 需要工具的查询类型
        String[] toolKeywords = {
                "查询", "搜索", "查找", "推荐", "收藏",
                "历史", "记录", "喜欢", "点赞", "歌手",
                "歌曲", "专辑", "播放", "音乐", "歌单",
                "最新的", "热门的", "流行的", "find", "search",
                "query", "recommend", "favorite", "history", "like"
        };

        for (String keyword : toolKeywords) {
            if (lowerPrompt.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 格式化错误信息
     */
    private static String formatErrorMessage(Throwable error, String language) {
        if ("en".equals(language)) {
            return "Sorry, an error occurred while processing your request: " +
                    error.getMessage() + ". Please try again later.";
        } else {
            return "抱歉，处理您的请求时出现错误：" +
                    error.getMessage() + "。请稍后重试。";
        }
    }

    /**
     * 格式化音乐数据为友好展示
     */
    public static String formatMusicData(List<Map<String, Object>> musicData, String title) {
        if (musicData == null || musicData.isEmpty()) {
            return "🎵 没有找到相关的音乐数据。";
        }

        StringBuilder result = new StringBuilder();
        result.append("🎵 ").append(title).append("（共").append(musicData.size()).append("首）\n\n");

        for (int i = 0; i < Math.min(musicData.size(), 15); i++) {
            Map<String, Object> music = musicData.get(i);
            result.append(i + 1).append(". ");

            // 歌手
            Object author = music.get("authorName");
            if (author != null) {
                result.append(author).append(" - ");
            }

            // 歌曲名
            Object songName = music.get("songName");
            if (songName != null) {
                result.append(songName);
            }

            // 专辑
            Object album = music.get("albumName");
            if (album != null && !album.toString().isEmpty()) {
                result.append("（专辑：").append(album).append("）");
            }

            // 语言
            Object language = music.get("language");
            if (language != null && !language.toString().isEmpty()) {
                result.append(" [").append(language).append("]");
            }

            // 标签
            Object label = music.get("label");
            if (label != null && !label.toString().isEmpty()) {
                result.append(" · ").append(label);
            }

            result.append("\n");
        }

        if (musicData.size() > 15) {
            result.append("\n... 还有").append(musicData.size() - 15).append("首歌曲");
        }

        return result.toString();
    }

    /**
     * 提取查询意图
     */
    public static Map<String, String> extractQueryIntent(String userQuery) {
        Map<String, String> intent = new HashMap<>();
        String lowerQuery = userQuery.toLowerCase();

        // 提取歌手信息
        String[] commonAuthors = {"周杰伦", "林俊杰", "邓紫棋", "陈奕迅", "薛之谦",
                "王菲", "张学友", "刘德华", "孙燕姿", "蔡依林"};
        for (String author : commonAuthors) {
            if (lowerQuery.contains(author.toLowerCase())) {
                intent.put("author", author);
                break;
            }
        }

        // 提取歌曲类型
        if (lowerQuery.contains("流行")) {
            intent.put("genre", "流行");
        } else if (lowerQuery.contains("摇滚")) {
            intent.put("genre", "摇滚");
        } else if (lowerQuery.contains("古典")) {
            intent.put("genre", "古典");
        } else if (lowerQuery.contains("民谣")) {
            intent.put("genre", "民谣");
        }

        // 提取语言
        if (lowerQuery.contains("中文") || lowerQuery.contains("国语")) {
            intent.put("language", "中文");
        } else if (lowerQuery.contains("英文") || lowerQuery.contains("英语")) {
            intent.put("language", "英文");
        } else if (lowerQuery.contains("日语") || lowerQuery.contains("日文")) {
            intent.put("language", "日语");
        }

        // 判断查询类型
        if (lowerQuery.contains("收藏") || lowerQuery.contains("喜欢")) {
            intent.put("type", "favorite");
        } else if (lowerQuery.contains("历史") || lowerQuery.contains("听过")) {
            intent.put("type", "history");
        } else if (lowerQuery.contains("推荐")) {
            intent.put("type", "recommend");
        } else if (lowerQuery.contains("搜索") || lowerQuery.contains("查找")) {
            intent.put("type", "search");
        }

        return intent;
    }

    /**
     * 构建工具调用参数
     */
    public static Map<String, Object> buildToolParams(String userQuery, String userId) {
        Map<String, Object> params = new HashMap<>();
        Map<String, String> intent = extractQueryIntent(userQuery);

        // 基本参数
        params.put("userId", userId);
        params.put("pageNum", 1);
        params.put("pageSize", 20);

        // 根据意图设置参数
        if (intent.containsKey("author")) {
            params.put("authorName", intent.get("author"));
        }
        if (intent.containsKey("genre")) {
            params.put("label", intent.get("genre"));
        }
        if (intent.containsKey("language")) {
            params.put("language", intent.get("language"));
        }

        return params;
    }

    /**
     * 验证查询参数
     */
    public static boolean validateQueryParams(Map<String, Object> params) {
        try {
            // 检查必要参数
            if (!params.containsKey("userId") || params.get("userId") == null) {
                return false;
            }

            // 验证分页参数
            if (params.containsKey("pageNum")) {
                int pageNum = Integer.parseInt(params.get("pageNum").toString());
                if (pageNum < 1) return false;
            }

            if (params.containsKey("pageSize")) {
                int pageSize = Integer.parseInt(params.get("pageSize").toString());
                if (pageSize < 1 || pageSize > 100) return false;
            }

            return true;
        } catch (Exception e) {
            log.error("参数验证失败", e);
            return false;
        }
    }

    /**
     * 生成响应摘要
     */
    public static String generateResponseSummary(String response, int maxLength) {
        if (response == null || response.length() <= maxLength) {
            return response;
        }

        // 截断并添加省略号
        String summary = response.substring(0, maxLength - 3) + "...";

        // 尝试在句子边界处截断
        int lastPeriod = summary.lastIndexOf('.');
        int lastExclamation = summary.lastIndexOf('!');
        int lastQuestion = summary.lastIndexOf('?');
        int lastNewline = summary.lastIndexOf('\n');

        int lastBreak = Math.max(Math.max(lastPeriod, lastExclamation),
                Math.max(lastQuestion, lastNewline));

        if (lastBreak > maxLength / 2) {
            summary = response.substring(0, lastBreak + 1);
        }

        return summary;
    }
}