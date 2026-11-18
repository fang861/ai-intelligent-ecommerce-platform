package com.scaffold.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String username;
    private String nickname;
    private String role;
    private String avatar;
    private Long userId;
    /** ADMIN | USER | MERCHANT */
    private String accountType;
    private String message;
}
