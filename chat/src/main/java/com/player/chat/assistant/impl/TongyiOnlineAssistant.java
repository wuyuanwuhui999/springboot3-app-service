package com.player.chat.assistant.impl;

import com.player.chat.assistant.OnlineAssistant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class TongyiOnlineAssistant implements OnlineAssistant {

    @Override
    public Flux<String> chat(String memoryId, String prompt, String language, String apiKey, String baseUrl) {
        // 实现通义千问在线API调用
        return Flux.create(sink -> {
            try {
                // 这里实现通义千问API的调用逻辑
                log.info("调用通义千问在线模型: baseUrl={}, memoryId={}", baseUrl, memoryId);

                // 模拟实现 - 实际需要调用真实的API
                String response = "这是通义千问在线模型的响应";
                sink.next(response);
                sink.complete();

            } catch (Exception e) {
                log.error("通义千问在线模型调用失败", e);
                sink.error(e);
            }
        });
    }
}