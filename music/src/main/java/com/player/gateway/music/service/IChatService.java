package com.player.gateway.music.service;

import com.player.gateway.common.entity.ResultEntity;
import com.player.gateway.music.entity.ChatParamsEntity;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;

public interface IChatService {
    Flux<String> chat(String userId, ChatParamsEntity chatParamsEntity);

    ResultEntity getChatHistory(String userId,int pageNum,int pageSize);

    ResultEntity getModelList();

    ResultEntity uploadDoc(MultipartFile file,String userId) throws IOException;

    ResultEntity getDocList(String userId);

    ResultEntity deleteDoc(String docId, String userId);
}
