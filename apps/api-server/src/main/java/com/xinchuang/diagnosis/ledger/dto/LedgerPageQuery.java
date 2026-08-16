package com.xinchuang.diagnosis.ledger.dto;

import com.xinchuang.diagnosis.common.query.PageQuery;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LedgerPageQuery extends PageQuery {
    @Size(max = 100)
    private String name;

    @Size(max = 64)
    private String ownerName;

    @Pattern(regexp = "NORMAL|WARNING|DANGER|OFFLINE", message = "状态只能是 NORMAL/WARNING/DANGER/OFFLINE")
    private String status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
