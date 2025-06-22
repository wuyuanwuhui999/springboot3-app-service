package com.player.music.service.imp;

import com.player.common.utils.FileTypeUtil;
import com.player.music.entity.ChatEntity;
import com.player.music.handler.ChatWebSocketHandler;
import com.player.music.mapper.ChatMapper;
import com.player.music.service.IChatService;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.music.uitls.PromptUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@Service
public class ChatService implements IChatService {

    @Bean
    public ChatWebSocketHandler chatWebSocketHandler(ChatClient chatClient, ChatService chatService, ChatMapper chatMapper,
                                                     @Value("${spring.servlet.multipart.location}") String uploadDir) {
        return new ChatWebSocketHandler(chatClient, chatService, chatMapper, uploadDir);
    }

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private ChatClient chatClient;

    @Value("${spring.servlet.multipart.location}")
    private String UPLOAD_DIR;

    @Autowired
    private VectorStore vectorStore;

    @Override
    public Flux<String> chat(String userId, String prompt, String chatId, int modelId, List<MultipartFile> files) {
        Flux<String> stringFlux;
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setChatId(chatId);
        chatEntity.setUserId(userId);
        chatEntity.setPrompt(prompt);
        chatEntity.setModelId(modelId);

        if (files == null || files.isEmpty()) {
            // 没有附件，纯文本聊天
            stringFlux = chatClient
                    .prompt()
                    .user(prompt)
                    .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
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
            stringFlux = chatClient.prompt()
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
    public String upload(List<MultipartFile> files) {
        File uploadDir = new File(this.UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        List<String> uploadedFileNames = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String originalFileName = file.getOriginalFilename();
                String fileExtension = "";
                if (originalFileName != null && originalFileName.contains(".")) {
                    fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
                }
                String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
                Path filePath = Paths.get(UPLOAD_DIR + uniqueFileName);
                Files.copy(file.getInputStream(), filePath);
                uploadedFileNames.add(uniqueFileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return String.join(";", uploadedFileNames);
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
    public ResultEntity generateVector(MultipartFile file) throws IOException {
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
        // 保存文件
        byte[] bytes = file.getBytes();
        Path path = uploadPath.resolve(Objects.requireNonNull(file.getOriginalFilename()));
        Files.write(path, bytes);
        List<String> fileUrls = new ArrayList<>();
        List<Document> documents = PromptUtil.convertToDocument(file);
        vectorStore.add(documents);
        return ResultUtil.success(fileUrls, "文件保存成功");
    }

    @Override
    public Flux<String> searchDoc(String query,String chatId) {
        // 1. 从向量库检索相关文档
        List<Document> relevantDocs = vectorStore.similaritySearch(query);
        // 2. 构建上下文提示
        String context = PromptUtil.buildContext(relevantDocs);
        // 3. 构建完整提示词
        String fullPrompt = PromptUtil.buildPrompt(query, context);
        return chatClient
                .prompt()
                .user(fullPrompt)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content();

    }
}
