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
    public ResultEntity getKeywordMusic(HttpServletRequest request,@RequestHeader("Authorization") String token) {
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
            @RequestHeader(required = false, value = "Authorization") String token
    ) {
        return musicService.getMusicListByClassifyId(HttpUtils.getFullRequestPath(request), classifyId, pageNum, pageSize, isRedis != 0, token);
    }

    //    @ApiOperation("获取歌手")
    @GetMapping("/music/getMusicAuthorListByCategoryId")
    public ResultEntity getMusicAuthorListByCategoryId(
            HttpServletRequest request,
            @RequestHeader(value = "Authorization",required = false) String token,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize,
            @RequestParam(name = "categoryId",required = true) int categoryId
    ) {
        return musicService.getMusicAuthorListByCategoryId(HttpUtils.getFullRequestPath(request),token, categoryId, pageNum, pageSize);
    }

    //    @ApiOperation("获取歌手专辑")
    @GetMapping("/music/getMusicListByAuthorId")
    public ResultEntity getMusicListByAuthorId(
            HttpServletRequest request,
            @RequestHeader(value = "Authorization",required = false) String token,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize,
            @RequestParam(name = "authorId",required = true) int authorId
    ) {
        return musicService.getMusicListByAuthorId(HttpUtils.getFullRequestPath(request),token, authorId, pageNum, pageSize);
    }

    //    @ApiOperation("获取我关注的歌手")
    @GetMapping("/music/getFavoriteAuthor")
    public ResultEntity getFavoriteAuthor(
            @RequestHeader("Authorization") String token,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    ) {
        return musicService.getFavoriteAuthor(token,pageNum,pageSize);
    }

    //    @ApiOperation("获取我关注的歌手")
    @PostMapping("/music/insertFavoriteAuthor/{authorId}")
    public ResultEntity insertFavoriteAuthor(
            @RequestHeader("Authorization") String token,
            @PathVariable(name = "authorId",required = true) int authorId
    ) {
        return musicService.insertFavoriteAuthor(token,authorId);
    }

    //    @ApiOperation("获取我关注的歌手")
    @DeleteMapping("/music/deleteFavoriteAuthor/{authorId}")
    public ResultEntity deleteFavoriteAuthor(
            @RequestHeader("Authorization") String token,
            @PathVariable(name = "authorId",required = true) int authorId
    ) {
        return musicService.deleteFavoriteAuthor(token,authorId);
    }

    //    @ApiOperation("获取最近播放的歌曲")
    @GetMapping("/music/getMusicRecord")
    public ResultEntity getMusicRecord(
            @RequestHeader("Authorization") String token,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    ) {
        return musicService.getMusicRecord(token,pageNum,pageSize);
    }

    //    @ApiOperation("插入播放记录")
    @PostMapping("/music/insertMusicRecord")
    public ResultEntity insertMusicRecord(
            @RequestHeader("Authorization") String token,
            @RequestBody MusicRecordEntity musicRecordEntity
    ) {
        return musicService.insertMusicRecord(token,musicRecordEntity);
    }

    //    @ApiOperation("插入音乐收藏")
    @PostMapping("/music/insertMusicLike/{id}")
    public ResultEntity insertMusicLike(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") int id
    ) {
        return musicService.insertMusicLike(token,id);
    }

    //    @ApiOperation("删除音乐收藏")
    @DeleteMapping("/music/deleteMusicLike/{id}")
    public ResultEntity deleteMusicLike(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") int id
    ) {
        return musicService.deleteMusicLike(token,id);
    }

    //    @ApiOperation("查询音乐收藏")
    @GetMapping("/music/getMusicLike")
    public ResultEntity getMusicLike(
            @RequestHeader("Authorization") String token,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    ) {
        return musicService.getMusicLike(token,pageNum,pageSize);
    }

    //    @ApiOperation("查询音乐收藏")
    @GetMapping("/music/searchMusic")
    public ResultEntity searchMusic(
            @RequestHeader(name = "Authorization",required = false) String token,
            @RequestParam(name = "keyword",required = true) String keyword,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize
    ) {
        return musicService.searchMusic(token,keyword,pageNum,pageSize);
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
            @RequestHeader(name = "Authorization",required = true) String token
    ) {
        return musicService.getFavoriteDirectory(token,musicId);
    }

    //    @ApiOperation("创建收藏夹")
    @PostMapping("/music/insertFavoriteDirectory")
    public ResultEntity insertFavoriteDirectory(
            @RequestBody MusicFavoriteDirectoryEntity favoriteDirectoryEntity,
            @RequestHeader(name = "Authorization",required = false) String token
    ) {
        return musicService.insertFavoriteDirectory(token,favoriteDirectoryEntity);
    }

    //    @ApiOperation("删除收藏夹")
    @DeleteMapping("/music/deleteFavoriteDirectory/{favoriteId}")
    public ResultEntity deleteFavoriteDirectory(
            @PathVariable("favoriteId") Long favoriteId,
            @RequestHeader(name = "Authorization",required = false) String token
    ) {
        return musicService.deleteFavoriteDirectory(token,favoriteId);
    }

    //    @ApiOperation("查询收藏夹音乐")
    @GetMapping("/music/getMusicListByFavoriteId")
    public ResultEntity getMusicListByFavoriteId(
            @RequestParam(name = "favoriteId",required = true) Long favoriteId,
            @RequestParam(name = "pageNum",required = true) int pageNum,
            @RequestParam(name = "pageSize",required = true) int pageSize,
            @RequestHeader(name = "Authorization",required = true) String token
    ) {
        return musicService.getMusicListByFavoriteId(token,favoriteId,pageNum,pageSize);
    }

    //    @ApiOperation("更新收藏夹名称")
    @PutMapping("/music/updateFavoriteDirectory")
    public ResultEntity updateFavoriteDirectory(
            @RequestBody MusicFavoriteDirectoryEntity favoriteDirectoryEntity,
            @RequestHeader(name = "Authorization",required = false) String token
    ) {
        return musicService.updateFavoriteDirectory(token,favoriteDirectoryEntity.getId(),favoriteDirectoryEntity.getName());
    }

    //    @ApiOperation("查询音乐收藏")
    @GetMapping("/music/isMusicFavorite/{musicId}")
    public ResultEntity isMusicFavorite(
            @PathVariable("musicId") Long musicId,
            @RequestHeader(name = "Authorization",required = false) String token
    ) {
        return musicService.isMusicFavorite(token,musicId);
    }

    //    @ApiOperation("添加音乐收藏夹")
    @PostMapping("/music/insertMusicFavorite/{musicId}")
    public ResultEntity insertMusicFavorite(
            @PathVariable("musicId") Long musicId,
            @RequestBody List<MusicFavoriteEntity> musicFavoriteEntityList,
            @RequestHeader(name = "Authorization",required = false) String token
    ) {
        return musicService.insertMusicFavorite(token,musicId,musicFavoriteEntityList);
    }
}

