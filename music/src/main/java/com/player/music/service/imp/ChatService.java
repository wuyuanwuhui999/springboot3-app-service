package com.player.music.service.imp;

import com.player.common.entity.ChatDocEntity;
import com.player.common.entity.ChatEntity;
import com.player.music.constants.SystemtConstants;
import com.player.music.entity.ChatParamsEntity;
import com.player.music.mapper.ChatMapper;
import com.player.music.service.IChatService;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.music.tools.MusicTool;
import com.player.music.uitls.ChatUtils;
import com.player.music.uitls.PromptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Slf4j
@Service
public class ChatService implements IChatService {

    @Autowired
    private ChatMapper chatMapper;


    @Autowired
    @Qualifier("deepseekChatClient")
    private ChatClient deepseekChatClient;

    @Autowired
    @Qualifier("qwenChatClient")
    private ChatClient qwenChatClient;

    @Value("${spring.servlet.multipart.location}")
    private String UPLOAD_DIR;

    @Autowired
    private MusicTool musicTool;

    @Autowired
    private VectorStore vectorStore;

    private ChatClient getChatClientByModelName(String modelName) {
        if ("qwen3:8b".equalsIgnoreCase(modelName)) {
            return qwenChatClient;
        } else if ("deepseek-r1:8b".equalsIgnoreCase(modelName)) {
            return deepseekChatClient;
        }
        return null;
    }

    @Override
    public Flux<String> chat(String userId, ChatParamsEntity chatParamsEntity) {
        ChatClient chatClient = getChatClientByModelName(chatParamsEntity.getModelName());
        if (chatClient == null) {
            return Flux.error(new IllegalArgumentException("Unsupported model: " + chatParamsEntity.getModelName()));
        }

        Flux<String> stringFlux = ChatUtils.processChat(
                chatParamsEntity,
                chatClient,
                vectorStore,
                userId,
                SystemtConstants.MUSIC_SYSTEMT_PROMPT,
                musicTool  // Added musicTool parameter
        );

        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setChatId(chatParamsEntity.getChatId());
        chatEntity.setUserId(userId);
        chatEntity.setPrompt(chatParamsEntity.getPrompt());
        chatEntity.setModelName(chatParamsEntity.getModelName());

        // 将 Flux<String> 转换为 Mono<String>
        Mono<String> contentMono = stringFlux.collectList()
                .map(list -> String.join("", list));

        // 订阅并保存到数据库
        contentMono.subscribe(content -> {
            chatEntity.setContent(content);
            chatMapper.saveChat(chatEntity);
        });

        return stringFlux;
    }

    @Override
    public ResultEntity getChatHistory(String userId, int pageNum, int pageSize) {
        int start = (pageNum - 1) * pageSize;
        ResultEntity success = ResultUtil.success(chatMapper.getChatHistory(userId, start, pageSize));
        success.setTotal(chatMapper.getChatHistoryTotal(userId));
        return success;
    }

    @Override
    public ResultEntity getModelList() {
        return ResultUtil.success(chatMapper.getModelList());
    }

    // 允许的文件类型
    private static final List<String> ALLOWED_TYPES = Arrays.asList("text/plain", "application/pdf");

    @Override
    public ResultEntity uploadDoc(MultipartFile file, String userId) throws IOException {
        // 添加内容检查
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            return ResultUtil.fail(null,"文件大小不能超过10MB");
        }
        // 检查文件类型
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            return ResultUtil.fail(null,"只允许上传txt和pdf格式文件");
        }

        // 检查文件是否为空
        if (file.isEmpty()) {
            return ResultUtil.fail(null,"文件不能为空");
        }

        // 创建上传目录（如果不存在）
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        ChatDocEntity chatDocEntity = new ChatDocEntity();

        // 生成32位ID
        String fileId = UUID.randomUUID().toString().replace("-", "");
        chatDocEntity.setId(fileId);

        // 获取原始文件名与扩展名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = PromptUtil.getFileExtension(file);

        chatDocEntity.setName(originalFilename);
        chatDocEntity.setUserId(userId);
        chatDocEntity.setExt(fileExtension);

        chatMapper.saveDoc(chatDocEntity);

        // 保存文件：使用32位ID作为文件名
        byte[] bytes = file.getBytes();

        // 构造新文件名：32位ID + 扩展名
        String newFileName = fileId + (fileExtension.isEmpty() ? "" : "." + fileExtension);
        Path path = uploadPath.resolve(newFileName);

        Files.write(path, bytes);

        List<String> fileUrls = new ArrayList<>();
        List<Document> documents = PromptUtil.convertToDocument(file,fileId,userId);

        // 设置当前用户
        vectorStore.add(documents);

        return ResultUtil.success(fileUrls, "文件保存成功");
    }

    @Override
    public ResultEntity getDocList(String userId) {
        return ResultUtil.success(chatMapper.getDocList(userId));
    }

    @Override
    public ResultEntity deleteDoc(String docId, String userId) {
        try {
            // 1. 先查询文档是否存在且属于该用户
            ChatDocEntity doc = chatMapper.getDocById(docId, userId);
            if (doc == null) {
                return ResultUtil.fail(null, "文档不存在或无权删除");
            }

            // 2. 从文件系统中删除文件
            Path filePath = Paths.get(UPLOAD_DIR, doc.getId() + (doc.getExt().isEmpty() ? "" : "." + doc.getExt()));
            Files.deleteIfExists(filePath);

            // 3. 从Elasticsearch中删除文档
            vectorStore.delete(List.of(docId));

            // 4. 从数据库中删除记录
            long rows = chatMapper.deleteDoc(docId, userId);

            return ResultUtil.success(rows, "文档删除成功");
        } catch (IOException e) {
            log.error("删除文档失败", e);
            return ResultUtil.fail(null, "删除文档失败: " + e.getMessage());
        }
    }
}
