package com.player.music.controller;

import com.player.common.entity.ResultEntity;
import com.player.music.entity.MusicFavoriteDirectoryEntity;
import com.player.music.entity.MusicFavoriteEntity;
import com.player.music.entity.MusicRecordEntity;
import com.player.music.service.IMusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/service")
public class MusicController {
    @Autowired
    private IMusicService musicService;

    // 获取搜索框默认推荐音乐
    @Cacheable(value = "music", key = "'keywordMusic'")
    @GetMapping("/music/getKeywordMusic")
    public ResultEntity getKeywordMusic() {
        return musicService.getKeywordMusic();
    }

    // 获取音乐分类
    @Cacheable(value = "music", key = "'classifyMusic'")
    @GetMapping("/music/getMusicClassify")
    public ResultEntity getClassifyMusic() {
        return musicService.getMusicClassify();
    }

    // 获取推荐音乐列表,isRedis表示是否从redis中获取数据
    @Cacheable(value = "music", key = "'musicList:' + #classifyId + ':' + #pageNum + ':' + #pageSize + ':' + #isRedis + ':' + #userId")
    @GetMapping("/music/getMusicListByClassifyId")
    public ResultEntity getMusicListByClassifyId(
            @RequestParam(name = "classifyId", required = true) int classifyId,
            @RequestParam(name = "pageNum", required = true) int pageNum,
            @RequestParam(name = "pageSize", required = true) int pageSize,
            @RequestParam(name = "isRedis", defaultValue = "0", required = false) int isRedis,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.getMusicListByClassifyId(classifyId, pageNum, pageSize, isRedis != 0, userId);
    }

    //  根据分类id获取歌手
    @Cacheable(value = "music", key = "'authorList:' + #categoryId + ':' + #pageNum + ':' + #pageSize + ':' + #userId")
    @GetMapping("/music/getMusicAuthorListByCategoryId")
    public ResultEntity getMusicAuthorListByCategoryId(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum", required = true) int pageNum,
            @RequestParam(name = "pageSize", required = true) int pageSize,
            @RequestParam(name = "categoryId", required = true) int categoryId
    ) {
        return musicService.getMusicAuthorListByCategoryId(userId, categoryId, pageNum, pageSize);
    }

    // 根据歌手id获取歌手专辑
    @Cacheable(value = "music", key = "'musicByAuthor:' + #authorId + ':' + #authorName + ':' + #pageNum + ':' + #pageSize + ':' + #userId")
    @GetMapping("/music/getMusicListByAuthor")
    public ResultEntity getMusicListByAuthor(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum", required = true) int pageNum,
            @RequestParam(name = "pageSize", required = true) int pageSize,
            @RequestParam(name = "authorName", required = false) String authorName,
            @RequestParam(name = "authorId", required = false) int authorId
    ) {
        return musicService.getMusicListByAuthor(userId, authorId, authorName, pageNum, pageSize);
    }

    // 获取用户收藏歌手
    @Cacheable(value = "music", key = "'favoriteAuthor:' + #userId + ':' + #pageNum + ':' + #pageSize")
    @GetMapping("/music/getFavoriteAuthor")
    public ResultEntity getFavoriteAuthor(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum", required = true) int pageNum,
            @RequestParam(name = "pageSize", required = true) int pageSize
    ) {
        return musicService.getFavoriteAuthor(userId, pageNum, pageSize);
    }

    // 插入收藏歌手
    @CacheEvict(value = "music", allEntries = true)
    @PostMapping("/music/insertFavoriteAuthor/{authorId}")
    public ResultEntity insertFavoriteAuthor(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable(name = "authorId", required = true) int authorId
    ) {
        return musicService.insertFavoriteAuthor(userId, authorId);
    }

    // 删除收藏歌手
    @CacheEvict(value = "music", allEntries = true)
    @DeleteMapping("/music/deleteFavoriteAuthor/{authorId}")
    public ResultEntity deleteFavoriteAuthor(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable(name = "authorId", required = true) int authorId
    ) {
        return musicService.deleteFavoriteAuthor(userId, authorId);
    }

