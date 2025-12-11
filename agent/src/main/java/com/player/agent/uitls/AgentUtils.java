package com.player.agent.uitls;

import com.player.agent.entity.AgentParamsEntity;
import com.player.agent.tool.AgentTool;
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


public class AgentUtils {

    public static Flux<String> processChat(
            AgentParamsEntity agentParamsEntity,
            ChatClient chatClient,
            VectorStore vectorStore,
            String userId,
            String systemPromptContent,
            AgentTool agentTool  // Added AgentTool parameter
    ) {
        String prompt = agentParamsEntity.getPrompt();

        if ("document".equals(agentParamsEntity.getType())) {
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
                    """, agentParamsEntity.getPrompt(), userId);

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
                "thinking", agentParamsEntity.getShowThink() ? "请详细解释你的思考过程。" : "直接给出最终答案，不要解释思考过程。",
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
                "language", "zh".equals(agentParamsEntity.getLanguage()) ? "中文" : "英文"
        );

        Message userMessage = userPromptTemplate.createMessage(userVariables);

        Prompt finalPrompt = new Prompt(List.of(systemMessage, userMessage));

        ChatClient.ChatClientRequestSpec advisors = chatClient
                .prompt(finalPrompt)
                .advisors(advisorSpec -> advisorSpec.param("AGENT", agentParamsEntity.getChatId()));

        if ("type".equals(agentParamsEntity.getType())) {
            advisors.tools(agentTool);
        }

        return advisors.stream().content();
    }
}