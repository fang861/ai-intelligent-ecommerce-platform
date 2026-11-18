package com.scaffold.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度为 3-32 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码至少 6 位")
    private String password;

    @Size(max = 64, message = "昵称过长")
    private String nickname;

    @Size(max = 20, message = "手机号过长")
    private String phone;

    /** USER | MERCHANT，默认 USER */
    private String role;

    @Size(max = 100, message = "店铺名称过长")
    private String shopName;

    @Size(max = 500, message = "店铺简介过长")
    private String shopDesc;

    /** 营业执照图片 URL */
    @Size(max = 512, message = "营业执照地址过长")
    private String licenseImage;

    /** 身份证正面 URL */
    @Size(max = 512, message = "身份证正面地址过长")
    private String idCardFront;

    /** 身份证反面 URL */
    @Size(max = 512, message = "身份证反面地址过长")
    private String idCardBack;
}
