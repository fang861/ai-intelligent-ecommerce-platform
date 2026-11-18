package com.scaffold.dto;

import lombok.Data;

@Data
public class AfterSaleApplyRequest {
    /** 1仅退款 2退货退款 3换货 */
    private Integer type;
    private String reason;
}
