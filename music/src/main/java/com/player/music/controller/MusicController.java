package com.player.music.controller;


import com.player.common.entity.ResultEntity;
import com.player.common.utils.HttpUtils;
import com.player.music.entity.MusicFavoriteDirectoryEntity;
import com.player.music.entity.MusicFavoriteEntity;
import com.player.music.entity.MusicRecordEntity;
import com.player.music.service.IMusicService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/service")
public class MusicController {
    @Autowired
    private IMusicService musicService;

    //    @ApiOperation("获取搜索框默认推荐音乐")
    @GetMapping("/music/getKeywordMusic")
    public ResultEntity getKeywordMusic(HttpServletRequest request,@RequestHeader("X-User-Id") String userId) {
        return musicService.getKeywordMusic(HttpUtils.getFullRequestPath(request));
    }

    //    @ApiOperation("获取音乐分类")
    @GetMapping("/music/getMusicClassify")
    public ResultEntity getClassifyMusic(HttpServletRequest request) {
        return musicService.getMusicClassify(HttpUtils.getFullRequestPath(request));
    }

    //    @ApiOperation("获取推荐音乐列表,isRedis表示是否从redis中获取数据")
    @GetMapping("/music/getMusicListByClassifyId")
    public ResultEntity getMusicListByClassifyId(
            HttpServletRequest request,
            @RequestParam(name = "classifyId",required = true) int classifyId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize,
            @RequestParam(name = "isRedis",defaultValue = "0",required = false) int isRedis,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.getMusicListByClassifyId(HttpUtils.getFullRequestPath(request), classifyId, pageNum, pageSize, isRedis != 0, userId);
    }

    //    @ApiOperation("获取歌手")
    @GetMapping("/music/getMusicAuthorListByCategoryId")
    public ResultEntity getMusicAuthorListByCategoryId(
            HttpServletRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize,
            @RequestParam(name = "categoryId",required = true) int categoryId
    ) {
        return musicService.getMusicAuthorListByCategoryId(HttpUtils.getFullRequestPath(request),userId, categoryId, pageNum, pageSize);
    }

    //    @ApiOperation("获取歌手专辑")
    @GetMapping("/music/getMusicListByAuthorId")
    public ResultEntity getMusicListByAuthorId(
            HttpServletRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize,
            @RequestParam(name = "authorId",required = true) int authorId
    ) {
        return musicService.getMusicListByAuthorId(HttpUtils.getFullRequestPath(request),userId, authorId, pageNum, pageSize);
    }

    //    @ApiOperation("获取我关注的歌手")
    @GetMapping("/music/getFavoriteAuthor")
    public ResultEntity getFavoriteAuthor(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    ) {
        return musicService.getFavoriteAuthor(userId,pageNum,pageSize);
    }

    //    @ApiOperation("获取我关注的歌手")
    @PostMapping("/music/insertFavoriteAuthor/{authorId}")
    public ResultEntity insertFavoriteAuthor(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable(name = "authorId",required = true) int authorId
    ) {
        return musicService.insertFavoriteAuthor(userId,authorId);
    }

    //    @ApiOperation("获取我关注的歌手")
    @DeleteMapping("/music/deleteFavoriteAuthor/{authorId}")
    public ResultEntity deleteFavoriteAuthor(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable(name = "authorId",required = true) int authorId
    ) {
        return musicService.deleteFavoriteAuthor(userId,authorId);
    }

    //    @ApiOperation("获取最近播放的歌曲")
    @GetMapping("/music/getMusicRecord")
    public ResultEntity getMusicRecord(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    ) {
        return musicService.getMusicRecord(userId,pageNum,pageSize);
    }

    //    @ApiOperation("插入播放记录")
    @PostMapping("/music/insertMusicRecord")
    public ResultEntity insertMusicRecord(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody MusicRecordEntity musicRecordEntity
    ) {
        return musicService.insertMusicRecord(userId,musicRecordEntity);
    }

