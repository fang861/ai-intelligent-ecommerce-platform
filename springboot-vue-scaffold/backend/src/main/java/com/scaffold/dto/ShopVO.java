package com.scaffold.dto;

import lombok.Data;

@Data
public class ShopVO {
    private Long id;
    private String shopName;
    private String shopDesc;
    private String avatar;
    private String nickname;
    private Integer productCount;
    private Integer salesCount;
    private String createdAt;
}
