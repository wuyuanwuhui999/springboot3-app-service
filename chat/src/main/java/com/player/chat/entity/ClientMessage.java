package com.player.chat.entity;

import lombok.Data;

import java.util.List;

@Data
public class ClientMessage {
    private String chatId;
    private String prompt;
    private String token;
    List<String> files;
}