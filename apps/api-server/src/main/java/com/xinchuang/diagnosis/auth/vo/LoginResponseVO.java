package com.xinchuang.diagnosis.auth.vo;

public class LoginResponseVO {
    private String token;
    private LoginUserVO user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LoginUserVO getUser() {
        return user;
    }

    public void setUser(LoginUserVO user) {
        this.user = user;
    }
}
