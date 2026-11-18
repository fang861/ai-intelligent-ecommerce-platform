package com.scaffold.dto;

import lombok.Data;

@Data
public class LogisticsNode {
    /** created / paid / shipped / collected / transit / delivering / signed */
    private String code;
    private String title;
    private String desc;
    private String time;
    /** 是否已完成 */
    private Boolean done;
    /** 是否当前节点 */
    private Boolean current;
}
