package com.xinchuang.diagnosis.system.dto;

import com.xinchuang.diagnosis.common.query.PageQuery;

public class DictTypePageQuery extends PageQuery {
    private String dictCode;
    private String dictName;
    private String status;

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getDictName() {
        return dictName;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
