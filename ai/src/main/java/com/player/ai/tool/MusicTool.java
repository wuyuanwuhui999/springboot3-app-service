package com.player.ai.tool;

import com.player.ai.entity.MusicEntity;
import com.player.ai.mapper.MusicMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MusicTool {
    @Autowired
    private MusicMapper musicMapper;

    @Tool(description = "根据歌手名称查询歌手的所有歌曲")
    public List<MusicEntity>selectMusicList(@ToolParam(description = "歌手名称") String authorName){
        return musicMapper.selectMusicList(authorName);
    }
}
