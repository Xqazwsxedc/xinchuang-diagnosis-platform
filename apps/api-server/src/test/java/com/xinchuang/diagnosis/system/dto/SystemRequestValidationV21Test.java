package com.xinchuang.diagnosis.system.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 2.1 regression coverage for system-management request boundaries.
 * These checks protect controller contracts before service or database writes begin.
 */
class SystemRequestValidationV21Test {
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void userCreateRejectsInvalidUsernamePattern() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("1unsafe-name");

        assertFalse(validator.validateProperty(request, "username").isEmpty());
    }

    @Test
    void userCreateRejectsPasswordContainingWhitespace() {
        UserCreateRequest request = new UserCreateRequest();
        request.setPassword("bad pass");

        assertFalse(validator.validateProperty(request, "password").isEmpty());
    }

    @Test
    void userCreateRejectsInvalidAvatarUrl() {
        UserCreateRequest request = new UserCreateRequest();
        request.setAvatarUrl("file:///private/avatar.png");

        assertFalse(validator.validateProperty(request, "avatarUrl").isEmpty());
    }

    @Test
    void profileRejectsInvalidPhoneNumber() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setPhone("not-a-phone");

        assertFalse(validator.validateProperty(request, "phone").isEmpty());
    }

    @Test
    void profileRejectsInvalidEmailAddress() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setEmail("invalid-email");

        assertFalse(validator.validateProperty(request, "email").isEmpty());
    }

    @Test
    void roleRejectsUnsupportedDataScope() {
        RoleSaveRequest request = new RoleSaveRequest();
        request.setDataScope("TENANT");

        assertFalse(validator.validateProperty(request, "dataScope").isEmpty());
    }

    @Test
    void configRejectsLowercaseConfigKey() {
        ConfigSaveRequest request = new ConfigSaveRequest();
        request.setConfigKey("system_name");

        assertFalse(validator.validateProperty(request, "configKey").isEmpty());
    }

    @Test
    void roleStatusRejectsUnsupportedValue() {
        RoleStatusRequest request = new RoleStatusRequest();
        request.setStatus("ARCHIVED");

        assertFalse(validator.validateProperty(request, "status").isEmpty());
    }
}
