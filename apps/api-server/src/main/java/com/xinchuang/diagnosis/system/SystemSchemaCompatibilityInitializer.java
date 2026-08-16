package com.xinchuang.diagnosis.system;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SystemSchemaCompatibilityInitializer implements ApplicationRunner {
    private static final List<SchemaChange> SCHEMA_CHANGES = List.of(
        new SchemaChange(
            "sys_user",
            "avatar_url",
            "ALTER TABLE sys_user ADD COLUMN avatar_url VARCHAR(500) NULL COMMENT '头像地址'"
        ),
        new SchemaChange(
            "sys_role",
            "data_scope",
            "ALTER TABLE sys_role ADD COLUMN data_scope VARCHAR(32) NOT NULL DEFAULT 'ALL' COMMENT '数据权限：ALL/DEPARTMENT/SELF/CUSTOM'"
        ),
        new SchemaChange(
            "sys_role",
            "data_scope_config",
            "ALTER TABLE sys_role ADD COLUMN data_scope_config VARCHAR(500) NULL COMMENT '自定义数据权限配置'"
        ),
        new SchemaChange(
            "kb_instance",
            "created_by",
            "ALTER TABLE kb_instance ADD COLUMN created_by BIGINT NULL"
        ),
        new SchemaChange(
            "kb_instance",
            "updated_by",
            "ALTER TABLE kb_instance ADD COLUMN updated_by BIGINT NULL"
        )
    );

    private final JdbcTemplate jdbcTemplate;

    public SystemSchemaCompatibilityInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        SCHEMA_CHANGES.forEach(this::addColumnIfMissing);
    }

    private void addColumnIfMissing(SchemaChange change) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
            Integer.class,
            change.tableName(),
            change.columnName()
        );
        if (count == null || count > 0) {
            return;
        }
        jdbcTemplate.execute(change.alterStatement());
    }

    private record SchemaChange(String tableName, String columnName, String alterStatement) {
    }
}
