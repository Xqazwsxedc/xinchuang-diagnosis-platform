package com.xinchuang.diagnosis.ai.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;
import java.math.BigDecimal;

@TableName("result_knowledge_ref")
public class ResultKnowledgeRef extends BaseEntity {
    private String resultType;
    private Long resultId;
    private Long knowledgeId;
    private String refType;
    private BigDecimal relevanceScore;

    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }
    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }
    public Long getKnowledgeId() { return knowledgeId; }
    public void setKnowledgeId(Long knowledgeId) { this.knowledgeId = knowledgeId; }
    public String getRefType() { return refType; }
    public void setRefType(String refType) { this.refType = refType; }
    public BigDecimal getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(BigDecimal relevanceScore) { this.relevanceScore = relevanceScore; }
}
