package com.xinchuang.diagnosis.ledger.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xinchuang.diagnosis.common.entity.BaseEntity;

/** 数据库资源的可扩展监控节点，不把 exporter 地址写入资源主表。 */
@TableName("ledger_db_monitor_node")
public class DbMonitorNode extends BaseEntity {
    private Long resourceId; private String nodeName; private String nodeRole; private String privateIp; private String publicIp;
    private String databaseType; private String nodeExporterTarget; private String dbExporterTarget; private String nodeStatus;
    public Long getResourceId(){return resourceId;} public void setResourceId(Long value){resourceId=value;}
    public String getNodeName(){return nodeName;} public void setNodeName(String value){nodeName=value;}
    public String getNodeRole(){return nodeRole;} public void setNodeRole(String value){nodeRole=value;}
    public String getPrivateIp(){return privateIp;} public void setPrivateIp(String value){privateIp=value;}
    public String getPublicIp(){return publicIp;} public void setPublicIp(String value){publicIp=value;}
    public String getDatabaseType(){return databaseType;} public void setDatabaseType(String value){databaseType=value;}
    public String getNodeExporterTarget(){return nodeExporterTarget;} public void setNodeExporterTarget(String value){nodeExporterTarget=value;}
    public String getDbExporterTarget(){return dbExporterTarget;} public void setDbExporterTarget(String value){dbExporterTarget=value;}
    public String getNodeStatus(){return nodeStatus;} public void setNodeStatus(String value){nodeStatus=value;}
}
