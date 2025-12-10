package com.player.agent.tool;

import com.player.agent.mapper.AgentMapper;
import com.player.common.entity.MusicEntity;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentTool {
    @Autowired
    private AgentMapper agentMapper;

    @Tool(description = "根据歌手名称查询歌手的所有歌曲")
    public List<MusicEntity>selectMusicList(@ToolParam(description = "歌手名称") String authorName){
        return agentMapper.selectMusicList(authorName);
    }
}
