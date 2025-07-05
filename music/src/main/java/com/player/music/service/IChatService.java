package com.player.music.service;

import com.player.common.entity.ResultEntity;
import com.player.music.entity.FileEntity;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

public interface IChatService {
    Flux<String> chat(String userId, String prompt, String chatId,String modelName);

    String upload(List<MultipartFile>files);

    ResultEntity getChatHistory(String userId,int pageNum,int pageSize);

    ResultEntity getModelList();

    ResultEntity uploadDoc(MultipartFile file,String userId) throws IOException;

    Flux<String> searchDoc(String query,String chatId,String userId,String modelName);

    ResultEntity getDocList(String userId);
}
