package com.pcc2.social.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserVO user;
}
