package com.scaffold.dto;

import lombok.Data;

@Data
public class AfterSaleHandleRequest {
    /** true同意 false拒绝 */
    private Boolean approve;
    private String reply;
}
