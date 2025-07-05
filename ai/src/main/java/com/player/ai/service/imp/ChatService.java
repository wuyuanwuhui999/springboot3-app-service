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
import dev.langchain4j.model.nomic.NomicEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
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
import java.nio.file.Files;
import java.util.UUID;

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
    public ResultEntity uploadDoc(MultipartFile file,String userId) throws IOException {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return ResultUtil.fail(null,"文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                (!originalFilename.toLowerCase().endsWith(".pdf") &&
                        !originalFilename.toLowerCase().endsWith(".txt"))) {
            return ResultUtil.fail("只能上传pdf和txt的文档");
        }
        String filePath = UPLOAD_DIR + "/" + originalFilename;
        File dest = new File(filePath);
        file.transferTo(dest);
        String content;
        if (originalFilename.toLowerCase().endsWith(".pdf")) {
            PDDocument pdfDocument = Loader.loadPDF(file.getBytes());
            PDFTextStripper stripper = new PDFTextStripper();
            content = stripper.getText(pdfDocument);

            for (int page = 1; page <= pdfDocument.getNumberOfPages(); page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String pageContent = stripper.getText(pdfDocument);
                TextSegment textSegment = TextSegment.from(pageContent);
                Embedding embedding = nomicEmbeddingModel.embed(pageContent).content();
                Metadata metadata = textSegment.metadata();
                metadata.put("filename", originalFilename);
                metadata.put("page", String.valueOf(page));
                metadata.put("total_pages", String.valueOf(pdfDocument.getNumberOfPages()));
                elasticsearchEmbeddingStore.add(embedding, textSegment);
            }
        } else {
            // Read TXT file
            content = new String(Files.readAllBytes(dest.toPath()));
        }

        Embedding embedding = nomicEmbeddingModel.embed(content).content();
        TextSegment textSegment = TextSegment.from(content);
        elasticsearchEmbeddingStore.add(embedding, textSegment);

        ChatDocEntity chatDocEntity = new ChatDocEntity();
        chatDocEntity.setName(originalFilename);
        chatDocEntity.setUserId(userId);
        chatDocEntity.setExt(PromptUtil.getFileExtension(file));
        // 生成32位ID
        String fileId = UUID.randomUUID().toString().replace("-", "");
        chatDocEntity.setId(fileId);

        chatMapper.saveDoc(chatDocEntity);

        return ResultUtil.success("文件上传成功");
    }

    @Override
    public Flux<String> searchDoc(String query,String chatId,String modelName) {
        String context = PromptUtil.buildContext(nomicEmbeddingModel, elasticsearchEmbeddingStore, query);
        String finalPrompt = PromptUtil.buildPrompt(query, context);
        return AssistantSelector.selectAssistant(modelName, qwenAssistant, deepSeekAssistant, chatId, finalPrompt, false);
    }

    @Override
    public ResultEntity getDocList(String userId) {
        return ResultUtil.success(chatMapper.getDocList(userId));
    }
}