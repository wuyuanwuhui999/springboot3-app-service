package com.player.ai.config;

import reactor.core.publisher.Flux;

import java.time.Duration;

public class ContentChunker {
    public static Flux<String> chunk(Flux<String> content) {
        return content.bufferTimeout(500, Duration.ofMillis(300))
                .flatMap(list -> Flux.fromIterable(list)
                        .map(chunk -> "[Chunk]" + chunk));
    }
}