package com.scaffold.dto;

import lombok.Data;

@Data
public class ShipOrderRequest {
    /** 快递公司 */
    private String expressCompany;
    /** 物流单号 */
    private String trackingNo;
    /** 备注 */
    private String remark;
}
