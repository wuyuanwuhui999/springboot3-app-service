package com.player.music.mapper;

import com.player.common.entity.LogEntity;
import com.player.music.entity.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MusicMapper {
    MusicEntity getKeywordMusic();

    List<MusicEntity> getMusicClassify();

    List<MusicEntity> getMusicListByClassifyId(int classifyId,int start,int pageSize,String userId);

    Long getMusicTotalByClassifyId(int classifyId);

    List<MusicAuthorEntity> getMusicAuthorListByCategoryId(String userId, int categoryId, int start, int pageSize);

    Long getMusicAuthorTotal(int categoryId);

    List<MusicEntity> getMusicListByAuthorId(String userId,int authorId, int start, int pageSize);

    Long getMusicListByAuthorIdTotal(int authorId);

    List<MusicAuthorEntity> getFavoriteAuthor(String userId, int start, int pageSize);

    Long getFavoriteAuthorCount(String userId);

    Long insertFavoriteAuthor(String userId,int authorId);

    Long deleteFavoriteAuthor(String userId,int authorId);

    List<MusicEntity> getMusicRecord(String userId, int start, int pageSize);

    Long getMusicRecordCount(String userId);

    Long insertMusicRecord(MusicRecordEntity myMusicRecordEntity);

    Long insertMusicLike(String userId,int musicId);

    Long deleteMusicLike(String userId,int musicId);

    List<MusicEntity> getMusicLike(String userId, int start, int pageSize);

    Long getMusicLikeCount(String userId);

    List<MusicEntity> searchMusic(String userId,String keyword, int start, int pageSize);

    Long searchMusicCount(String keyword);

    List<MusicAuthorCategoryEntity> getMusicAuthorCategory();

    List<MusicFavoriteDirectoryEntity> getFavoriteDirectory(String userId, Long musicId);

    List<MusicEntity> getMusicListByFavoriteId(String userId,Long favoriteId,int start,int pageSize);

    Long getMusicCountByFavoriteId(Long favoriteId);

    Long deleteFavoriteDirectory(String userId, Long favoriteId);

    Long updateFavoriteDirectory(String userId, Long favoriteId, String name);

    Long insertFavoriteDirectory(MusicFavoriteDirectoryEntity favoriteDirectoryEntity);

    MusicFavoriteDirectoryEntity getFavoriteDirectoryById(Long id);

    Long insertMusicFavorite(List<MusicFavoriteEntity> myMusicFavoriteEntityList);

    Long deleteMusicFavorite(String userId,Long musicId);

    Long deleteMusicFavoriteByFavoriteId(String userId,Long favoriteId);

    Long isMusicFavorite(String userId,Long musicId);

    Long saveLog(LogEntity logEntity);
}
