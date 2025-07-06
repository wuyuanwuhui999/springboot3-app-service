package com.player.ai.service.imp;

import com.player.ai.assistant.AssistantSelector;
import com.player.ai.assistant.DeepSeekAssistant;
import com.player.ai.assistant.QwenAssistant;
import com.player.ai.entity.ChatEntity;
import com.player.ai.mapper.ChatMapper;
import com.player.ai.service.IChatService;
import com.player.ai.utils.PromptUtil;
import com.player.common.entity.ChatDocEntity;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchRequestFailedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

@Slf4j
@Service
public class ChatService implements IChatService {


    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private QwenAssistant qwenAssistant;

    @Autowired
    private DeepSeekAssistant deepSeekAssistant;

    @Value("${spring.servlet.multipart.location}")
    private String UPLOAD_DIR;

    @Autowired
    private ElasticsearchEmbeddingStore elasticsearchEmbeddingStore;

    private final EmbeddingModel nomicEmbeddingModel;

    public ChatService(EmbeddingModel nomicEmbeddingModel) {
        this.nomicEmbeddingModel = nomicEmbeddingModel;
    }


    @Override
    public Flux<String> chat(String userId, String prompt, String chatId, String modelName,boolean showThink) {
        // 确保中文编码正确
        // 构建ChatEntity对象用于保存
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setUserId(userId);
        chatEntity.setChatId(chatId);
        chatEntity.setPrompt(prompt);
        chatEntity.setModelName(modelName);
        return AssistantSelector.selectAssistant(modelName, qwenAssistant, deepSeekAssistant, chatId, prompt,showThink)
                .collectList()
                .flatMapMany(aiResponses -> {
                    String fullResponse = String.join("", aiResponses);
                    chatEntity.setContent(fullResponse);
                    chatMapper.saveChat(chatEntity);
                    return Flux.fromIterable(aiResponses);
                });
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
    public ResultEntity uploadDoc(MultipartFile file, String userId) throws IOException {
        // 1. 基础验证
        if (file.isEmpty()) {
            return ResultUtil.fail(null, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                (!originalFilename.toLowerCase().endsWith(".pdf") &&
                        !originalFilename.toLowerCase().endsWith(".txt"))) {
            return ResultUtil.fail("只能上传pdf和txt的文档");
        }

        try {
            // 2. 读取文件内容
            byte[] fileBytes = file.getBytes();
            String content;

            // 3. 分块处理文档
            if (originalFilename.toLowerCase().endsWith(".pdf")) {
                try (PDDocument pdfDocument = Loader.loadPDF(fileBytes)) {
                    PDFTextStripper stripper = new PDFTextStripper();

                    // 分页处理PDF（每3页一批，避免过大请求）
                    int batchSize = 3;
                    for (int page = 1; page <= pdfDocument.getNumberOfPages(); page += batchSize) {
                        int endPage = Math.min(page + batchSize - 1, pdfDocument.getNumberOfPages());
                        stripper.setStartPage(page);
                        stripper.setEndPage(endPage);

                        String batchContent = stripper.getText(pdfDocument);
                        processContentBatch(batchContent, originalFilename, userId, page, endPage);
                    }
                }
            } else {
                // 处理TXT文件（每5000字符一批）
                content = new String(fileBytes, StandardCharsets.UTF_8);
                int chunkSize = 5000;
                for (int i = 0; i < content.length(); i += chunkSize) {
                    String chunk = content.substring(i, Math.min(i + chunkSize, content.length()));
                    processContentBatch(chunk, originalFilename, userId, 1, 1);
                }
            }

            // 4. 保存文件到本地
            String filePath = UPLOAD_DIR + "/" + originalFilename;
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);

            // 5. 保存文档元数据
            ChatDocEntity chatDocEntity = new ChatDocEntity();
            chatDocEntity.setName(originalFilename);
            chatDocEntity.setUserId(userId);
            chatDocEntity.setExt(PromptUtil.getFileExtension(file));
            chatDocEntity.setId(UUID.randomUUID().toString().replace("-", ""));

            chatMapper.saveDoc(chatDocEntity);

            return ResultUtil.success("文件上传成功");

        } catch (ElasticsearchRequestFailedException e) {
            log.error("Elasticsearch操作失败: {}", e.getMessage());
            return ResultUtil.fail("文档处理失败，请稍后重试");
        } catch (IOException e) {
            log.error("文件处理失败: {}", e.getMessage());
            return ResultUtil.fail("文件处理失败");
        } catch (Exception e) {
            log.error("未知错误: {}", e.getMessage());
            return ResultUtil.fail("系统错误");
        }
    }

    private void processContentBatch(String content, String filename, String userId, int startPage, int endPage) {
        try {
            TextSegment textSegment = TextSegment.from(content);
            Embedding embedding = nomicEmbeddingModel.embed(content).content();

            Metadata metadata = textSegment.metadata();
            metadata.put("id", filename + "-pages-" + startPage + "-" + endPage);
            metadata.put("filename", filename);
            metadata.put("user_id", userId);
            metadata.put("page_range", startPage + "-" + endPage);

            // 带重试机制的存储
            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    elasticsearchEmbeddingStore.add(embedding, textSegment);
                    break;
                } catch (ElasticsearchRequestFailedException e) {
                    if (i == maxRetries - 1) throw e;
                    Thread.sleep(1000 * (i + 1)); // 指数退避
                }
            }
        } catch (Exception e) {
            log.error("处理文档分块失败: {} pages {}-{}", filename, startPage, endPage, e);
            throw new RuntimeException("文档处理失败", e);
        }
    }

    @Override
    public Flux<String> searchDoc(String query, String chatId, String userId, String modelName) {
        // 修改buildContext方法调用，传入userId
        String context = PromptUtil.buildContext(nomicEmbeddingModel, elasticsearchEmbeddingStore, query, userId);
        String finalPrompt = PromptUtil.buildPrompt(query, context);
        return AssistantSelector.selectAssistant(modelName, qwenAssistant, deepSeekAssistant, chatId, finalPrompt, false);
    }

    @Override
    public ResultEntity getDocList(String userId) {
        return ResultUtil.success(chatMapper.getDocList(userId));
    }
}