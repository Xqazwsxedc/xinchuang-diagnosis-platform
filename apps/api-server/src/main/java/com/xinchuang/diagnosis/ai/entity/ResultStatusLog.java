package com.xinchuang.diagnosis.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;

@TableName("result_status_log")
public class ResultStatusLog extends BaseEntity {
    private String resultType;
    private Long resultId;
    private String beforeStatus;
    private String afterStatus;
    private String operationRemark;
    private Long operatorId;
    private String operatorNameSnapshot;

    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }
    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }
    public String getBeforeStatus() { return beforeStatus; }
    public void setBeforeStatus(String beforeStatus) { this.beforeStatus = beforeStatus; }
    public String getAfterStatus() { return afterStatus; }
    public void setAfterStatus(String afterStatus) { this.afterStatus = afterStatus; }
    public String getOperationRemark() { return operationRemark; }
    public void setOperationRemark(String operationRemark) { this.operationRemark = operationRemark; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getOperatorNameSnapshot() { return operatorNameSnapshot; }
    public void setOperatorNameSnapshot(String operatorNameSnapshot) { this.operatorNameSnapshot = operatorNameSnapshot; }
}