    // 查询播放记录
    @Cacheable(value = "music", key = "'musicRecord:' + #userId + ':' + #startDate + ':' + #endDate + ':' + #pageNum + ':' + #pageSize")
    @GetMapping("/music/getMusicRecord")
    public ResultEntity getMusicRecord(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "startDate", required = false) Date startDate,
            @RequestParam(name = "endDate", required = false) Date endDate,
            @RequestParam(name = "pageNum", required = true) int pageNum,
            @RequestParam(name = "pageSize", required = true) int pageSize
    ) {
        return musicService.getMusicRecord(userId, startDate, endDate, pageNum, pageSize);
    }

    // 插入播放记录
    @CacheEvict(value = "music", allEntries = true)
    @PostMapping("/music/insertMusicRecord")
    public ResultEntity insertMusicRecord(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody MusicRecordEntity musicRecordEntity
    ) {
        return musicService.insertMusicRecord(userId, musicRecordEntity);
    }

    // 插入音乐收藏
    @CacheEvict(value = "music", allEntries = true)
    @PostMapping("/music/insertMusicLike/{id}")
    public ResultEntity insertMusicLike(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("id") int id
    ) {
        return musicService.insertMusicLike(userId, id);
    }

    // 删除音乐收藏
    @CacheEvict(value = "music", allEntries = true)
    @DeleteMapping("/music/deleteMusicLike/{id}")
    public ResultEntity deleteMusicLike(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable("id") int id
    ) {
        return musicService.deleteMusicLike(userId, id);
    }

    // 查询点赞的音乐
    @Cacheable(value = "music", key = "'musicLike:' + #userId + ':' + #pageNum + ':' + #pageSize")
    @GetMapping("/music/getMusicLike")
    public ResultEntity getMusicLike(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "pageNum", required = true) int pageNum,
            @RequestParam(name = "pageSize", required = true) int pageSize
    ) {
        return musicService.getMusicLike(userId, pageNum, pageSize);
    }

    // 搜素音乐
    @Cacheable(value = "music", key = "'searchMusic:' + #userId + ':' + #keyword + ':' + #pageNum + ':' + #pageSize")
    @GetMapping("/music/searchMusic")
    public ResultEntity searchMusic(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(name = "keyword", required = true) String keyword,
            @RequestParam(name = "pageNum", required = true) int pageNum,
            @RequestParam(name = "pageSize", required = true) int pageSize
    ) {
        return musicService.searchMusic(userId, keyword, pageNum, pageSize);
    }

    // 搜素音乐
    @Cacheable(value = "music", key = "'queryMusic:' + #songName + ':' + #authorName + ':' + #albumName + ':' + #language + ':' + #publishStart + ':' + #label + ':' + #pageNum + ':' + #pageSize")
    @GetMapping("/music/queryMusic")
    public ResultEntity queryMusic(
            @RequestParam(name = "songName", required = false) String songName,
            @RequestParam(name = "authorName", required = false) String authorName,
            @RequestParam(name = "albumName", required = false) String albumName,
            @RequestParam(name = "language", required = false) String language,
            @RequestParam(name = "publishStart", required = false) Date publishStart,
            @RequestParam(name = "label", required = false) String label,
            @RequestParam(name = "pageNum", required = true) int pageNum,
            @RequestParam(name = "pageSize", required = true) int pageSize
    ) {
        return musicService.queryMusic(songName, authorName, albumName, language, publishStart, label, pageNum, pageSize);
    }

    // 获取歌手分类
    @Cacheable(value = "music", key = "'authorCategory'")
    @GetMapping("/music/getMusicAuthorCategory")
    public ResultEntity getMusicAuthorCategory() {
        return musicService.getMusicAuthorCategory();
    }

    // 查询收藏夹列表
    @Cacheable(value = "music", key = "'favoriteDirectory:' + #userId + ':' + #musicId")
    @GetMapping("/music/getFavoriteDirectory")
    public ResultEntity getFavoriteDirectory(
            @RequestParam(name = "musicId", required = true) Long musicId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.getFavoriteDirectory(userId, musicId);
    }

    // 创建收藏夹
    @CacheEvict(value = "music", allEntries = true)
    @PostMapping("/music/insertFavoriteDirectory")
    public ResultEntity insertFavoriteDirectory(
            @RequestBody MusicFavoriteDirectoryEntity favoriteDirectoryEntity,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.insertFavoriteDirectory(userId, favoriteDirectoryEntity);
    }

    // 删除收藏夹
    @CacheEvict(value = "music", allEntries = true)
    @DeleteMapping("/music/deleteFavoriteDirectory/{favoriteId}")
    public ResultEntity deleteFavoriteDirectory(
            @PathVariable("favoriteId") Long favoriteId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.deleteFavoriteDirectory(userId, favoriteId);
    }

    // 查询收藏夹音乐
    @Cacheable(value = "music", key = "'musicListByFavorite:' + #userId + ':' + #favoriteId + ':' + #pageNum + ':' + #pageSize")
    @GetMapping("/music/getMusicListByFavoriteId")
    public ResultEntity getMusicListByFavoriteId(
            @RequestParam(name = "favoriteId", required = false) Long favoriteId,
            @RequestParam(name = "pageNum", required = true) int pageNum,
            @RequestParam(name = "pageSize", required = true) int pageSize,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.getMusicListByFavoriteId(userId, favoriteId, pageNum, pageSize);
    }

    // 更新收藏夹名称
    @CacheEvict(value = "music", allEntries = true)
    @PutMapping("/music/updateFavoriteDirectory")
    public ResultEntity updateFavoriteDirectory(
            @RequestBody MusicFavoriteDirectoryEntity favoriteDirectoryEntity,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.updateFavoriteDirectory(userId, favoriteDirectoryEntity.getId(), favoriteDirectoryEntity.getName());
    }

    // 查询音乐收藏
    @Cacheable(value = "music", key = "'isMusicFavorite:' + #userId + ':' + #musicId")
    @GetMapping("/music/isMusicFavorite/{musicId}")
    public ResultEntity isMusicFavorite(
            @PathVariable("musicId") Long musicId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.isMusicFavorite(userId, musicId);
    }

    // 添加音乐收藏夹
    @CacheEvict(value = "music", allEntries = true)
    @PostMapping("/music/insertMusicFavorite/{musicId}")
    public ResultEntity insertMusicFavorite(
            @PathVariable("musicId") Long musicId,
            @RequestBody List<MusicFavoriteEntity> musicFavoriteEntityList,
            @RequestHeader("X-User-Id") String userId
    ) {
        return musicService.insertMusicFavorite(userId, musicId, musicFavoriteEntityList);
    }
}