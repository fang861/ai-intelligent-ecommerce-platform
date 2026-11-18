package com.scaffold.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RagSearchHit {
    private String filename;
    private String text;
    private Double score;
}
