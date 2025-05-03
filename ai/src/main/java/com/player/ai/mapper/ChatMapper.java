package com.player.ai.mapper;

import com.player.ai.entity.ChatEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMapper {
    void saveChat(ChatEntity chatEntity);
}
