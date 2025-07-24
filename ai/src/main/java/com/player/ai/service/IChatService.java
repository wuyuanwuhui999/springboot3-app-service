package com.player.ai.service;

import com.player.ai.entity.ChatParamsEntity;
import com.player.ai.entity.DirectoryEntity;
import com.player.common.entity.ResultEntity;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public interface IChatService {
    Flux<String> chat(String userId, ChatParamsEntity chatParamsEntity);

    ResultEntity getChatHistory(String userId,int pageNum,int pageSize);

    ResultEntity getModelList();

    ResultEntity uploadDoc(MultipartFile file,String userId,String appId) throws IOException;

    ResultEntity getDocList(String userId,String appId);

    Flux<String> chatWithWebSocketHandling(String userId, ChatParamsEntity chatParamsEntity, Consumer<String> responseHandler);

    ResultEntity deleteDoc(String docId, String userId, String directoryId);

    ResultEntity getDirectoryList(String userId);

    ResultEntity isDirExist(String userId, String directory);

    ResultEntity createDir(DirectoryEntity directoryEntity);

    ResultEntity renameDir(DirectoryEntity directoryEntity);

    ResultEntity deleteDir(String userId, long directoryId);
}
