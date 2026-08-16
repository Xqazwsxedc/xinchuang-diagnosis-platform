package com.xinchuang.diagnosis.system.service;

import com.xinchuang.diagnosis.common.api.PageResponse;
import com.xinchuang.diagnosis.system.dto.DictItemPageQuery;
import com.xinchuang.diagnosis.system.dto.DictItemSaveRequest;
import com.xinchuang.diagnosis.system.dto.DictTypePageQuery;
import com.xinchuang.diagnosis.system.dto.DictTypeSaveRequest;
import com.xinchuang.diagnosis.system.dto.SimpleStatusRequest;
import com.xinchuang.diagnosis.system.vo.DictItemVO;
import com.xinchuang.diagnosis.system.vo.DictTypeVO;
import java.util.List;

public interface SysDictService {
    PageResponse<DictTypeVO> pageTypes(DictTypePageQuery query);

    DictTypeVO typeDetail(Long id);

    DictTypeVO createType(DictTypeSaveRequest request);

    DictTypeVO updateType(Long id, DictTypeSaveRequest request);

    void updateTypeStatus(Long id, SimpleStatusRequest request);

    void deleteType(Long id);

    PageResponse<DictItemVO> pageItems(DictItemPageQuery query);

    List<DictItemVO> enabledItems(String dictCode);

    DictItemVO itemDetail(Long id);

    DictItemVO createItem(DictItemSaveRequest request);

    DictItemVO updateItem(Long id, DictItemSaveRequest request);

    void updateItemStatus(Long id, SimpleStatusRequest request);

    void deleteItem(Long id);
}
