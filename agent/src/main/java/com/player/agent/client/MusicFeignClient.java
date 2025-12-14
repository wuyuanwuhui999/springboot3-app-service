package com.player.agent.client;

import com.player.common.entity.ResultEntity;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

@FeignClient(name = "music-service") // 对应 music 模块的 spring.application.name
public interface MusicFeignClient {

    @GetMapping("/service/music/getMusicRecord")
    ResultEntity getMusicRecord(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("startDate") Date startDate,
            @RequestParam("endDate") Date endDate,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize
    );

    // 查询收藏的歌手
    @GetMapping("/service/music/getFavoriteAuthor")
    ResultEntity getFavoriteAuthor(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    );

    // 查询点赞的收藏
    @GetMapping("/service/music/getMusicLike")
    ResultEntity getMusicLike(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    );

    // 搜索歌曲
    @GetMapping("/service/music/queryMusic")
    ResultEntity queryMusic(
            @RequestParam("songName") String songName,
            @RequestParam("authorName") String authorName,
            @RequestParam("albumName") String albumName,
            @RequestParam("language") String language,
            @RequestParam("publishStart") Date publishStart,
            @RequestParam("label") String label,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize
    );

    @GetMapping("/service/music/getFavoriteDirectory")
    ResultEntity getFavoriteDirectory(
            @RequestParam(name = "musicId",required = true) Long musicId,
            @RequestHeader("X-User-Id") String userId
    );

    // 查询收藏夹音乐
    @GetMapping("/service/music/getMusicListByFavoriteId")
    ResultEntity getMusicListByFavoriteId(
            @RequestParam(name = "favoriteId",required = false) String favoriteId,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    );

    @GetMapping("/service/music/getMusicListByAuthor")
    ResultEntity getMusicListByAuthor(
            @RequestParam(name = "authorId",required = false) int authorId,
            @RequestParam(name = "authorName",required = false) String authorName,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    );
}