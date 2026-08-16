package com.xinchuang.diagnosis.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import java.time.LocalDateTime;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // updateById 会携带查询出来的旧 updatedAt；严格填充不会覆盖非空字段，
        // 因此更新时必须直接写入当前时间，保证列表“更新时间”反映最后一次修改。
        setFieldValByName("updatedAt", LocalDateTime.now(), metaObject);
    }
}
