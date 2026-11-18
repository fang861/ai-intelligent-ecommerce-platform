package com.scaffold.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiChatRequest {
    private String message;
    /** 识图导购：已上传图片地址，如 /uploads/xxx.jpg */
    private String imageUrl;
    /** Agent 多轮上下文（不含当前 message） */
    private List<AiChatTurn> history;

    @Data
    public static class AiChatTurn {
        private String role;
        private String content;
    }
}
