package com.player.music.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class AssistantTools {

    @Tool("获取当前时间")
    public String currentTime() {
        return "当前时间是: " + LocalTime.now().toString();
    }
}