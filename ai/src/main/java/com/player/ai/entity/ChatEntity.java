package com.player.ai.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChatEntity {
    @Schema(description = "主键")
    private Long id;
    @Schema(description = "用户id")
    private String userId;
    @Schema(description = "文件")
    private String files;
    @Schema(description = "会话id")
    private String chatId;
    @Schema(description = "提示词")
    private String prompt;
    @Schema(description = "模型")
    private String model;
    @Schema(description = "内容")
    private String content;
    @Schema(description = "think思考内容")
    private String thinkContent;
    @Schema(description = "回复的正文")
    private String responseContent;
    @Schema(description = "创建时间")
    private String createTime;

    // 自定义setContent方法实现自动拆分
    public void setContent(String content) {
        this.content = content;

        if (content == null || content.isEmpty()) {
            this.thinkContent = null;
            this.responseContent = null;
            return;
        }

        // 查找<think>标签位置
        int thinkStart = content.indexOf("<think>");
        int thinkEnd = content.indexOf("</think>");

        // 检查是否找到完整标签
        if (thinkStart >= 0 && thinkEnd > thinkStart) {
            // 提取包含标签的思考内容
            this.thinkContent = content.substring(
                    thinkStart,
                    thinkEnd + "</think>".length()
            );

            // 提取响应内容（标签之后的部分）
            String afterThink = content.substring(thinkEnd + "</think>".length());

            // 清理响应内容前后的换行和空格
            this.responseContent = afterThink
                    .replaceFirst("^\\s+", "")  // 移除开头空白
                    .replaceFirst("\\s+$", ""); // 移除结尾空白
        } else {
            // 没有找到标签时，思考内容为空
            this.thinkContent = null;
            this.responseContent = content;
        }
    }
}