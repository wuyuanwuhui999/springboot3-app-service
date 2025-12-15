package com.player.agent.tool;

import com.player.agent.client.MusicFeignClient;
import com.player.agent.mapper.AgentMapper;
import com.player.common.entity.MusicEntity;
import com.player.common.entity.ResultEntity;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AgentTool {
    @Autowired
    private AgentMapper agentMapper;

    @Autowired
    private MusicFeignClient musicFeignClient;

    @Tool(description = "根据多种条件查询音乐，支持模糊查询")
    public List<MusicEntity> queryMusic(
            @ToolParam(description = "歌曲名称（可选，支持模糊匹配）") String songName,
            @ToolParam(description = "歌手名称（可选，支持模糊匹配）") String authorName,
            @ToolParam(description = "专辑名称（可选，支持模糊匹配）") String albumName,
            @ToolParam(description = "语言（可选，如：中文、英语、日语等）") String language,
            @ToolParam(description = "发布时间范围开始（可选，格式：yyyy-MM-dd）") Date publishStart,
            @ToolParam(description = "标签（可选，如：流行、摇滚、古典等）") String label,
            @ToolParam(description = "页码，从1开始，必须") int pageNum,
            @ToolParam(description = "每页条数，必须") int pageSize
    ) {
        try {
            ResultEntity result = musicFeignClient.queryMusic(
                    songName, authorName, albumName, language,
                    publishStart, label, pageNum, pageSize
            );
            if (result != null && result.getData() instanceof List) {
                return (List<MusicEntity>) result.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Tool(description = "查询用户收藏的歌曲")
    public List<MusicEntity> getMusicListByFavoriteId(
            @ToolParam(description = "用户ID") String userId,
            @ToolParam(description = "页码，从1开始") int pageNum,
            @ToolParam(description = "每页条数") int pageSize,
            @ToolParam(description = "收藏夹Id（可选），省略时查询所有收藏夹的歌曲") String favoriteId
    ) {
        try {
            ResultEntity result = musicFeignClient.getMusicListByFavoriteId(
                    favoriteId, userId, pageNum, pageSize
            );
            if (result != null && result.getData() instanceof List) {
                return (List<MusicEntity>) result.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Tool(description = "获取用户播放历史")
    public List<MusicEntity> getMusicRecord(
            @ToolParam(description = "用户ID") String userId,
            @ToolParam(description = "开始时间（可选，格式：yyyy-MM-dd）") Date startDate,
            @ToolParam(description = "结束时间（可选，格式：yyyy-MM-dd）") Date endDate,
            @ToolParam(description = "页码，从1开始") int pageNum,
            @ToolParam(description = "每页条数，默认100") int pageSize
    ) {
        try {
            ResultEntity result = musicFeignClient.getMusicRecord(
                    userId, startDate, endDate, pageNum, pageSize
            );
            if (result != null && result.getData() instanceof List) {
                return (List<MusicEntity>) result.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Tool(description = "查询歌手的所有歌曲")
    public List<MusicEntity> getMusicListByAuthor(
            @ToolParam(description = "用户ID") String userId,
            @ToolParam(description = "页码，从1开始") int pageNum,
            @ToolParam(description = "每页条数") int pageSize,
            @ToolParam(description = "歌手名称（可选）") String authorName
    ) {
        try {
            ResultEntity result = musicFeignClient.getMusicListByAuthor(
                    0, authorName, userId, pageNum, pageSize
            );
            if (result != null && result.getData() instanceof List) {
                return (List<MusicEntity>) result.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Tool(description = "查询用户喜欢的音乐")
    public List<MusicEntity> getMusicLike(
            @ToolParam(description = "用户ID") String userId,
            @ToolParam(description = "页码，从1开始") int pageNum,
            @ToolParam(description = "每页条数") int pageSize
    ) {
        try {
            ResultEntity result = musicFeignClient.getMusicLike(userId, pageNum, pageSize);
            if (result != null && result.getData() instanceof List) {
                return (List<MusicEntity>) result.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Tool(description = "查询用户收藏的歌手")
    public List<Map<String, Object>> getFavoriteAuthor(
            @ToolParam(description = "用户ID") String userId,
            @ToolParam(description = "页码，从1开始") int pageNum,
            @ToolParam(description = "每页条数") int pageSize
    ) {
        try {
            ResultEntity result = musicFeignClient.getFavoriteAuthor(userId, pageNum, pageSize);
            if (result != null && result.getData() instanceof List) {
                return (List<Map<String, Object>>) result.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Tool(description = "智能搜索音乐 - 根据用户自然语言描述搜索音乐")
    public String smartMusicSearch(
            @ToolParam(description = "用户自然语言查询，如：'周杰伦的流行歌曲' 或 '最近发布的摇滚音乐'") String userQuery,
            @ToolParam(description = "用户ID（用于个性化推荐）") String userId
    ) {
        try {
            // 这里可以调用AI分析用户意图，并转换为具体的查询条件
            // 暂时实现一个简单的关键词匹配逻辑
            String analysis = analyzeUserQuery(userQuery);

            // 根据分析结果调用相应的工具
            if (analysis.contains("收藏")) {
                List<MusicEntity> favoriteSongs = getMusicListByFavoriteId(userId, 1, 20, null);
                return formatMusicList(favoriteSongs, "您的收藏歌曲");
            } else if (analysis.contains("历史") || analysis.contains("听过")) {
                List<MusicEntity> history = getMusicRecord(userId, null, null, 1, 20);
                return formatMusicList(history, "您的播放历史");
            } else if (analysis.contains("喜欢")) {
                List<MusicEntity> likes = getMusicLike(userId, 1, 20);
                return formatMusicList(likes, "您喜欢的歌曲");
            } else if (analysis.contains("歌手") || analysis.contains("周杰伦") || analysis.contains("林俊杰")) {
                // 提取歌手名
                String authorName = extractAuthorName(userQuery);
                if (authorName != null) {
                    List<MusicEntity> songs = getMusicListByAuthor(userId, 1, 20, authorName);
                    return formatMusicList(songs, authorName + " 的歌曲");
                }
            }

            // 默认调用普通查询
            String language = extractLanguage(userQuery);
            String label = extractLabel(userQuery);
            List<MusicEntity> result = queryMusic(null, null, null, language, null, label, 1, 20);
            return formatMusicList(result, "搜索结果");

        } catch (Exception e) {
            e.printStackTrace();
            return "搜索过程中出现错误：" + e.getMessage();
        }
    }

    @Tool(description = "推荐我可能喜欢的歌曲")
    public String recommendForUser(
            @ToolParam(description = "用户ID") String userId,
            @ToolParam(description = "推荐数量，默认10首") Integer count
    ) {
        if (count == null) count = 10;

        try {
            // 1. 获取用户历史偏好
            List<MusicEntity> historyList = getMusicRecord(userId, null, null, 1, 100);
            List<MusicEntity> favoriteList = getMusicListByFavoriteId(userId, 1, 100, null);
            List<MusicEntity> likeList = getMusicLike(userId, 1, 100);

            // 2. 分析偏好特征
            Map<String, Integer> authorPref = new HashMap<>();
            Map<String, Integer> languagePref = new HashMap<>();
            Map<String, Integer> labelPref = new HashMap<>();

            analyzePreferences(historyList, authorPref, languagePref, labelPref);
            analyzePreferences(favoriteList, authorPref, languagePref, labelPref);
            analyzePreferences(likeList, authorPref, languagePref, labelPref);

            // 3. 根据偏好推荐
            List<MusicEntity> recommendations = new ArrayList<>();

            // 按歌手偏好推荐
            String topAuthor = getTopPreference(authorPref);
            if (topAuthor != null) {
                List<MusicEntity> authorSongs = getMusicListByAuthor(userId, 1, 5, topAuthor);
                recommendations.addAll(authorSongs);
            }

            // 按语言偏好推荐
            String topLanguage = getTopPreference(languagePref);
            if (topLanguage != null && recommendations.size() < count) {
                List<MusicEntity> languageSongs = queryMusic(null, null, null, topLanguage, null, null, 1, count - recommendations.size());
                recommendations.addAll(languageSongs);
            }

            // 按标签偏好推荐
            String topLabel = getTopPreference(labelPref);
            if (topLabel != null && recommendations.size() < count) {
                List<MusicEntity> labelSongs = queryMusic(null, null, null, null, null, topLabel, 1, count - recommendations.size());
                recommendations.addAll(labelSongs);
            }

            // 去重
            Set<Long> addedIds = new HashSet<>();
            List<MusicEntity> uniqueRecommendations = new ArrayList<>();
            for (MusicEntity music : recommendations) {
                if (!addedIds.contains(music.getId()) && uniqueRecommendations.size() < count) {
                    addedIds.add(music.getId());
                    uniqueRecommendations.add(music);
                }
            }

            return formatMusicList(uniqueRecommendations, "为您推荐的歌曲");

        } catch (Exception e) {
            e.printStackTrace();
            return "推荐过程中出现错误：" + e.getMessage();
        }
    }

    @Tool(description = "查询热门歌曲")
    public List<MusicEntity> getHotMusic(
            @ToolParam(description = "热门类型：1-本周热门，2-本月热门，3-年度热门") Integer hotType,
            @ToolParam(description = "返回数量，默认20") Integer count
    ) {
        if (count == null) count = 20;
        if (hotType == null) hotType = 1;

        // 这里可以调用实际的接口，暂时返回空列表
        // 实际项目中应该调用对应的FeignClient方法
        return Collections.emptyList();
    }

    @Tool(description = "根据歌词搜索歌曲")
    public List<MusicEntity> searchByLyrics(
            @ToolParam(description = "歌词关键词") String lyricsKeyword,
            @ToolParam(description = "页码，从1开始") int pageNum,
            @ToolParam(description = "每页条数") int pageSize
    ) {
        try {
            // 在实际项目中，这里应该调用数据库的全文搜索
            // 暂时使用现有接口的label字段进行模拟
            return queryMusic(null, null, null, null, null, lyricsKeyword, pageNum, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // ========== 辅助方法 ==========

    private String analyzeUserQuery(String userQuery) {
        // 简单的关键词分析
        userQuery = userQuery.toLowerCase();
        if (userQuery.contains("收藏") || userQuery.contains("收藏夹")) {
            return "收藏";
        } else if (userQuery.contains("历史") || userQuery.contains("听过") || userQuery.contains("播放记录")) {
            return "历史";
        } else if (userQuery.contains("喜欢") || userQuery.contains("点赞")) {
            return "喜欢";
        } else if (userQuery.contains("歌手") || userQuery.contains("演唱")) {
            return "歌手";
        } else if (userQuery.contains("推荐")) {
            return "推荐";
        }
        return "搜索";
    }

    private String extractAuthorName(String query) {
        // 简单的歌手名提取逻辑
        String[] commonAuthors = {"周杰伦", "林俊杰", "邓紫棋", "陈奕迅", "薛之谦", "王菲", "张学友"};
        for (String author : commonAuthors) {
            if (query.contains(author)) {
                return author;
            }
        }
        return null;
    }

    private String extractLanguage(String query) {
        query = query.toLowerCase();
        if (query.contains("中文") || query.contains("国语") || query.contains("华语")) {
            return "中文";
        } else if (query.contains("英语") || query.contains("英文")) {
            return "英语";
        } else if (query.contains("日语") || query.contains("日文")) {
            return "日语";
        } else if (query.contains("韩语") || query.contains("韩文")) {
            return "韩语";
        }
        return null;
    }

    private String extractLabel(String query) {
        query = query.toLowerCase();
        if (query.contains("流行")) {
            return "流行";
        } else if (query.contains("摇滚")) {
            return "摇滚";
        } else if (query.contains("古典")) {
            return "古典";
        } else if (query.contains("民谣")) {
            return "民谣";
        } else if (query.contains("爵士")) {
            return "爵士";
        } else if (query.contains("电子") || query.contains("电音")) {
            return "电子";
        } else if (query.contains("说唱") || query.contains("rap")) {
            return "说唱";
        }
        return null;
    }

    private void analyzePreferences(List<MusicEntity> musicList,
                                    Map<String, Integer> authorPref,
                                    Map<String, Integer> languagePref,
                                    Map<String, Integer> labelPref) {
        for (MusicEntity music : musicList) {
            if (music.getAuthorName() != null) {
                authorPref.put(music.getAuthorName(),
                        authorPref.getOrDefault(music.getAuthorName(), 0) + 1);
            }
            if (music.getLanguage() != null) {
                languagePref.put(music.getLanguage(),
                        languagePref.getOrDefault(music.getLanguage(), 0) + 1);
            }
            if (music.getLabel() != null) {
                labelPref.put(music.getLabel(),
                        labelPref.getOrDefault(music.getLabel(), 0) + 1);
            }
        }
    }

    private String getTopPreference(Map<String, Integer> preferenceMap) {
        return preferenceMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private String formatMusicList(List<MusicEntity> musicList, String title) {
        if (musicList == null || musicList.isEmpty()) {
            return "没有找到相关歌曲。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🎵 ").append(title).append("（共").append(musicList.size()).append("首）\n\n");

        for (int i = 0; i < Math.min(musicList.size(), 10); i++) {
            MusicEntity music = musicList.get(i);
            sb.append(i + 1).append(". ")
                    .append(music.getAuthorName()).append(" - ")
                    .append(music.getSongName());

            if (music.getAlbumName() != null && !music.getAlbumName().isEmpty()) {
                sb.append("（专辑：").append(music.getAlbumName()).append("）");
            }

            if (music.getLanguage() != null && !music.getLanguage().isEmpty()) {
                sb.append(" | 语言：").append(music.getLanguage());
            }

            if (music.getLabel() != null && !music.getLabel().isEmpty()) {
                sb.append(" | 风格：").append(music.getLabel());
            }

            sb.append("\n");
        }

        if (musicList.size() > 10) {
            sb.append("\n... 还有").append(musicList.size() - 10).append("首歌曲");
        }

        return sb.toString();
    }
}