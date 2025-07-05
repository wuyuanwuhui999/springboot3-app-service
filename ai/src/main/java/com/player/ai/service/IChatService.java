package com.player.ai.service;

import com.player.common.entity.ResultEntity;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

public interface IChatService {
    Flux<String> chat(String userId, String prompt, String chatId, String modelName,boolean showThink);

    ResultEntity getChatHistory(String userId,int pageNum,int pageSize);

    ResultEntity uploadDoc(MultipartFile file,String userId) throws IOException;

    Flux<String> searchDoc(String query,String chatId,String modelName);

    ResultEntity getDocList(String userId);
}
