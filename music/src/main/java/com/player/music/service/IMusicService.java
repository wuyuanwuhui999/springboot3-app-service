package com.player.music.service;

import com.player.common.entity.ResultEntity;
import com.player.music.entity.MusicFavoriteDirectoryEntity;
import com.player.music.entity.MusicFavoriteEntity;
import com.player.music.entity.MusicRecordEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface IMusicService {
    ResultEntity getKeywordMusic(String redisKey);

    ResultEntity getMusicClassify(String redisKey);

    ResultEntity getMusicListByClassifyId(String redisKey, int classifyId, int pageNum, int pageSize, boolean isRedis, String token);

    ResultEntity getMusicAuthorListByCategoryId(String redisKey,String token, int categoryId, int pageNum, int pageSize);

    ResultEntity getMusicListByAuthor(String redisKey,String token, int authorId, int pageNum, int pageSize);

    ResultEntity getFavoriteAuthor(String token,int pageNum, int pageSize);

    ResultEntity insertFavoriteAuthor(String token,int authorId);

    ResultEntity deleteFavoriteAuthor(String token,int authorId);

    ResultEntity getMusicRecord(String token, Date startDate, Date endDate, int pageNum, int pageSize);

    @Transactional
    ResultEntity insertMusicRecord(String token, MusicRecordEntity myMusicRecordEntity);

    @Transactional
    ResultEntity insertMusicLike(String token,int musicId);

    @Transactional
    ResultEntity deleteMusicLike(String token,int id);

    ResultEntity getMusicLike(String token, int pageNum, int pageSize);

    ResultEntity searchMusic(String token,String keyword, int pageNum, int pageSize);

    ResultEntity queryMusic(String songName,String authorName,String albumName,String language,Date publishStart,String label,int pageNum,int pageSize);

    ResultEntity getMusicAuthorCategory(String redisKey);

    ResultEntity getFavoriteDirectory(String token,Long musicId);

    ResultEntity getMusicListByFavoriteId(String token,Long favoriteId,int pageNum,int pageSize);

    @Transactional
    ResultEntity insertFavoriteDirectory(String token, MusicFavoriteDirectoryEntity favoriteDirectoryEntity);

    @Transactional
    ResultEntity deleteFavoriteDirectory(String token, Long favoriteId);

    @Transactional
    ResultEntity updateFavoriteDirectory(String token, Long favoriteId,String name);

    @Transactional
    ResultEntity insertMusicFavorite(String token,Long musicId, List<MusicFavoriteEntity> myMusicFavoriteEntityList);

    ResultEntity isMusicFavorite(String token, Long musicId);

}
