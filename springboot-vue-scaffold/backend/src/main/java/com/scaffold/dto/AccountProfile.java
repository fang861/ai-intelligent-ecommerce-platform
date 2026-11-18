package com.scaffold.dto;

import lombok.Data;

@Data
public class AccountProfile {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    /** ADMIN / USER / MERCHANT */
    private String role;
    /** ADMIN | USER | MERCHANT */
    private String accountType;
    private Integer status;
    private Double balance;
    private Integer auditStatus;
    private String shopName;
    private String shopDesc;
    private String auditRemark;
    private String createdAt;
}
