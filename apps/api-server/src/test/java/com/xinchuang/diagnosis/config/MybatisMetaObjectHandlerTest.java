package com.xinchuang.diagnosis.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.xinchuang.diagnosis.ledger.entity.BusinessSystem;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

/** 验证全局审计时间字段在持久化更新时始终覆盖旧值。 */
class MybatisMetaObjectHandlerTest {

    @Test
    void updateFillReplacesExistingUpdatedTime() {
        BusinessSystem entity = new BusinessSystem();
        LocalDateTime oldTime = LocalDateTime.of(2026, 1, 1, 0, 0);
        entity.setUpdatedAt(oldTime);

        new MybatisMetaObjectHandler().updateFill(SystemMetaObject.forObject(entity));

        assertTrue(entity.getUpdatedAt().isAfter(oldTime));
    }
}
