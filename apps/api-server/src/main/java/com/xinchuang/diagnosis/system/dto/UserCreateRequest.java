package com.xinchuang.diagnosis.system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Collections;
import java.util.List;

public class UserCreateRequest {
    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_:-]{2,63}$", message = "账号需以字母开头，长度 3-64 位，只能包含字母、数字、下划线、冒号和短横线")
    private String username;

    @NotBlank
    @Size(min = 6, max = 32)
    @Pattern(regexp = "\\S{6,32}", message = "密码不能包含空白字符")
    private String password;

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

    @NotBlank
    @Pattern(regexp = "ENABLED|DISABLED", message = "状态只能是 ENABLED/DISABLED")
    private String status;

    @Size(max = 500)
    private String remark;

    private List<Long> roleIds = Collections.emptyList();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
