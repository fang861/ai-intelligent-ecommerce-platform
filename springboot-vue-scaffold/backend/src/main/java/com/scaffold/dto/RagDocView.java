package com.scaffold.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagDocView {
    private String filename;
    private String title;
    private String summary;
    private String content;
    private int chunkCount;
    private int charCount;
    /** builtin=内置 classpath；upload=后台上传 */
    private String origin;
    private boolean uploaded;
    private List<RagChunkView> chunks = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RagChunkView {
        private int index;
        private String text;
        private int charCount;
    }
}
