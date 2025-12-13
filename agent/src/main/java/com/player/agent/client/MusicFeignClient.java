package com.player.agent.client;

import com.player.common.entity.ResultEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "music-service") // 对应 music 模块的 spring.application.name
public interface MusicFeignClient {

    @GetMapping("/service/music/getMusicRecord")
    ResultEntity getMusicRecord(
            @RequestHeader("X-User-Id") String userId,
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
    @GetMapping("/service/music/searchMusic")
    ResultEntity searchMusic(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "keyword",required = true) String keyword,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    );

    @GetMapping("/service/music/getFavoriteDirectory")
    ResultEntity getFavoriteDirectory(
            @RequestParam(name = "musicId",required = true) Long musicId,
            @RequestHeader("X-User-Id") String userId
    );

    // 查询收藏夹音乐
    @GetMapping("/service/music/getMusicListByFavoriteId")
    ResultEntity getMusicListByFavoriteId(
            @RequestParam(name = "favoriteId",required = true) Long favoriteId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize,
            @RequestHeader("X-User-Id") String userId
    );
}