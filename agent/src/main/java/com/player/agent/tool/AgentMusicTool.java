package com.player.agent.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.player.agent.mapper.AgentMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * agent 模块可调用的工具（收藏夹 / 音乐圈操作）——与 FastAPI 侧 agent/tools/agent_tool.py 一一对应。
 *
 * <p>与 SQL 查询路径互补：用户的意图**无法提取出音乐查询 SQL** 时（例如「把周杰伦的歌加到大陆男歌手收藏夹」
 * 「创建/删除/重命名收藏夹」「把这首歌分享到音乐圈」），由大模型从工具清单里选一个工具，服务端执行。
 * 选不出任何工具时，agent 统一回复「只能回答与音乐相关的问题」。
 *
 * <p>表结构：
 * <ul>
 *   <li>music_favorite_directory(id, name, user_id, create_time, update_time)</li>
 *   <li>music_favorite_list(id, music_id, favorite_id, ...)  favorite_id 外键 ON DELETE CASCADE</li>
 *   <li>circle(id, relation_id, content, imgs, type, user_id, ..., permission)  type=MUSIC 时 relation_id = music.id</li>
 * </ul>
 */
@Slf4j
@Component
public class AgentMusicTool {

    /** 一次性操作最多涉及多少首歌 */
    public static final int MAX_SONGS = 50;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** 工具清单：名称 -> 「描述||参数说明」，两项目（Spring / FastAPI）保持一致 */
    private static final Map<String, String> TOOL_CATALOG = new LinkedHashMap<>();

    static {
        TOOL_CATALOG.put("addMusicToFavorite",
                "把歌曲添加到用户的收藏夹（收藏夹不存在时会提示先创建）||"
                        + "favoriteName：收藏夹名称（必填）、songName：歌曲名（可选，模糊匹配）、"
                        + "authorName：歌手名（可选，模糊匹配）、albumName：专辑名（可选，模糊匹配）、"
                        + "musicIds：歌曲 id 数组（可选；用户用「这首歌/这些歌」指代上一轮结果时用）");
        TOOL_CATALOG.put("createFavoriteDirectory", "创建收藏夹||name：收藏夹名称（必填）");
        TOOL_CATALOG.put("deleteFavoriteDirectory", "删除收藏夹，连同收藏夹里的歌曲一起删除||name：收藏夹名称（必填）");
        TOOL_CATALOG.put("renameFavoriteDirectory", "修改收藏夹名称||oldName：原收藏夹名称（必填）、newName：新收藏夹名称（必填）");
        TOOL_CATALOG.put("shareMusicToCircle",
                "把歌曲分享到音乐圈（发一条音乐朋友圈）||"
                        + "songName：歌曲名（可选）、authorName：歌手名（可选）、"
                        + "musicIds：歌曲 id 数组（可选；用户用「这首歌」指代上一轮结果时用）、"
                        + "content：分享文案（可选，不给则自动生成）、permission：可见权限 0/1（可选，默认 1）");
    }

    @Autowired
    private AgentMapper agentMapper;

    /** 可用工具名列表 */
    public static List<String> toolNames() {
        return new ArrayList<>(TOOL_CATALOG.keySet());
    }

