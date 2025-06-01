package com.player.music.service;

import com.player.common.entity.ResultEntity;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IChatService {
    Flux<String> chat(String userId, String prompt, String chatId, List<MultipartFile> files);

    String upload(List<MultipartFile>files);

    ResultEntity getChatHistory(String userId,int pageNum,int pageSize);
}
