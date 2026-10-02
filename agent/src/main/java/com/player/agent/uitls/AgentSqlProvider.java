package com.player.agent.uitls;

import java.util.Map;

// SQL提供者类
public class AgentSqlProvider {
    public String buildSafeQuery(Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        Map<String, Object> conditions = (Map<String, Object>) params.get("conditions");

        // SELECT 列表：下划线字段统一起驼峰别名（song_name AS songName），查询结果直接就是驼峰字段
        // （原先是 SELECT *，返回的 Map key 为下划线列名；WHERE 条件里仍用数据库真实列名）
        StringBuilder sql = new StringBuilder("SELECT "
                + "id, album_id AS albumId, song_name AS songName, author_id AS authorId, "
                + "author_name AS authorName, album_name AS albumName, version, language, "
                + "publish_date AS publishDate, wide_audio_id AS wideAudioId, is_publish AS isPublish, "
                + "big_pack_id AS bigPackId, final_id AS finalId, audio_id AS audioId, "
                + "similar_audio_id AS similarAudioId, is_hot AS isHot, album_audio_id AS albumAudioId, "
                + "audio_group_id AS audioGroupId, cover, play_url AS playUrl, "
                + "local_play_url AS localPlayUrl, source_name AS sourceName, source_url AS sourceUrl, "
                + "create_time AS createTime, update_time AS updateTime, label, lyrics, permission "
                + "FROM music WHERE 1=1 ");

        if (conditions.containsKey("songName")) {
            sql.append("AND song_name LIKE CONCAT('%', #{conditions.songName}, '%') ");
        }
        if (conditions.containsKey("authorName")) {
            sql.append("AND author_name LIKE CONCAT('%', #{conditions.authorName}, '%') ");
        }
        if (conditions.containsKey("albumName")) {
            sql.append("AND album_name LIKE CONCAT('%', #{conditions.albumName}, '%') ");
        }
        if (conditions.containsKey("language")) {
            sql.append("AND language = #{conditions.language} ");
        }
        if (conditions.containsKey("label")) {
            sql.append("AND label LIKE CONCAT('%', #{conditions.label}, '%') ");
        }
        if (conditions.containsKey("isHot")) {
            sql.append("AND is_hot = #{conditions.isHot} ");
        }

        sql.append("LIMIT 100"); // 限制返回数量

        return sql.toString();
    }
}