    /** 渲染工具清单文本（拼进工具选择提示词） */
    public static String renderToolCatalog() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : TOOL_CATALOG.entrySet()) {
            String[] parts = entry.getValue().split("\\|\\|", 2);
            sb.append("- ").append(entry.getKey()).append("：").append(parts[0]).append("\n");
            sb.append("  参数：").append(parts.length > 1 ? parts[1] : "").append("\n");
        }
        return sb.toString().trim();
    }

    /** 工具选择结果 */
    @Data
    public static class ToolIntent {
        private String tool;
        private Map<String, Object> params;

        public boolean isEmpty() {
            return tool == null || tool.trim().isEmpty();
        }
    }

    /**
     * 解析大模型返回的工具选择 JSON（容忍 ```json 包裹、多余文字、大小写差异）。
     * 没选中工具（none / 解析失败 / 工具名不认识）时返回空 ToolIntent，调用方据此输出统一提示。
     */
    public static ToolIntent parseToolSelection(String raw) {
        ToolIntent intent = new ToolIntent();
        intent.setParams(new LinkedHashMap<>());
        if (raw == null || raw.trim().isEmpty()) {
            return intent;
        }
        String value = raw.trim();
        if (value.startsWith("```")) {
            value = value.replace("`", "").trim();
            if (value.toLowerCase().startsWith("json")) {
                value = value.substring(4).trim();
            }
        }
        int start = value.indexOf('{');
        int end = value.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return intent;
        }
        JsonNode node;
        try {
            node = objectMapper.readTree(value.substring(start, end + 1));
        } catch (Exception e) {
            log.warn("工具选择结果不是合法 JSON: {}", value.length() > 120 ? value.substring(0, 120) : value);
            return intent;
        }
        String tool = node.hasNonNull("tool") ? node.get("tool").asText() : (node.hasNonNull("name") ? node.get("name").asText() : "");
        tool = tool == null ? "" : tool.trim();
        if (tool.isEmpty() || Arrays.asList("none", "null", "no", "无", "没有").contains(tool.toLowerCase())) {
            return intent;
        }
        String matched = null;
        for (String name : TOOL_CATALOG.keySet()) {
            if (name.equalsIgnoreCase(tool) || normalize(name).equals(normalize(tool))) {
                matched = name;
                break;
            }
        }
        if (matched == null) {
            log.warn("模型选了不存在的工具: {}", tool);
            return intent;
        }
        intent.setTool(matched);
        JsonNode params = node.has("params") ? node.get("params") : node.get("arguments");
        if (params != null && params.isObject()) {
            try {
                intent.setParams(objectMapper.convertValue(params, LinkedHashMap.class));
            } catch (Exception e) {
                log.warn("工具参数解析失败: {}", e.getMessage());
            }
        }
        return intent;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase().replace("_", "").replace("-", "");
    }

    // ==================== 参数处理 ====================
    private static Object param(Map<String, Object> params, String... keys) {
        if (params == null) {
            return null;
        }
        for (String key : keys) {
            Object value = params.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof String && ((String) value).trim().isEmpty()) {
                continue;
            }
            if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
                continue;
            }
            return value;
        }
        return null;
    }

    private static String strParam(Map<String, Object> params, String... keys) {
        Object value = param(params, keys);
        return value == null ? null : String.valueOf(value).trim();
    }

    private static List<Long> musicIdParams(Map<String, Object> params) {
        Object raw = param(params, "musicIds", "music_ids", "ids", "id");
        List<Long> result = new ArrayList<>();
        if (raw == null) {
            return result;
        }
        if (raw instanceof Collection) {
            for (Object item : (Collection<?>) raw) {
                Long id = toLong(item);
                if (id != null) {
                    result.add(id);
                }
            }
        } else {
            Long id = toLong(raw);
            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value).trim().replace(".0", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== 歌曲 / 收藏夹解析 ====================
    private Long directoryId(String userId, String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return agentMapper.getFavoriteDirectoryIdByName(userId, name.trim());
    }

    /** 按 id 或名称条件找歌曲：优先 musicIds，其次 songName/authorName/albumName 模糊匹配 */
    private List<Map<String, Object>> findMusic(Map<String, Object> params, int limit) {
        List<Long> ids = musicIdParams(params);
        if (!ids.isEmpty()) {
            return agentMapper.selectMusicByIds(ids, Math.min(limit, ids.size()));
        }
        String songName = strParam(params, "songName", "song_name", "song");
        String authorName = strParam(params, "authorName", "author_name", "author", "singer");
        String albumName = strParam(params, "albumName", "album_name", "album");
        if (songName == null && authorName == null && albumName == null) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> rows = agentMapper.selectMusicByNames(songName, authorName, albumName, limit);
        return rows == null ? Collections.emptyList() : rows;
    }

    private static String describeSongs(List<Map<String, Object>> songs, int maxShow) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(maxShow, songs.size()); i++) {
            Map<String, Object> song = songs.get(i);
            if (i > 0) {
                sb.append("、");
            }
            sb.append(song.get("authorName") == null ? "" : song.get("authorName"))
                    .append("《").append(song.get("songName") == null ? "" : song.get("songName")).append("》");
        }
        if (songs.size() > maxShow) {
            sb.append(" 等 ").append(songs.size()).append(" 首");
        }
        return sb.toString();
    }

    // ==================== 各工具实现 ====================
    public String createFavoriteDirectory(String userId, Map<String, Object> params) {
        String name = strParam(params, "name", "favoriteName", "favorite_name", "directoryName");
        if (name == null) {
            return "请告诉我要创建的收藏夹名称。";
        }
        if (directoryId(userId, name) != null) {
            return "你已经有一个叫「" + name + "」的收藏夹了，不需要重复创建。";
        }
        agentMapper.insertFavoriteDirectory(name, userId);
        log.info("[AgentTool] 创建收藏夹成功: userId={}, name={}", userId, name);
        return "已为你创建收藏夹「" + name + "」。";
    }

    public String deleteFavoriteDirectory(String userId, Map<String, Object> params) {
        String name = strParam(params, "name", "favoriteName", "favorite_name", "directoryName");
        if (name == null) {
            return "请告诉我要删除的收藏夹名称。";
        }
        Long favoriteId = directoryId(userId, name);
        if (favoriteId == null) {
            return "没有找到名为「" + name + "」的收藏夹。";
        }
        Long rows = agentMapper.countFavoriteListRows(favoriteId);
        agentMapper.deleteFavoriteDirectoryByName(userId, name);
        log.info("[AgentTool] 删除收藏夹成功: userId={}, name={}, 关联歌曲={}", userId, name, rows);
        return "已删除收藏夹「" + name + "」，连同其中的 " + (rows == null ? 0 : rows) + " 首歌曲。";
    }

    public String renameFavoriteDirectory(String userId, Map<String, Object> params) {
        String oldName = strParam(params, "oldName", "old_name", "name", "favoriteName");
        String newName = strParam(params, "newName", "new_name", "toName", "targetName");
        if (oldName == null || newName == null) {
            return "请告诉我原收藏夹名称和要改成的新名称。";
        }
        if (directoryId(userId, oldName) == null) {
            return "没有找到名为「" + oldName + "」的收藏夹。";
        }
        if (directoryId(userId, newName) != null) {
            return "你已经有一个叫「" + newName + "」的收藏夹了，换一个名称吧。";
        }
        agentMapper.updateFavoriteDirectoryName(userId, oldName, newName);
        log.info("[AgentTool] 重命名收藏夹成功: userId={}, {} -> {}", userId, oldName, newName);
        return "已把收藏夹「" + oldName + "」重命名为「" + newName + "」。";
    }

    public String addMusicToFavorite(String userId, Map<String, Object> params) {
        String favoriteName = strParam(params, "favoriteName", "favorite_name", "directoryName", "name");
        if (favoriteName == null) {
            return "请告诉我要添加到哪个收藏夹。";
        }
        Long favoriteId = directoryId(userId, favoriteName);
        if (favoriteId == null) {
            return "没有找到名为「" + favoriteName + "」的收藏夹，你可以先让我创建它。";
        }
        List<Map<String, Object>> songs = findMusic(params, MAX_SONGS);
        if (songs.isEmpty()) {
            return "没有找到符合条件「" + favoriteName + "」要收藏的歌曲，请告诉我歌曲名或歌手名。";
        }
        int added = 0;
        for (Map<String, Object> song : songs) {
            Long musicId = toLong(song.get("id"));
            if (musicId == null) {
                continue;
            }
            Long exists = agentMapper.countFavoriteListItem(favoriteId, musicId);
            if (exists != null && exists > 0) {
                continue;
            }
            agentMapper.insertFavoriteListItem(musicId, favoriteId);
            added++;
        }
        log.info("[AgentTool] 添加收藏成功: userId={}, favorite={}, 新增={}/{}", userId, favoriteName, added, songs.size());
        if (added == 0) {
            return "这些歌曲都已经在收藏夹「" + favoriteName + "」里了。";
        }
        return "已把 " + describeSongs(songs, 3) + " 添加到收藏夹「" + favoriteName + "」（新增 " + added + " 首）。";
    }

    public String shareMusicToCircle(String userId, Map<String, Object> params) {
        List<Map<String, Object>> songs = findMusic(params, 3);
        if (songs.isEmpty()) {
            return "请告诉我要分享到音乐圈的歌曲（可以说歌名、歌手，或直接说“这首歌”）。";
        }
        Map<String, Object> song = songs.get(0);
        String content = strParam(params, "content", "text", "message");
        if (content == null) {
            content = "分享歌曲：" + (song.get("authorName") == null ? "" : song.get("authorName"))
                    + " - 《" + (song.get("songName") == null ? "" : song.get("songName")) + "》";
        }
        int permission = 1;
        Object permissionParam = param(params, "permission");
        if (permissionParam != null) {
            try {
                permission = Integer.parseInt(String.valueOf(permissionParam).trim());
            } catch (NumberFormatException ignored) {
                permission = 1;
            }
        }
        Long musicId = toLong(song.get("id"));
        agentMapper.insertCircle(content, musicId, userId, permission);
        log.info("[AgentTool] 分享音乐圈成功: userId={}, musicId={}, content={}", userId, musicId, content);
        return "已把《" + (song.get("songName") == null ? "" : song.get("songName")) + "》分享到音乐圈啦：" + content;
    }

    /**
     * 执行工具并返回给用户的话术；工具名不认识时返回 null（调用方据此走「只能回答音乐相关问题」的统一提示）。
     */
    public String execute(String userId, String toolName, Map<String, Object> params) {
        if (toolName == null || !TOOL_CATALOG.containsKey(toolName.trim())) {
            return null;
        }
        Map<String, Object> safeParams = params == null ? Collections.emptyMap() : params;
        try {
            switch (toolName.trim()) {
                case "addMusicToFavorite":
                    return addMusicToFavorite(userId, safeParams);
                case "createFavoriteDirectory":
                    return createFavoriteDirectory(userId, safeParams);
                case "deleteFavoriteDirectory":
                    return deleteFavoriteDirectory(userId, safeParams);
                case "renameFavoriteDirectory":
                    return renameFavoriteDirectory(userId, safeParams);
                case "shareMusicToCircle":
                    return shareMusicToCircle(userId, safeParams);
                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("[AgentTool] 工具执行失败: tool=" + toolName, e);
            return "抱歉，操作没有成功：" + e.getMessage();
        }
    }
}
