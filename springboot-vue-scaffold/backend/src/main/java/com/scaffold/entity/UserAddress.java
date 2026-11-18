package com.scaffold.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.scaffold.common.FlagDeserializer;
import lombok.Data;

@Data
@TableName("user_address")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAddress {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    @JsonAlias({"receiverName", "name"})
    private String receiver;
    private String phone;
    private String province;
    private String city;
    private String district;
    @JsonAlias({"address"})
    private String detail;
    @JsonAlias({"defaultAddress"})
    @JsonDeserialize(using = FlagDeserializer.class)
    private Integer isDefault;
    @TableField(fill = FieldFill.INSERT)
    private String createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedAt;
}
