package com.pcc2.social.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserVO user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }
}
