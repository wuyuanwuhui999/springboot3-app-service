package com.player.chat.tool;

import com.player.chat.entity.MusicEntity;
import com.player.chat.mapper.MusicMapper;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MusicTool {
    @Autowired
    private MusicMapper musicMapper;

    @Tool(name="selectMusicListByAuthor",value = "根据歌手名称查询歌手的所有歌曲")
    public List<MusicEntity>selectMusicListByAuthor(@P("歌手名称") String authorName){
        return musicMapper.selectMusicListByAuthor(authorName);
    }
}
