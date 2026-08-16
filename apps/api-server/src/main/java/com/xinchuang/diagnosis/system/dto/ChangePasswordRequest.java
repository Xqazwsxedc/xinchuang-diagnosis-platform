package com.xinchuang.diagnosis.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
    @NotBlank
    @Size(min = 6, max = 32)
    @Pattern(regexp = "\\S{6,32}", message = "原密码不能包含空白字符")
    private String oldPassword;

    @NotBlank
    @Size(min = 6, max = 32)
    @Pattern(regexp = "\\S{6,32}", message = "新密码不能包含空白字符")
    private String newPassword;

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
