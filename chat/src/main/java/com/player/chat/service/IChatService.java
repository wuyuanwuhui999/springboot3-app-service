package com.player.chat.service;

import com.player.chat.entity.ChatParamsEntity;
import com.player.chat.entity.DirectoryEntity;
import com.player.common.entity.ResultEntity;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.function.Consumer;

public interface IChatService {
    Flux<String> chat(String userId, ChatParamsEntity chatParamsEntity);

    ResultEntity getChatHistory(String userId,int pageNum,int pageSize);

    ResultEntity getModelList();

    ResultEntity uploadDoc(MultipartFile file,String userId,String tenantId,String directoryId) throws IOException;

    ResultEntity getDocList(String userId,String tenantId);

    Flux<String> chatWithWebSocketHandling(String userId, ChatParamsEntity chatParamsEntity, Consumer<String> responseHandler);

    ResultEntity deleteDoc(String docId, String userId, String directoryId);

    ResultEntity getDirectoryList(String userId,String tenantId);

    ResultEntity createDir(DirectoryEntity directoryEntity);

    ResultEntity renameDir(DirectoryEntity directoryEntity);

    ResultEntity deleteDir(String userId, long directoryId);

    ResultEntity getDocListByDirId(String userId,String tenantId,String directoryId);
}
