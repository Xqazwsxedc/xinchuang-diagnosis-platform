package com.xinchuang.diagnosis.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xinchuang.diagnosis.ledger.entity.BusinessSystemDb;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BusinessSystemDbMapper extends BaseMapper<BusinessSystemDb> {
    @Delete("DELETE FROM ledger_business_system_db WHERE business_system_id = #{businessSystemId}")
    int deleteByBusinessSystemIdPhysically(@Param("businessSystemId") Long businessSystemId);
}
