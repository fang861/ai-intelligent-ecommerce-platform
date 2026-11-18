package com.scaffold.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentToolView {
    private String name;
    private String groupName;
    private String description;
    private boolean needLogin;
    private boolean confirmRequired;
    private List<ParamView> params = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamView {
        private String name;
        private String type;
        private String description;
        private boolean required;
    }
}
