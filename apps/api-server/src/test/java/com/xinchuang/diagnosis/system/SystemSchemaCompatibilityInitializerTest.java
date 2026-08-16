package com.xinchuang.diagnosis.system;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;

class SystemSchemaCompatibilityInitializerTest {
    @Test
    void appliesOnlyPredefinedDdlForMissingColumns() {
        JdbcTemplate jdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), any(), any())).thenReturn(0);

        new SystemSchemaCompatibilityInitializer(jdbcTemplate).run(new DefaultApplicationArguments());

        verify(jdbcTemplate, times(5)).execute(any(String.class));
        verify(jdbcTemplate).execute("ALTER TABLE sys_user ADD COLUMN avatar_url VARCHAR(500) NULL COMMENT '头像地址'");
        verify(jdbcTemplate).execute("ALTER TABLE sys_role ADD COLUMN data_scope VARCHAR(32) NOT NULL DEFAULT 'ALL' COMMENT '数据权限：ALL/DEPARTMENT/SELF/CUSTOM'");
        verify(jdbcTemplate).execute("ALTER TABLE sys_role ADD COLUMN data_scope_config VARCHAR(500) NULL COMMENT '自定义数据权限配置'");
        verify(jdbcTemplate).execute("ALTER TABLE kb_instance ADD COLUMN created_by BIGINT NULL");
        verify(jdbcTemplate).execute("ALTER TABLE kb_instance ADD COLUMN updated_by BIGINT NULL");
    }

    @Test
    void skipsDdlWhenColumnsAlreadyExist() {
        JdbcTemplate jdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(any(String.class), eq(Integer.class), any(), any())).thenReturn(1);

        new SystemSchemaCompatibilityInitializer(jdbcTemplate).run(new DefaultApplicationArguments());

        verify(jdbcTemplate, never()).execute(any(String.class));
    }
}
