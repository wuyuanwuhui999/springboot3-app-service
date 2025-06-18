package com.player.music.service.imp;

import com.player.common.utils.FileTypeUtil;
import com.player.music.entity.ChatEntity;
import com.player.music.entity.FileEntity;
import com.player.music.handler.ChatWebSocketHandler;
import com.player.music.mapper.ChatMapper;
import com.player.music.service.IChatService;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
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

    @Override
    public ResultEntity generateVector(FileEntity fileEntity) {
        if (fileEntity == null || fileEntity.getBase64() == null || fileEntity.getBase64().length == 0) {
            return ResultUtil.fail("Base64数据不能为空");
        }
        List<Document> documents = new ArrayList<>();
        List<String> fileUrls = new ArrayList<>();
        try {
            for (String base64 : fileEntity.getBase64()) {
                if (!StringUtils.hasText(base64)) {
                    continue;
                }
                String[] parts = base64.split(",");
                if (parts.length < 2) {
                    continue;
                }
                String header = parts[0];
                String dataPart = parts[1];
                byte[] fileBytes = Base64.getDecoder().decode(dataPart);
                String fileExtension = FileTypeUtil.getExtensionFromBase64Header(header);
                String fileName = UUID.randomUUID() + fileExtension;
                Path directory = Paths.get(UPLOAD_DIR);
                if (!Files.exists(directory)) {
                    Files.createDirectories(directory);
                }
                Path filePath = directory.resolve(fileName);
                Files.write(filePath, fileBytes);
                String fileUrl = UPLOAD_DIR + "/" + fileName;
                fileUrls.add(fileUrl);
                Document document = convertToDocument(filePath);
                if (document != null) {
                    documents.add(document);
                }
            }
            vectorStore.add(documents);
            return ResultUtil.success(fileUrls, "文件保存成功");
        } catch (IOException e) {
            e.printStackTrace();
            return ResultUtil.fail("文件保存失败: " + e.getMessage());
        }
    }

    private Document convertToDocument(Path filePath) throws IOException {
        // Get file extension
        String fileName = filePath.getFileName().toString();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        // Read file content based on file type
        String content;
        switch (fileExtension) {
            case "txt":
                content = new String(Files.readAllBytes(filePath));
                break;
            case "pdf":
                try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    content = stripper.getText(document);
                }
                break;
            default:
                throw new UnsupportedOperationException("不支持的文件格式: " + fileExtension);
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("file_name", fileName);
        metadata.put("file_path", filePath.toString());
        metadata.put("file_type", fileExtension);

        return new Document(content, metadata);
    }
}
