package com.xinchuang.diagnosis.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DictTypeSaveRequest {
    @NotBlank(message = "字典编码不能为空")
    @Size(max = 100, message = "字典编码最多100个字符")
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "字典编码需使用大写字母、数字和下划线，并以字母开头")
    private String dictCode;

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 100, message = "字典名称最多100个字符")
    private String dictName;

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "ENABLED|DISABLED", message = "状态只能是 ENABLED/DISABLED")
    private String status;

    @Size(max = 500, message = "备注最多500个字符")
    private String remark;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
