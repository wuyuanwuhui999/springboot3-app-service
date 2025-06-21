package com.player.ai.service.imp;

import com.player.ai.assistant.Assistant;
import com.player.ai.mapper.ChatMapper;
import com.player.ai.service.IChatService;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class ChatService implements IChatService {


    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private Assistant assistant;

    @Value("${spring.servlet.multipart.location}")
    private String UPLOAD_DIR;

    @Override
    public Flux<String> chat(String userId, String prompt, String chatId, List<MultipartFile> files) {
        return assistant.chat(chatId,prompt);
    }

    @Override
    public String upload(List<MultipartFile>files){
        // 确保上传目录存在
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        List<String> uploadedFileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                // 获取原始文件名
                String originalFileName = file.getOriginalFilename();

                // 获取文件扩展名
                String fileExtension = "";
                if (originalFileName != null && originalFileName.contains(".")) {
                    fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }

                // 生成唯一文件名
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

                // 构建文件保存路径
                Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);

                // 将文件保存到指定路径
                Files.copy(file.getInputStream(), filePath);

                // 添加到文件名列表
                uploadedFileNames.add(uniqueFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 拼接文件名，用分号隔开
        String result = String.join(";", uploadedFileNames);
        return result.isEmpty() ? "" : result;
    }

    @Override
    public ResultEntity getChatHistory(String userId, int pageNum, int pageSize){
        int start = (pageNum - 1) * pageSize;
        ResultEntity success = ResultUtil.success(chatMapper.getChatHistory(userId, start, pageSize));
        success.setTotal(chatMapper.getChatHistoryTotal(userId));
        return success;
    }
}