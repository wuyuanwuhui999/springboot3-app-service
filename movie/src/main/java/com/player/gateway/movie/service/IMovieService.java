package com.player.gateway.movie.service;

import com.player.gateway.common.entity.ResultEntity;

public interface IMovieService {

    ResultEntity findClassify(String redisKey);

    ResultEntity getKeyWord(String classify,String redisKey);

    ResultEntity getUserMsg(String userId);

    ResultEntity getAllCategoryByClassify(String classsify,String redisKey);

    ResultEntity getAllCategoryListByPageName(String pageName,String redisKey);

    ResultEntity getCategoryList(String classify, String category,String redisKey);

    ResultEntity getTopMovieList(String classify, String category,String redisKey);

    ResultEntity search(String classify, String category, String label,String star,String director,String keyword,int pageNum,int pageSize,String redisKey);

    ResultEntity getStar(Long movieId,String redisKey);

    ResultEntity getMovieUrl(Long movieId,String redisKey);

    ResultEntity getPlayRecord(String userId,int pageNum,int pageSize);

    ResultEntity savePlayRecord(int movieId,String userId);

    ResultEntity getViewRecord(String userId,int pageNum,int pageSize);

    ResultEntity saveViewRecord(int movieId,String userId);

    ResultEntity getFavoriteList(String userId,int pageNum,int pageSize);

    ResultEntity saveFavorite(int movieId,String userId);

    ResultEntity deleteFavorite(int movieId,String userId);

    ResultEntity isFavorite(Long movieId, String userId);

    ResultEntity getYourLikes(String labels,String classify,String redisKey);

    ResultEntity getRecommend(String classify,String redisKey);

    ResultEntity getMovieDetail(int movieId);

    ResultEntity getMovieListByType(String types,String classify,String redisKey);

    ResultEntity getSearchHistory(String userId,int pageNum,int pageSize);

}
