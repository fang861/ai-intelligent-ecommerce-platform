package com.scaffold.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AgentAdminOverview {
    private boolean ready;
    private boolean enabled;
    private String provider;
    private String apiUrl;
    private String model;
    private String visionModel;
    private String apiKeyMasked;
    private String chatEndpoint;
    private String visionEndpoint;
    private String storeType;
    private String embeddingType;
    private int toolCount;
    private int ragDocCount;
    private int ragChunkCount;
    private List<AgentToolView> tools = new ArrayList<>();
    private List<RagDocView> ragDocs = new ArrayList<>();
}
