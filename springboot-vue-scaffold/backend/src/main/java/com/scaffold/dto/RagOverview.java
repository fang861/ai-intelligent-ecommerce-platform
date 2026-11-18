package com.scaffold.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagOverview {
    private int docCount;
    private int chunkCount;
    private int totalChars;
    private String storeType;
    private String embeddingType;
    private List<RagDocView> docs = new ArrayList<>();
}
