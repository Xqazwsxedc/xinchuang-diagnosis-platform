package com.xinchuang.diagnosis.auth.service;

import com.xinchuang.diagnosis.auth.dto.LoginRequest;
import com.xinchuang.diagnosis.auth.vo.LoginResponseVO;
import com.xinchuang.diagnosis.auth.vo.LoginUserVO;

public interface AuthService {
    LoginResponseVO login(LoginRequest request);

    LoginUserVO currentUser();
}
