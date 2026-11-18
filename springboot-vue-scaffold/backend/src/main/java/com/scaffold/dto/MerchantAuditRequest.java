package com.scaffold.dto;

import lombok.Data;

@Data
public class MerchantAuditRequest {
    /** 1通过 2拒绝 */
    private Integer auditStatus;
    private String auditRemark;
}
