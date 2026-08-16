package com.xinchuang.diagnosis.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProfileUpdateRequest {
    @NotBlank
    @Size(max = 64)
    private String realName;

    @Size(max = 500)
    @Pattern(regexp = "^$|^https?://.+|^data:image/.+;base64,.+", message = "头像地址需为 http(s) 图片地址或 data:image base64")
    private String avatarUrl;

    @Size(max = 100)
    private String department;

    @Size(max = 32)
    @Pattern(regexp = "^$|^1\\d{10}$|^\\+?[0-9\\- ]{6,32}$", message = "手机号格式不正确")
    private String phone;

    @Email
    @Size(max = 128)
    private String email;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
