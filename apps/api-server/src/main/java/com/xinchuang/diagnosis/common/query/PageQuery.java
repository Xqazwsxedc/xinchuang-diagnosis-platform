package com.xinchuang.diagnosis.common.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PageQuery {
    @Min(1)
    private long pageNo = 1;

    @Min(1)
    @Max(200)
    private long pageSize = 10;

    private String keyword;

    public long getPageNo() {
        return pageNo;
    }

    public void setPageNo(long pageNo) {
        this.pageNo = pageNo;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}

