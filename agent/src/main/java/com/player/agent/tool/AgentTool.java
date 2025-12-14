package com.player.agent.tool;

import com.player.agent.client.MusicFeignClient;
import com.player.agent.mapper.AgentMapper;
import com.player.common.entity.MusicEntity;
import com.player.common.entity.ResultEntity;
import com.player.common.utils.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

@Component
public class AgentTool {
    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private MusicFeignClient musicFeignClient;

    // 建议扩展AgentTool类
    @Tool(description = "根据多种条件查询音乐")
    public List<MusicEntity> queryMusic(
            @ToolParam(description = "歌曲名称（可选）") String songName,
            @ToolParam(description = "歌手名称（可选）") String authorName,
            @ToolParam(description = "专辑名称（可选）") String albumName,
            @ToolParam(description = "语言（可选）") String language,
            @ToolParam(description = "发布时间范围开始（可选）") Date publishStart,
            @ToolParam(description = "标签（可选）") String label,
            @ToolParam(description = "页码，从1开始，必须") int pageNum,
            @ToolParam(description = "每页条数，必须") int pageSize
    ) {
        return (List<MusicEntity>) musicFeignClient.queryMusic(songName,authorName,albumName,language,publishStart,label,pageNum,pageSize).getData();
    }


    @Tool(description = "查询用户收藏的歌曲")
    public List<MusicEntity> getMusicListByFavoriteId(
            @ToolParam(description = "用户ID") String userId,
            @ToolParam(description = "页码，从1开始，必须") int pageNum,
            @ToolParam(description = "每页条数，必须") int pageSize,
            @ToolParam(description = "收藏夹Id,可选，省略时查询所有收藏加的歌曲") String favoriteId
    ) {
        return (List<MusicEntity>) musicFeignClient.getMusicListByFavoriteId(favoriteId,userId,pageNum,pageSize).getData();
    }

    @Tool(description = "获取用户播放历史")
    public List<MusicEntity> getMusicRecord(
            @ToolParam(description = "用户ID") String userId,
            @ToolParam(description = "开始时间，可省，默认null") Date startDate,
            @ToolParam(description = "结束时间，可省，默认null") Date endDate,
            @ToolParam(description = "页码，从1开始") int pageNum,
            @ToolParam(description = "每页条数，可省，默认100") int pageSize
            ) {
        return (List<MusicEntity>) musicFeignClient.getMusicRecord(userId,startDate,endDate,pageNum,pageSize).getData();
    }

    @Tool(description = "查询歌手的所有歌曲")
    @GetMapping("/music/getMusicListByAuthor")
    public List<MusicEntity> getMusicListByAuthor(
            HttpServletRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize,
            @RequestParam(name = "authorId",required = true) String authorId,
            @RequestParam(name = "authorId",required = true) String authorName
    ){
        return (List<MusicEntity>) musicFeignClient.getMusicListByAuthor(authorId,authorName,userId,pageNum,pageSize).getData();
    }

    @Tool(description = "智能音乐搜索")
    public String smartMusicSearch(String userQuery) {
        // 1.使用AI分析用户意图，生成SQL

        // 2.构建动态SQL查询

        // 3.执行查询
//        return agentMapper.executeDynamicQuery(sql);
        return null;
    }

    @Tool(description = "推荐我可能喜欢的歌曲")
    public List<MusicEntity> recommendForUser(String userId) {
        // 1. 获取用户历史偏好
        ResultEntity musicRecord = musicFeignClient.getMusicRecord(userId, 1, 100);
        List<MusicEntity> history = (List<MusicEntity>) musicRecord.getData();
        ResultEntity favoriteDirectory = musicFeignClient.getFavoriteDirectory(0L, userId);
        List<MusicEntity> favorites = (List<MusicEntity>) favoriteDirectory.getData();

        // 2. 分析偏好特征
//        UserPreference preference = analyzePreference(history, favorites);

        // 3. 基于偏好推荐
//        return recommendationEngine.recommend(preference);
        return null;
    }
}
