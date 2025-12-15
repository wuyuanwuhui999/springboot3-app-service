package com.player.agent.uitls;

import java.util.Map;

// SQL提供者类
public class AgentSqlProvider {
    public String buildSafeQuery(Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        Map<String, Object> conditions = (Map<String, Object>) params.get("conditions");

        StringBuilder sql = new StringBuilder("SELECT * FROM music WHERE 1=1 ");

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
