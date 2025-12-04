package com.player.gateway.music.uitls;

import com.player.gateway.music.entity.ChatParamsEntity;
import com.player.gateway.music.tools.MusicTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

public class ChatUtils {

    public static Flux<String> processChat(
            ChatParamsEntity chatParamsEntity,
            ChatClient chatClient,
            VectorStore vectorStore,
            String userId,
            String systemPromptContent,
            MusicTool musicTool  // Added MusicTool parameter
    ) {
        String prompt = chatParamsEntity.getPrompt();

        if ("document".equals(chatParamsEntity.getType())) {
            // 构建原始查询DSL
            String queryDsl = String.format("""
                    {
                      "query": {
                        "bool": {
                          "must": {
                            "match": {
                              "content": "%s"
                            }
                          },
                          "filter": [
                            {"term": {"metadata.user_id": "%s"}},
                            {"term": {"metadata.directory_id": "default"}}
                          ]
                        }
                      }
                    }
                    """, chatParamsEntity.getPrompt(), userId);

            List<Document> relevantDocs = vectorStore.similaritySearch(queryDsl);
            if (relevantDocs.isEmpty()) {
                return Flux.just("没有查询到相关文档").doOnNext(document -> {});
            }
            // 2. 构建上下文提示
            String context = PromptUtil.buildContext(relevantDocs);
            // 3. 构建完整提示词
            prompt = PromptUtil.buildPrompt(prompt, context);
        }

        String systemPromptTemplate = """
                {systemPrompt}
                {thinking}
                当前日期: {current_date}
            """;

        Map<String, Object> systemVariables = Map.of(
                "systemPrompt", systemPromptContent,
                "thinking", chatParamsEntity.getShowThink() ? "请详细解释你的思考过程。" : "直接给出最终答案，不要解释思考过程。",
                "current_date", LocalDate.now().toString()
        );

        SystemPromptTemplate systemPrompt = new SystemPromptTemplate(systemPromptTemplate);
        Message systemMessage = systemPrompt.createMessage(systemVariables);

        PromptTemplate userPromptTemplate = new PromptTemplate("""
                用户问题: {message}
                请用{language}回答。
            """);

        Map<String, Object> userVariables = Map.of(
                "message", prompt,
                "language", "zh".equals(chatParamsEntity.getLanguage()) ? "中文" : "英文"
        );

        Message userMessage = userPromptTemplate.createMessage(userVariables);

        Prompt finalPrompt = new Prompt(List.of(systemMessage, userMessage));

        ChatClient.ChatClientRequestSpec advisors = chatClient
                .prompt(finalPrompt)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatParamsEntity.getChatId()));

        // Add tools configuration if type is "type"
        if ("type".equals(chatParamsEntity.getType())) {
            advisors.tools(musicTool);
        }

        return advisors.stream().content();
    }
}