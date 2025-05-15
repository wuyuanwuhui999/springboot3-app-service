package com.player.ai.service.imp;

import com.player.ai.entity.ChatEntity;
import com.player.ai.mapper.ChatMapper;
import com.player.ai.service.IChatService;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Service
public class ChatService implements IChatService {

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private ChatClient chatClient;

    @Value("${spring.servlet.multipart.location}")
    private String UPLOAD_DIR;

    @Override
    public Flux<String> chat(String userId, String prompt, String chatId, List<MultipartFile> files) {
        Flux<String> stringFlux;
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setChatId(chatId);
        chatEntity.setUserId(userId);
        chatEntity.setPrompt(prompt);
        if (files == null || files.isEmpty()) {
            // 没有附件，纯文本聊天
            stringFlux = chatClient
                    .prompt()
                    .user(prompt)

                    .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))
                    .stream()
                    .content();

        } else {
            // 有附件，多模态聊天
            // 1.解析多媒体
            String uploadFiles = upload(files);
            chatEntity.setFiles(uploadFiles);
            List<Media> medias = files.stream()
                    .map(file -> new Media(
                                    MimeType.valueOf(Objects.requireNonNull(file.getContentType())),
                                    file.getResource()
                            )
                    )
                    .toList();
            // 2.请求模型
            stringFlux =  chatClient.prompt()
                    .user(p -> p.text(prompt).media(medias.toArray(Media[]::new)))
                    .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                    .stream()
                    .content();
        }
        // 将 Flux<String> 转换为 Mono<String>
        Mono<String> contentMono = stringFlux.collectList()
                .map(list -> String.join("", list)); // 拼接字符串

        // 订阅并保存到数据库
        contentMono.subscribe(content -> {
            chatEntity.setContent(content); // 设置内容
            chatMapper.saveChat(chatEntity);
        });

        return stringFlux;
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