package com.player.gateway.service.imp;

import com.player.gateway.entity.LogEntity;
import com.player.gateway.service.ILogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class LogService implements ILogService {

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Override
    public void saveRequestLog(LogEntity logEntity) {
        if (logEntity.getId() == null) {
            logEntity.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (logEntity.getCreateTime() == null) {
            logEntity.setCreateTime(LocalDateTime.now());
            logEntity.setUpdateTime(LocalDateTime.now());
        }
        // 异步插入（fire-and-forget）
        mongoTemplate.save(logEntity)
                .doOnError(e -> System.err.println("Failed to save log to MongoDB: " + e.getMessage()))
                .subscribe();
    }

    @Override
    public void updateResponseInfo(String requestId, String responseStatus, String responseBody,
                                   String responseHeaders, Long executeTime, String errorMessage) {
        Integer status = null;
        try {
            status = Integer.parseInt(responseStatus);
        } catch (NumberFormatException e) {
            status = 500;
        }

        Query query = Query.query(Criteria.where("request_id").is(requestId));
        Update update = new Update()
                .set("response_status", status)
                .set("response_body", responseBody)
                .set("response_headers", responseHeaders)
                .set("execute_time", executeTime)
                .set("error_message", errorMessage)
                .set("update_time", LocalDateTime.now());

        // 异步更新
        mongoTemplate.updateFirst(query, update, LogEntity.class)
                .doOnError(e -> System.err.println("Failed to update log in MongoDB: " + e.getMessage()))
                .subscribe();
    }
}