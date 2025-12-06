package com.player.movie.controller;

import com.player.common.entity.ResultEntity;
import com.player.common.utils.HttpUtils;
import com.player.movie.service.IMovieService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/service")
@RestController
public class MovieController {
    @Autowired
    private IMovieService movieService;

    // 获取分类信息
    @GetMapping("/movie/findClassify")
    public ResultEntity findClassify(HttpServletRequest request) {
        String path = request.getRequestURI();
        return movieService.findClassify(path);
    }

    // 按照类型获取推荐影片
    @GetMapping("/movie/getKeyWord")
    public ResultEntity getKeyWord(
            @RequestParam("classify") String classify,
            HttpServletRequest request
    ) {
        return movieService.getKeyWord(classify, HttpUtils.getFullRequestPath(request));
    }

    // 查询当前用户的使用天数，关注数，观看记录数，浏览记录数
    @GetMapping("/movie/getUserMsg")
    public ResultEntity getUserMsg(@RequestHeader("X-User-Id") String userId) {
        return movieService.getUserMsg(userId);
    }

    // 按classify大类查询所有catory小类
    @GetMapping("/movie/getAllCategoryByClassify")
    public ResultEntity getAllCategoryByClassify(
            @RequestParam("classify") String classsify,
            HttpServletRequest request
    ) {
        return movieService.getAllCategoryByClassify(classsify,HttpUtils.getFullRequestPath(request));
    }

    // 按页面获取要展示的category小类
    @GetMapping("/movie/getAllCategoryListByPageName")
    public ResultEntity getAllCategoryListByPageName(
            @RequestParam("pageName") String pageName,
            HttpServletRequest request
    ) {
        return movieService.getAllCategoryListByPageName(pageName,HttpUtils.getFullRequestPath(request));
    }

    // 获取大类中的小类
    @GetMapping("/movie/getCategoryList")
    public ResultEntity getCategoryList(
            @RequestParam("classify") String classify,
            @RequestParam("category") String category,
            HttpServletRequest request
    ) {
        return movieService.getCategoryList(classify, category,HttpUtils.getFullRequestPath(request));
    }

    // 根据分类获取前20条数据,请求地地址
    @GetMapping("/movie/getTopMovieList")
    public ResultEntity getTopMovieList(
            @RequestParam("classify") String classify,
            @RequestParam(value = "category",required = false) String category,
            HttpServletRequest request
    ) {
        return movieService.getCategoryList(classify, category,HttpUtils.getFullRequestPath(request));
    }

    // 搜索,请求地地址
    @GetMapping(value = "/movie/search")
    public ResultEntity search(
            @RequestParam(required = false, value="classify") String classify,
            @RequestParam(required = false, value="category") String category,
            @RequestParam(required = false, value="label") String label,
            @RequestParam(required = false, value="star") String star,
            @RequestParam(required = false, value="director") String director,
            @RequestParam(required = false, value="keyword") String keyword,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize,
            HttpServletRequest request
    ) {
        return movieService.search(classify, category, label,star,director,keyword,pageNum,pageSize,HttpUtils.getFullRequestPath(request));
    }

    // 获取演员列表
    @GetMapping("/movie/getStar/{movieId}")
    public ResultEntity getStar(
            @PathVariable("movieId") Long movieId,
            HttpServletRequest request
    ) {
        return movieService.getStar(movieId,HttpUtils.getFullRequestPath(request));
    }

    // 获取演员列表
    @GetMapping("/movie/getMovieUrl")
    public ResultEntity getMovieUrl(
            @RequestParam("movieId") Long movieId,
            HttpServletRequest request
    ) {
        return movieService.getMovieUrl(movieId,HttpUtils.getFullRequestPath(request));
    }

    // 获取观看记录
    @GetMapping("/movie/getPlayRecord")
    public ResultEntity getPlayRecord(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("pageNum")int pageNum,
            @RequestParam("pageSize")int pageSize) {
        return movieService.getPlayRecord(userId,pageNum,pageSize);
    }

    // 获取播放记录
    @PostMapping("/movie/savePlayRecord/{movieId}")
    public ResultEntity savePlayRecord(@PathVariable("movieId") int movieId,@RequestHeader("X-User-Id") String userId) {
        return movieService.savePlayRecord(movieId,userId);
    }

    // 获取观看记录
    @GetMapping("/movie/getViewRecord")
    public ResultEntity getViewRecord(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("pageNum")int pageNum,
            @RequestParam("pageSize")int pageSize) {
        return movieService.getViewRecord(userId,pageNum,pageSize);
    }

    // 获取播放记录
    @PostMapping("/movie/saveViewRecord/{movieId}")
    public ResultEntity saveViewRecord(@PathVariable("movieId")int movieId,@RequestHeader("X-User-Id") String userId) {
        return movieService.saveViewRecord(movieId,userId);
    }

    // 获取观看记录,请求地地址
    @GetMapping("/movie/getFavoriteList")
    public ResultEntity getFavorite(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("pageNum")int pageNum,
            @RequestParam("pageSize")int pageSize) {
        return movieService.getFavoriteList(userId,pageNum,pageSize);
    }

    // 保存收藏记录
    @PostMapping("/movie/saveFavorite/{movieId}")
    public ResultEntity saveFavorite(@PathVariable("movieId") int movieId,@RequestHeader("X-User-Id") String userId) {
        return movieService.saveFavorite(movieId,userId);
    }

    // 删除收藏
    @DeleteMapping("/movie/deleteFavorite/{movieId}")
    public ResultEntity deleteFavorite(@PathVariable("movieId") int movieId,@RequestHeader("X-User-Id") String userId) {
        return movieService.deleteFavorite(movieId,userId);
    }

    // 查询是否已经收藏
    @GetMapping("/movie/isFavorite")
    public ResultEntity isFavorite(@RequestParam("movieId") Long movieId,@RequestHeader("X-User-Id") String userId) {
        return movieService.isFavorite(movieId,userId);
    }

    // 获取猜你想看的电影
    @GetMapping("/movie/getYourLikes")
    public ResultEntity getYourLikes(
            @RequestParam("labels") String labels,
            @RequestParam("classify") String classify,
            HttpServletRequest request
    ) {
        return movieService.getYourLikes(labels,classify,HttpUtils.getFullRequestPath(request));
    }

    // 获取推荐的电影
    @GetMapping("/movie/getRecommend")
    public ResultEntity getRecommend(@RequestParam("classify") String classify, HttpServletRequest request) {
        return movieService.getRecommend(classify, HttpUtils.getFullRequestPath(request));
    }

    // 获取电影详情
    @GetMapping("/movie/getMovieDetail/{movieId}")
    public ResultEntity getMovieDetail(@PathVariable("movieId") int movieId) {
        return movieService.getMovieDetail(movieId);
    }

    // 获取类型相似的电影
    @GetMapping("/movie/getMovieListByType")
    public ResultEntity getMovieListByType(
            @RequestParam("types") String types,
            @RequestParam("classify") String classify,
            HttpServletRequest request
    ) {
        return movieService.getMovieListByType(types,classify,HttpUtils.getFullRequestPath(request));
    }

    // 获取搜索历史
    @GetMapping("/movie/getSearchHistory")
    public ResultEntity getSearchHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize")int pageSize
    ) {
        return movieService.getSearchHistory(userId,pageNum,pageSize);
    }
}
