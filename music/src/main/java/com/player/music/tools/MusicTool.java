package com.player.music.tools;

import com.player.music.entity.MusicEntity;
import com.player.music.mapper.ChatMapper;
import com.player.music.mapper.MusicMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MusicTool {
    @Autowired
    private ChatMapper chatMapper;

    @Tool(description = "根据歌手名称查询歌手的所有歌曲")
    public List<MusicEntity>selectMusicList(@ToolParam(description = "歌手名称") String authorName){
        return chatMapper.selectMusicList(authorName);
    }
}
