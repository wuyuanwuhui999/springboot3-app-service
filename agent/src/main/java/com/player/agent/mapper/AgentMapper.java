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

    // ==================== agent 工具（收藏夹 / 音乐圈）====================
    /** 按「用户 + 收藏夹名称」查收藏夹 id（用户只知道名称，id 由服务端解析） */
    Long getFavoriteDirectoryIdByName(@Param("userId") String userId, @Param("name") String name);

    int insertFavoriteDirectory(@Param("name") String name, @Param("userId") String userId);

    int deleteFavoriteDirectoryByName(@Param("userId") String userId, @Param("name") String name);

    int updateFavoriteDirectoryName(@Param("userId") String userId,
                                    @Param("oldName") String oldName,
                                    @Param("newName") String newName);

    /** 收藏夹里现有多少首歌（删除时用于提示） */
    Long countFavoriteListRows(@Param("favoriteId") Long favoriteId);

    /** 该歌曲是否已在收藏夹里（music_favorite_list 无唯一约束，显式判重） */
    Long countFavoriteListItem(@Param("favoriteId") Long favoriteId, @Param("musicId") Long musicId);

    int insertFavoriteListItem(@Param("musicId") Long musicId, @Param("favoriteId") Long favoriteId);

    /** 按名称条件解析歌曲（供收藏/分享工具把「歌名/歌手」换成 music_id） */
    List<Map<String, Object>> selectMusicByNames(@Param("songName") String songName,
                                                 @Param("authorName") String authorName,
                                                 @Param("albumName") String albumName,
                                                 @Param("limit") int limit);

    List<Map<String, Object>> selectMusicByIds(@Param("musicIds") List<Long> musicIds, @Param("limit") int limit);

    /** 发音乐圈（写 circle 表，type=MUSIC，relation_id = music.id） */
    int insertCircle(@Param("content") String content,
                     @Param("relationId") Long relationId,
                     @Param("userId") String userId,
                     @Param("permission") int permission);
}