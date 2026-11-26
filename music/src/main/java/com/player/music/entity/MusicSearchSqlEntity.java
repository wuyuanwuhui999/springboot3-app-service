package com.player.music.entity;

import lombok.Data;

/**
 * 智能搜索 SQL 缓存（表 music_search_sql）。
 * <p>
 * 同一个 searchId 只让大模型生成一次 SQL，分页时复用；
 * 查询/复用一律带 user_id，避免跨用户复用别人生成的 SQL。
 */
@Data
public class MusicSearchSqlEntity {

    /** 主键 */
    private Integer id;

    /** 搜索会话ID（前端生成，与 agent 的 chatId 对齐） */
    private String searchId;

    /** 用户ID */
    private String userId;

    /** 生成 SQL 时的搜索关键词 */
    private String keyword;

    /** 生成 SQL 使用的大模型ID */
    private String modelId;

    /** 大模型生成的 WHERE 条件 */
    private String sqlCondition;

    /** 大模型提取的关键词 */
    private String searchKeyword;

    /** 大模型判定的搜索类型 */
    private String searchType;

    /** 大模型给出的意图说明 */
    private String explanation;

    /** 创建时间 */
    private String createTime;

    /** 更新时间 */
    private String updateTime;
}
