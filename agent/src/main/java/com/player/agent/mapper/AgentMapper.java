package com.player.agent.mapper;

import com.player.agent.uitls.AgentSqlProvider;
import com.player.common.entity.ChatEntity;
import com.player.common.entity.ChatModelEntity;
import com.player.common.entity.MusicEntity;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface AgentMapper {
    void saveChat(ChatEntity chatEntity);

    List<ChatEntity> getChatHistory(String userId, int start, int limit);

    Long getChatHistoryTotal(String userId);

    List<ChatModelEntity> getModelList();

    List<MusicEntity> selectMusicList(String author);

    ChatModelEntity getModelById(String modelId);

    // 新增：执行动态SQL查询
    @Select("${sql}")
    List<Map<String, Object>> executeDynamicQuery(@Param("sql") String sql);

    // 新增：安全的SQL查询方法，只允许SELECT
    @SelectProvider(type = AgentSqlProvider.class, method = "buildSafeQuery")
    List<Map<String, Object>> safeQuery(@Param("conditions") Map<String, Object> conditions);
}