package com.player.chat.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 会话记录压缩概要总结（MongoDB chat_memory 的备份，Mongo 崩溃后可据此恢复会话记忆）。
 * 表：chat_history_summary
 */
@Data
public class ChatHistorySummaryEntity {

    @Schema(description = "主键（user_id:chat_id 的 md5，与 FastAPI 同一算法，保证同会话同一行）")
    private String id;

    @Schema(description = "会话id")
    private String chatId;

    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "会话记录压缩后的概要总结")
    private String summary;

    @Schema(description = "生成该概要时压缩掉的原始会话消息条数")
    private Integer messageCount;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
