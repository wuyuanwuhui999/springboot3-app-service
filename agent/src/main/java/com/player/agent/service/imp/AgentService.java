package com.player.agent.service.imp;

import com.player.agent.mapper.AgentMapper;
import com.player.common.entity.ChatDocEntity;
import com.player.common.entity.ChatEntity;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;
import com.player.agent.constants.SystemtConstants;
import com.player.agent.entity.AgentParamsEntity;
import com.player.agent.service.IAgentService;
import com.player.agent.tool.AgentTool;
import com.player.agent.uitls.AgentUtils;
import com.player.agent.uitls.PromptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AgentService implements IAgentService {

    @Autowired
    private AgentMapper agentMapper;


    @Autowired
    @Qualifier("deepseekOllamaChatClient")
    private ChatClient deepseekOllamaChatClient;

    @Autowired
    @Qualifier("qwenOllamaChatClient")
    private ChatClient qwenOllamaChatClient;

    @Autowired
    @Qualifier("deepseekOnlineChatClient")
    private ChatClient deepseekOnlineChatClient;

    @Autowired
    @Qualifier("qwenOnlineChatClient")
    private ChatClient qwenOnlineChatClient;

    @Value("${spring.servlet.multipart.location}")
    private String UPLOAD_DIR;

    @Autowired
    private AgentTool agentTool;

    @Autowired
    private VectorStore vectorStore;

    private ChatClient getChatClientByModelName(String modelType) {
        if (modelType.equals("qwen_ollama")) {
            return qwenOllamaChatClient;
        } else if (modelType.equals("deepseek_ollama")) {
            return deepseekOllamaChatClient;
        } else if (modelType.equals("deepseek_online")) {
            return deepseekOnlineChatClient;
        } else if (modelType.equals("qwen_online")) {
            return qwenOnlineChatClient;
        }
        return null;
    }

    @Override
    public Flux<String> chat(String userId, AgentParamsEntity agentParamsEntity) {
        ChatClient chatClient = getChatClientByModelName(agentParamsEntity.getModelName());
        if (chatClient == null) {
            return Flux.error(new IllegalArgumentException("Unsupported model: " + agentParamsEntity.getModelName()));
        }

        Flux<String> stringFlux = AgentUtils.processChat(
                agentParamsEntity,
                chatClient,
                vectorStore,
                userId,
                SystemtConstants.MUSIC_SYSTEMT_PROMPT,
                agentTool  // Added agentTool parameter
        );

        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setChatId(agentParamsEntity.getChatId());
        chatEntity.setUserId(userId);
        chatEntity.setPrompt(agentParamsEntity.getPrompt());
        chatEntity.setModelName(agentParamsEntity.getModelName());

        // 将 Flux<String> 转换为 Mono<String>
        Mono<String> contentMono = stringFlux.collectList()
                .map(list -> String.join("", list));

        // 订阅并保存到数据库
        contentMono.subscribe(content -> {
            chatEntity.setContent(content);
            agentMapper.saveChat(chatEntity);
        });

        return stringFlux;
    }

    @Override
    public ResultEntity getChatHistory(String userId, int pageNum, int pageSize) {
        int start = (pageNum - 1) * pageSize;
        ResultEntity success = ResultUtil.success(agentMapper.getChatHistory(userId, start, pageSize));
        success.setTotal(agentMapper.getChatHistoryTotal(userId));
        return success;
    }

    @Override
    public ResultEntity getModelList() {
        return ResultUtil.success(agentMapper.getModelList());
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

        agentMapper.saveDoc(chatDocEntity);

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
        return ResultUtil.success(agentMapper.getDocList(userId));
    }

    @Override
    public ResultEntity deleteDoc(String docId, String userId) {
        try {
            // 1. 先查询文档是否存在且属于该用户
            ChatDocEntity doc = agentMapper.getDocById(docId, userId);
            if (doc == null) {
                return ResultUtil.fail(null, "文档不存在或无权删除");
            }

            // 2. 从文件系统中删除文件
            Path filePath = Paths.get(UPLOAD_DIR, doc.getId() + (doc.getExt().isEmpty() ? "" : "." + doc.getExt()));
            Files.deleteIfExists(filePath);

            // 3. 从Elasticsearch中删除文档
            vectorStore.delete(List.of(docId));

            // 4. 从数据库中删除记录
            long rows = agentMapper.deleteDoc(docId, userId);

            return ResultUtil.success(rows, "文档删除成功");
        } catch (IOException e) {
            log.error("删除文档失败", e);
            return ResultUtil.fail(null, "删除文档失败: " + e.getMessage());
        }
    }
}