    //    @ApiOperation("插入音乐收藏")
    @PostMapping("/music/insertMusicLike/{id}")
    public ResultEntity insertMusicLike(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("id") int id
    ) {
        return musicService.insertMusicLike(userId,id);
    }

    //    @ApiOperation("删除音乐收藏")
    @DeleteMapping("/music/deleteMusicLike/{id}")
    public ResultEntity deleteMusicLike(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("id") int id
    ) {
        return musicService.deleteMusicLike(userId,id);
    }

    //    @ApiOperation("查询音乐收藏")
    @GetMapping("/music/getMusicLike")
    public ResultEntity getMusicLike(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    ) {
        return musicService.getMusicLike(userId,pageNum,pageSize);
    }

    //    @ApiOperation("查询音乐收藏")
    @GetMapping("/music/searchMusic")
    public ResultEntity searchMusic(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "keyword",required = true) String keyword,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    ) {
        return musicService.searchMusic(userId,keyword,pageNum,pageSize);
    }

    //    @ApiOperation("获取歌手分类")
    @GetMapping("/music/getMusicAuthorCategory")
    public ResultEntity getMusicAuthorCategory(
            HttpServletRequest request
    ) {
        return musicService.getMusicAuthorCategory(HttpUtils.getFullRequestPath(request));
    }

    //    @ApiOperation("查询音乐收藏")
    @GetMapping("/music/insertMusicRecord")
    public ResultEntity insertMusicRecord(
            HttpServletRequest request
    ) {
        return musicService.getMusicAuthorCategory(HttpUtils.getFullRequestPath(request));
    }

    //    @ApiOperation("查询收藏夹列表")
    @GetMapping("/music/getFavoriteDirectory")
    public ResultEntity getFavoriteDirectory(
            @RequestParam(name = "musicId",required = true) Long musicId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.getFavoriteDirectory(userId,musicId);
    }

    //    @ApiOperation("创建收藏夹")
    @PostMapping("/music/insertFavoriteDirectory")
    public ResultEntity insertFavoriteDirectory(
            @RequestBody MusicFavoriteDirectoryEntity favoriteDirectoryEntity,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.insertFavoriteDirectory(userId,favoriteDirectoryEntity);
    }

    //    @ApiOperation("删除收藏夹")
    @DeleteMapping("/music/deleteFavoriteDirectory/{favoriteId}")
    public ResultEntity deleteFavoriteDirectory(
            @PathVariable("favoriteId") Long favoriteId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.deleteFavoriteDirectory(userId,favoriteId);
    }

    //    @ApiOperation("查询收藏夹音乐")
    @GetMapping("/music/getMusicListByFavoriteId")
    public ResultEntity getMusicListByFavoriteId(
            @RequestParam(name = "favoriteId",required = true) Long favoriteId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.getMusicListByFavoriteId(userId,favoriteId,pageNum,pageSize);
    }

    //    @ApiOperation("更新收藏夹名称")
    @PutMapping("/music/updateFavoriteDirectory")
    public ResultEntity updateFavoriteDirectory(
            @RequestBody MusicFavoriteDirectoryEntity favoriteDirectoryEntity,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.updateFavoriteDirectory(userId,favoriteDirectoryEntity.getId(),favoriteDirectoryEntity.getName());
    }

    //    @ApiOperation("查询音乐收藏")
    @GetMapping("/music/isMusicFavorite/{musicId}")
    public ResultEntity isMusicFavorite(
            @PathVariable("musicId") Long musicId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.isMusicFavorite(userId,musicId);
    }

    //    @ApiOperation("添加音乐收藏夹")
    @PostMapping("/music/insertMusicFavorite/{musicId}")
    public ResultEntity insertMusicFavorite(
            @PathVariable("musicId") Long musicId,
            @RequestBody List<MusicFavoriteEntity> musicFavoriteEntityList,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.insertMusicFavorite(userId,musicId,musicFavoriteEntityList);
    }
}

