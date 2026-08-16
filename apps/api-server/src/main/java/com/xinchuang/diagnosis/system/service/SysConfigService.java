package com.xinchuang.diagnosis.system.service;

import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.ConfigPageQuery;
import com.xinchuang.diagnosis.system.dto.ConfigSaveRequest;
import com.xinchuang.diagnosis.system.dto.SimpleStatusRequest;
import com.xinchuang.diagnosis.system.vo.ConfigVO;

public interface SysConfigService {
    PageResponse<ConfigVO> page(ConfigPageQuery query);

    ConfigVO detail(Long id);

    ConfigVO getByKey(String configKey);

    ConfigVO create(ConfigSaveRequest request);

    ConfigVO update(Long id, ConfigSaveRequest request);

    void updateStatus(Long id, SimpleStatusRequest request);

    void delete(Long id);
}
