package com.xinchuang.diagnosis.common.api;

import java.util.Collections;
import java.util.List;

public class PageResponse<T> {
    private List<T> records = Collections.emptyList();
    private long total;
    private long pageNo;
    private long pageSize;
    private long pages;

    public static <T> PageResponse<T> of(List<T> records, long total, long pageNo, long pageSize) {
        PageResponse<T> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(total);
        response.setPageNo(pageNo);
        response.setPageSize(pageSize);
        response.setPages(pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize);
        return response;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

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

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }
}

