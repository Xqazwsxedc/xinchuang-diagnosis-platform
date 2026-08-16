package com.xinchuang.diagnosis.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class DictItemSaveRequest {
    @NotBlank(message = "字典编码不能为空")
    @Size(max = 100, message = "字典编码最多100个字符")
    private String dictCode;

    @NotBlank(message = "字典项值不能为空")
    @Size(max = 100, message = "字典项值最多100个字符")
    private String itemValue;

    @NotBlank(message = "字典项名称不能为空")
    @Size(max = 100, message = "字典项名称最多100个字符")
    private String itemLabel;

    @Size(max = 32, message = "标签类型最多32个字符")
    private String tagType;

    @NotNull(message = "排序不能为空")
    private Integer sortOrder;

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

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
