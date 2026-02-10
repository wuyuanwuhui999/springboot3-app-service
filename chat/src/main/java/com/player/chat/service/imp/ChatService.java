package com.player.chat.service.imp;

import com.player.chat.assistant.*;
import com.player.chat.assistant.*;
import com.player.chat.entity.ChatEntity;
import com.player.chat.entity.ChatParamsEntity;
import com.player.chat.entity.DirectoryEntity;
import com.player.chat.mapper.ChatMapper;
import com.player.chat.service.IChatService;
import com.player.chat.utils.PromptUtil;
import com.player.common.entity.ChatDocEntity;
import com.player.common.entity.ResultEntity;
import com.player.common.entity.ResultUtil;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchRequestFailedException;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.comparison.IsEqualTo;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
public class ChatService implements IChatService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ChatMapper chatMapper;

    @Value("${spring.servlet.multipart.location}")
    private String UPLOAD_DIR;


    @Autowired
    private ElasticsearchEmbeddingStore elasticsearchEmbeddingStore;

    private final EmbeddingModel nomicEmbeddingModel;

    private final AssistantSelector assistantSelector;

    public ChatService(EmbeddingModel nomicEmbeddingModel,AssistantSelector assistantSelector) {
        this.nomicEmbeddingModel = nomicEmbeddingModel;
        this.assistantSelector = assistantSelector;
    }


    @Override
    public Flux<String> chat(String userId, ChatParamsEntity chatParamsEntity) {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setUserId(userId);
        chatEntity.setChatId(chatParamsEntity.getChatId());
        chatEntity.setTenantId(chatParamsEntity.getTenantId());
        chatEntity.setPrompt(chatParamsEntity.getPrompt());
        chatEntity.setModelId(chatParamsEntity.getModelId());
        chatEntity.setContent(""); // Initialize empty content

        StringBuilder responseCollector = new StringBuilder();

        return chatWithWebSocketHandling(
                userId,
                chatParamsEntity,
                responsePart -> {
                    // Collect each response part
                    responseCollector.append(responsePart);
                    chatEntity.setContent(responseCollector.toString());
                }
        )
                .collectList()
                .flatMapMany(aiResponses -> {
                    // Save the final chat content
                    String fullResponse = String.join("", aiResponses);
                    chatEntity.setContent(fullResponse);
                    chatMapper.saveChat(chatEntity);
                    return Flux.fromIterable(aiResponses);
                });
    }

    @Override
    public Flux<String> chatWithWebSocketHandling(String userId, ChatParamsEntity chatParamsEntity, Consumer<String> responseHandler) {
        ChatEntity chatEntity = new ChatEntity();
        chatEntity.setUserId(userId);
        chatEntity.setChatId(chatParamsEntity.getChatId());
        chatEntity.setPrompt(chatParamsEntity.getPrompt());
        chatEntity.setModelId(chatParamsEntity.getModelId());
        chatEntity.setTenantId(chatParamsEntity.getTenantId());
        if ("document".equals(chatParamsEntity.getType())) {
            String context = PromptUtil.buildContext(nomicEmbeddingModel, elasticsearchEmbeddingStore, chatParamsEntity);
            if (context == null || context.isEmpty()) {
                return Flux.just("对不起，没有查询到相关文档").doOnNext(responseHandler);
            }
            chatParamsEntity.setPrompt(context);
        }

        return assistantSelector.selectAssistant(
                    chatParamsEntity
                )
                .doOnNext(responseHandler)
                .doOnComplete(() -> {
                    chatMapper.saveChat(chatEntity);
                })
                .doOnError(e -> {
                    log.error("Error during chat streaming", e);
                });
    }

    @Override
    public ResultEntity deleteDoc(String docId, String userId, String directoryId) {
        try {
            // 1. 先查询文档是否存在且属于该用户
            ChatDocEntity doc = chatMapper.getDocById(docId, userId,directoryId);
            if (doc == null) {
                return ResultUtil.fail(null, "文档不存在或无权删除");
            }

            // 2. 从文件系统中删除文件
            Path filePath = Paths.get(UPLOAD_DIR, doc.getId() + (doc.getExt().isEmpty() ? "" : "." + doc.getExt()));
            Files.deleteIfExists(filePath);

            // 3. 从Elasticsearch中删除文档
            IsEqualTo directoryFilter = new IsEqualTo("metadata.directory_id", directoryId);
            IsEqualTo userIdFilter = new IsEqualTo("metadata.user_id", userId);
            Filter andFilter = Filter.and(directoryFilter, userIdFilter);
            elasticsearchEmbeddingStore.removeAll(andFilter);

            // 4. 从数据库中删除记录
            long rows = chatMapper.deleteDoc(docId, userId,directoryId);

            return ResultUtil.success(rows, "文档删除成功");
        } catch (IOException e) {
            log.error("删除文档失败", e);
            return ResultUtil.fail(null, "删除文档失败: " + e.getMessage());
        }
    }

    @Override
    public ResultEntity getChatHistory(String tenantId, String userId, int pageNum, int pageSize) {
        int start = (pageNum - 1) * pageSize;
        ResultEntity success = ResultUtil.success(chatMapper.getChatHistory(tenantId,userId, start, pageSize));
        success.setTotal(chatMapper.getChatHistoryTotal(tenantId,userId));
        return success;
    }

    /**
     * 获取模型列表
     * 使用@Cacheable缓存结果，key为固定值"model:list"
     */
    @Cacheable(value = "model", key = "'list'")
    @Override
    public ResultEntity getModelList() {
        return ResultUtil.success(chatMapper.getModelList());
    }

    @Override
    public ResultEntity uploadDoc(MultipartFile file, String userId,String tenantId,String directoryId) throws IOException {
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
            String docId = UUID.randomUUID().toString().replace("-", "");
            String fileExtension = PromptUtil.getFileExtension(file);

            // 3. 分块处理文档
            if (originalFilename.toLowerCase().endsWith(".pdf")) {
                try (PDDocument pdfDocument = Loader.loadPDF(fileBytes)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    int totalPages = pdfDocument.getNumberOfPages();

                    // 分页处理PDF（每3页一批，避免过大请求）
                    int batchSize = 3;
                    for (int page = 1; page <= totalPages; page += batchSize) {
                        int endPage = Math.min(page + batchSize - 1, totalPages);
                        stripper.setStartPage(page);
                        stripper.setEndPage(endPage);

                        String batchContent = stripper.getText(pdfDocument);
                        processContentBatch(
                                batchContent,
                                originalFilename,
                                userId,
                                docId,
                                page,
                                endPage,
                                directoryId,
                                tenantId,
                                totalPages,         // total pages
                                fileExtension       // file type
                        );
                    }
                }
            } else {
                // 处理TXT文件（每5000字符一批）
                content = new String(fileBytes, StandardCharsets.UTF_8);
                int chunkSize = 5000;
                for (int i = 0; i < content.length(); i += chunkSize) {
                    String chunk = content.substring(i, Math.min(i + chunkSize, content.length()));
                    processContentBatch(
                            chunk,
                            originalFilename,
                            userId,
                            docId,
                            1,
                            1,
                            directoryId,
                            tenantId,
                            1,                 // total pages (1 for txt)
                            fileExtension      // file type
                    );
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
            chatDocEntity.setExt(fileExtension);
            chatDocEntity.setId(docId);
            chatDocEntity.setTenantId(tenantId);
            chatDocEntity.setDirectoryId(directoryId);

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

    private void processContentBatch(
            String content,
            String filename,
            String userId,
            String docId,
            int startPage,
            int endPage,
            String directoryId,
            String tenantId,
            int totalPages,
            String fileType
    ) {
        try {
            TextSegment textSegment = TextSegment.from(content);
            Embedding embedding = nomicEmbeddingModel.embed(content).content();

            Metadata metadata = textSegment.metadata();
            metadata.put("id", filename + "-pages-" + startPage + "-" + endPage);
            metadata.put("filename", filename);
            metadata.put("doc_id", docId);
            metadata.put("user_id", userId);
            metadata.put("page_range", startPage + "-" + endPage);

            // 添加新的元数据
            metadata.put("directory_id", directoryId);
            metadata.put("page", String.valueOf(totalPages)); // 总页数
            metadata.put("type", fileType);
            metadata.put("tenant_id", tenantId);
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

    /**
     * @author: wuwenqiang
     * @methodsName: getDocList
     * @description: 获取文档列表
     * @return: String
     * @date: 2025-07-24 21:23
     */
    @Override
    public ResultEntity getDocList(String userId,String tenantId) {
        return ResultUtil.success(chatMapper.getDocList(userId,tenantId));
    }

    /**
     * @author: wuwenqiang
     * @methodsName: getDirectoryList
     * @description: 获取目录列表，使用@Cacheable缓存结果，key为"directory:list:{userId}"
     * @date: 2025-07-24 21:23
     */
    @Cacheable(value = "directory", key = "'list:' + #userId +':' + #tenantId")
    @Override
    public ResultEntity getDirectoryList(String userId,String tenantId) {
        List<DirectoryEntity> directoryList = chatMapper.getDirectoryList(userId,tenantId);
        return ResultUtil.success(directoryList);
    }

    /**
     * @author: wuwenqiang
     * @methodsName: createDir
     * @description: 创建目录，使用@CacheEvict清除目录列表缓存
     * @date: 2025-07-24 21:23
     */
    /**
     * @author: wuwenqiang
     * @methodsName: createDir
     * @description: 创建目录，使用@CacheEvict清除目录列表缓存
     * @date: 2025-07-24 21:23
     */
    @CacheEvict(value = "directory", key = "'list:' + #directoryEntity.userId + ':' + #directoryEntity.tenantId")
    @Override
    public ResultEntity createDir(DirectoryEntity directoryEntity) {
        directoryEntity.setId(UUID.randomUUID().toString().replace("-", ""));

        long result = chatMapper.createDir(directoryEntity);

        if (result > 0) {
            // 插入成功后，查询完整的目录数据
            DirectoryEntity insertedDir = chatMapper.getDirectoryById(directoryEntity.getId(), directoryEntity.getUserId());
            return ResultUtil.success(insertedDir);
        }
        return ResultUtil.fail("创建目录失败");
    }

    /**
     * @author: wuwenqiang
     * @methodsName: renameDir
     * @description: 重命名目录，清除目录列表缓存和该目录的存在检查缓存
     * @date: 2025-07-24 21:23
     */
    @Caching(
            evict = {
                    @CacheEvict(value = "directory", key = "'list:' + #directoryEntity.userId + ':' + #directoryEntity.directory"),
                    @CacheEvict(value = "directory", key = "'exist:' + #directoryEntity.userId + ':' + #directoryEntity.directory")
            }
    )
    @Override
    public ResultEntity renameDir(DirectoryEntity directoryEntity) {
        long result = chatMapper.renameDir(directoryEntity);
        if (result > 0) {
            return ResultUtil.success(result);
        }
        return ResultUtil.fail("重命名目录失败");
    }

    /**
     * @author: wuwenqiang
     * @methodsName: deleteDir
     * @description: 删除目录。清除该用户所有相关的目录缓存
     * @date: 2025-07-24 21:23
     */
    @CacheEvict(value = "directory", allEntries = true)
    @Override
    public ResultEntity deleteDir(String userId, long directoryId) {
        long result = chatMapper.deleteDir(userId, directoryId);
        if (result > 0) {
            return ResultUtil.success(result);
        }
        return ResultUtil.fail("删除目录失败");
    }

    /**
     * @author: wuwenqiang
     * @methodsName: getDocListByDirId
     * @description: 根据目录id文档
     * @date: 2025-11-01 12:07
     */
    @Cacheable(value = "getDocListByDirId", key = "'list:' + #userId +':' + #tenantId + ':' + #directoryId")
    @Override
    public ResultEntity getDocListByDirId(String userId, String tenantId,String directoryId) {
        List<ChatDocEntity> docListByDirId = chatMapper.getDocListByDirId(userId, tenantId, directoryId);
        return ResultUtil.success(docListByDirId);
    }

}