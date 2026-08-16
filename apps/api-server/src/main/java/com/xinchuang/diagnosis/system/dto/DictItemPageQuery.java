package com.xinchuang.diagnosis.system.dto;

import com.xinchuang.diagnosis.common.query.PageQuery;

public class DictItemPageQuery extends PageQuery {
    private String dictCode;
    private String itemValue;
    private String itemLabel;
    private String status;

    public String getDictCode() {
        return dictCode;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public String getItemValue() {
        return itemValue;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
