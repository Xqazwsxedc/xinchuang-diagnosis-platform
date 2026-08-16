package com.xinchuang.diagnosis.system.service;

import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.ChangePasswordRequest;
import com.xinchuang.diagnosis.system.dto.ProfileUpdateRequest;
import com.xinchuang.diagnosis.system.dto.ResetPasswordRequest;
import com.xinchuang.diagnosis.system.dto.UserCreateRequest;
import com.xinchuang.diagnosis.system.dto.UserPageQuery;
import com.xinchuang.diagnosis.system.dto.UserStatusRequest;
import com.xinchuang.diagnosis.system.dto.UserUpdateRequest;
import com.xinchuang.diagnosis.system.entity.SysUser;
import com.xinchuang.diagnosis.system.vo.RoleVO;
import com.xinchuang.diagnosis.system.vo.UserVO;
import java.util.List;

public interface SysUserService {
    PageResponse<UserVO> page(UserPageQuery query);

    UserVO detail(Long id);

    UserVO create(UserCreateRequest request);

    UserVO update(Long id, UserUpdateRequest request);

    void updateStatus(Long id, UserStatusRequest request);

    void resetPassword(Long id, ResetPasswordRequest request);

    UserVO currentProfile();

    UserVO updateCurrentProfile(ProfileUpdateRequest request);

    void changeCurrentPassword(ChangePasswordRequest request);

    void delete(Long id);

    List<RoleVO> roles();

    SysUser getEnabledUserById(Long id);

    List<String> getRoleCodes(Long userId);
}
