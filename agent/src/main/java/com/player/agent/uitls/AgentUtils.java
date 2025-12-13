package com.player.agent.uitls;

import com.player.agent.entity.AgentParamsEntity;
import com.player.agent.tool.AgentTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class AgentUtils {

    public static Flux<String> processChat(
            AgentParamsEntity agentParamsEntity,
            ChatClient chatClient,
            String systemPromptContent,
            AgentTool agentTool  // Added AgentTool parameter
    ) {
        String prompt = agentParamsEntity.getPrompt();

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

        advisors.tools(agentTool);

        return advisors.stream().content();
    }
}