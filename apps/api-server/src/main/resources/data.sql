INSERT IGNORE INTO ledger_db_resource
(id, name, public_ip, private_ip, owner_name, status, created_at, updated_at, deleted)
VALUES
(1900000000000000001, '分布式数据库-01 (192.168.1.10)', '58.48.100.10', '192.168.1.10', '张三', 'NORMAL', NOW(), NOW(), 0),
(1900000000000000002, '分布式数据库-02 (192.168.1.11)', '58.48.100.11', '192.168.1.11', '李四', 'WARNING', NOW(), NOW(), 0),
(1900000000000000003, '分布式数据库-03 (192.168.1.12)', '58.48.100.12', '192.168.1.12', '王五', 'NORMAL', NOW(), NOW(), 0);

INSERT IGNORE INTO ledger_business_system
(id, name, database_id, owner_name, status, access_url, remark, created_at, updated_at, deleted)
VALUES
(1900000000000000101, '核心业务系统', 1900000000000000001, '张三', 'NORMAL', 'http://core.company.com', '核心业务处理系统', NOW(), NOW(), 0),
(1900000000000000102, '财务管理系统', 1900000000000000002, '李四', 'WARNING', 'http://finance.company.com', '财务结算与对账系统', NOW(), NOW(), 0),
(1900000000000000103, '客户关系管理系统', 1900000000000000003, '王五', 'NORMAL', 'http://crm.company.com', '客户关系管理系统', NOW(), NOW(), 0),
(1900000000000000104, '供应链管理系统', 1900000000000000003, '赵六', 'DANGER', NULL, '供应链业务系统', NOW(), NOW(), 0);

INSERT IGNORE INTO bus_system_data_record
(id, system_id, collected_at, total_size_bytes, collect_status, file_name, record_summary, created_at, updated_at, deleted)
VALUES
(1900000000000000301, 1900000000000000101, DATE_SUB(NOW(), INTERVAL 1 DAY), 131386572, 'SUCCESS', 'core-business-data-2024-05-18.csv', '核心交易、订单与客户主数据收集完成。', NOW(), NOW(), 0),
(1900000000000000302, 1900000000000000101, DATE_SUB(NOW(), INTERVAL 2 DAY), 124466995, 'SUCCESS', 'core-business-data-2024-05-17.csv', '核心业务日增量数据收集完成。', NOW(), NOW(), 0),
(1900000000000000303, 1900000000000000101, DATE_SUB(NOW(), INTERVAL 3 DAY), 127401574, 'SUCCESS', 'core-business-data-2024-05-16.csv', '核心业务日增量数据收集完成。', NOW(), NOW(), 0),
(1900000000000000304, 1900000000000000101, DATE_SUB(NOW(), INTERVAL 4 DAY), 117859942, 'FAILED', 'core-business-data-2024-05-15.csv', '演示数据：归档节点响应超时，收集失败。', NOW(), NOW(), 0),
(1900000000000000305, 1900000000000000102, DATE_SUB(NOW(), INTERVAL 1 DAY), 100453580, 'SUCCESS', 'finance-business-data-2024-05-18.csv', '财务结算与对账数据收集完成。', NOW(), NOW(), 0),
(1900000000000000306, 1900000000000000102, DATE_SUB(NOW(), INTERVAL 2 DAY), 95734989, 'SUCCESS', 'finance-business-data-2024-05-17.csv', '财务日终账务数据收集完成。', NOW(), NOW(), 0),
(1900000000000000307, 1900000000000000102, DATE_SUB(NOW(), INTERVAL 3 DAY), 98230272, 'SUCCESS', 'finance-business-data-2024-05-16.csv', '财务对账增量数据收集完成。', NOW(), NOW(), 0),
(1900000000000000308, 1900000000000000102, DATE_SUB(NOW(), INTERVAL 4 DAY), 90391542, 'FAILED', 'finance-business-data-2024-05-15.csv', '演示数据：归档文件校验失败，等待下一轮重试。', NOW(), NOW(), 0);

INSERT IGNORE INTO db_instance_metric
(id, db_instance_id, sampled_at, cpu_usage, memory_usage, disk_iops, cache_hit_rate, created_at, updated_at, deleted)
VALUES
(1900000000000000401, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 23 HOUR), 46.20, 61.40, 980, 84.30, NOW(), NOW(), 0),
(1900000000000000402, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 21 HOUR), 68.50, 73.20, 1210, 85.10, NOW(), NOW(), 0),
(1900000000000000403, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 19 HOUR), 60.30, 64.90, 1105, 84.80, NOW(), NOW(), 0),
(1900000000000000404, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 17 HOUR), 72.60, 75.10, 1320, 86.20, NOW(), NOW(), 0),
(1900000000000000405, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 15 HOUR), 55.80, 68.30, 1020, 83.70, NOW(), NOW(), 0),
(1900000000000000406, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 13 HOUR), 63.40, 70.20, 1195, 85.60, NOW(), NOW(), 0),
(1900000000000000407, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 11 HOUR), 47.90, 60.60, 940, 82.90, NOW(), NOW(), 0),
(1900000000000000408, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 9 HOUR), 66.70, 72.40, 1250, 85.00, NOW(), NOW(), 0),
(1900000000000000409, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 7 HOUR), 58.20, 66.80, 1090, 84.10, NOW(), NOW(), 0),
(1900000000000000410, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 5 HOUR), 69.40, 74.60, 1380, 86.40, NOW(), NOW(), 0),
(1900000000000000411, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 3 HOUR), 52.60, 63.10, 1010, 83.80, NOW(), NOW(), 0),
(1900000000000000412, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 1 HOUR), 45.20, 67.80, 1250, 85.00, NOW(), NOW(), 0),
(1900000000000000413, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 45 MINUTE), 62.80, 65.40, 1180, 84.10, NOW(), NOW(), 0),
(1900000000000000414, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 30 MINUTE), 56.30, 71.20, 1305, 85.60, NOW(), NOW(), 0),
(1900000000000000415, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 15 MINUTE), 45.20, 67.80, 1250, 85.00, NOW(), NOW(), 0);

INSERT IGNORE INTO db_instance_log
(id, db_instance_id, logged_at, log_level, log_content, created_at, updated_at, deleted)
VALUES
(1900000000000000501, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 55 MINUTE), 'INFO', '演示日志：数据库实例健康检查完成，连接池状态正常。', NOW(), NOW(), 0),
(1900000000000000502, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 3 HOUR), 'WARN', '演示日志：检测到瞬时磁盘 IO 峰值，已恢复至正常阈值。', NOW(), NOW(), 0),
(1900000000000000503, 1900000000000000001, DATE_SUB(NOW(), INTERVAL 7 HOUR), 'INFO', '演示日志：业务数据同步任务执行完成。', NOW(), NOW(), 0),
(1900000000000000504, 1900000000000000002, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'WARN', '演示日志：慢 SQL 采样数量高于预警阈值。', NOW(), NOW(), 0);

INSERT IGNORE INTO sys_role
(id, role_code, role_name, status, remark, created_at, updated_at, deleted)
VALUES
(1900000000000001001, 'admin', '系统管理员', 'ENABLED', '拥有系统管理权限，菜单和按钮权限后续扩展', NOW(), NOW(), 0),
(1900000000000001002, 'operator', '运维操作员', 'ENABLED', '负责台账维护、告警处理和诊断任务操作', NOW(), NOW(), 0),
(1900000000000001003, 'viewer', '只读查看员', 'ENABLED', '只读查看数据大屏、台账和报表', NOW(), NOW(), 0);

INSERT IGNORE INTO ai_model
(id, model_code, model_name, model_type, model_version, provider, description, status, created_at, updated_at, deleted)
VALUES
(1900000000000003001, 'DEMO-FAULT-MODEL-001', '演示-故障预测轻量模型', 'FAULT_PREDICTION', 'v1.0-demo', '演示供应方', '本条为演示数据，用于展示故障预测任务选择模型。', 'ENABLED', NOW(), NOW(), 0),
(1900000000000003002, 'DEMO-DEFECT-MODEL-001', '演示-缺陷检测规则模型', 'DEFECT_DETECTION', 'v1.0-demo', '演示供应方', '本条为演示数据，用于展示缺陷检测任务选择模型。', 'ENABLED', NOW(), NOW(), 0),
(1900000000000003003, 'DEMO-OPT-MODEL-001', '演示-性能优化建议模型', 'PERFORMANCE_OPTIMIZATION', 'v1.0-demo', '演示供应方', '本条为演示数据，用于展示性能优化任务选择模型。', 'ENABLED', NOW(), NOW(), 0);

INSERT IGNORE INTO kb_instance
(id, knowledge_code, knowledge_name, knowledge_type, knowledge_content, attachment_names, source_type, status, version, created_at, updated_at, deleted)
VALUES
(1900000000000003101, 'DEMO-FAULT-KB-001', '演示-数据库连接池耗尽风险知识', 'FAULT', '本条为演示数据。适用对象：核心业务系统；样例现象：连接池使用率超过 90%，等待线程持续上升；建议：排查长连接、释放异常空闲连接、临时扩容连接池。', '演示附件-连接池风险说明.pdf', 'MANUAL', 'ENABLED', 1, NOW(), NOW(), 0),
(1900000000000003102, 'DEMO-DEFECT-KB-001', '演示-慢 SQL 索引缺失缺陷知识', 'DEFECT', '本条为演示数据。适用对象：财务管理系统；样例现象：高频查询出现全表扫描；建议：补充组合索引，复核查询条件选择性。', '演示附件-慢SQL缺陷样例.docx', 'MANUAL', 'ENABLED', 1, NOW(), NOW(), 0),
(1900000000000003103, 'DEMO-OPT-KB-001', '演示-SQL 执行计划优化知识', 'OPTIMIZATION', '本条为演示数据。适用对象：分布式数据库；样例问题：执行计划未命中索引；建议：更新统计信息、调整索引、拆分大事务。', '演示附件-执行计划优化.xlsx', 'MANUAL', 'ENABLED', 1, NOW(), NOW(), 0);

INSERT IGNORE INTO ai_task
(id, task_code, task_name, task_type, model_id, model_name_snapshot, target_type, target_id, target_name_snapshot, metric_codes, param_json, schedule_rule, status, last_exec_time, next_exec_time, execution_count, result_count, created_at, updated_at, deleted)
VALUES
(1900000000000003201, 'DEMO-FAULT-TASK-001', '演示-核心业务系统故障预测任务', 'FAULT_PREDICTION', 1900000000000003001, '演示-故障预测轻量模型', 'BUSINESS_SYSTEM', 1900000000000000101, '核心业务系统', 'CPU,内存,数据库连接数,SQL响应时长,锁等待次数', '{"demo":true,"note":"本条为演示数据"}', '[{"time":"08:00","repeatType":"daily","weekDays":[],"predictionWindowHours":4,"demo":true}]', 'ENABLED', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 3, 2, NOW(), NOW(), 0),
(1900000000000003202, 'DEMO-DEFECT-TASK-001', '演示-财务系统缺陷检测任务', 'DEFECT_DETECTION', 1900000000000003002, '演示-缺陷检测规则模型', 'BUSINESS_SYSTEM', 1900000000000000102, '财务管理系统', '慢SQL数量,锁等待次数,索引命中率', '{"demo":true,"note":"本条为演示数据"}', '[{"time":"12:00","repeatType":"daily","demo":true}]', 'ENABLED', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 2, 2, NOW(), NOW(), 0),
(1900000000000003203, 'DEMO-OPT-TASK-001', '演示-分布式数据库性能优化任务', 'PERFORMANCE_OPTIMIZATION', 1900000000000003003, '演示-性能优化建议模型', 'DB_RESOURCE', 1900000000000000002, '分布式数据库-02 (192.168.1.11)', 'SQL响应时长,缓存命中率,磁盘IO', '{"demo":true,"note":"本条为演示数据"}', '[{"time":"20:00","repeatType":"daily","demo":true}]', 'ENABLED', NOW(), DATE_ADD(NOW(), INTERVAL 1 DAY), 2, 2, NOW(), NOW(), 0);

INSERT IGNORE INTO task_exec_record
(id, task_id, task_type, task_name_snapshot, execution_status, trigger_type, start_time, end_time, result_count, error_message, created_at, updated_at, deleted)
VALUES
(1900000000000003301, 1900000000000003201, 'FAULT_PREDICTION', '演示-核心业务系统故障预测任务', 'SUCCESS', 'SCHEDULED', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 119 MINUTE), 2, NULL, NOW(), NOW(), 0),
(1900000000000003302, 1900000000000003202, 'DEFECT_DETECTION', '演示-财务系统缺陷检测任务', 'SUCCESS', 'MANUAL', DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_SUB(NOW(), INTERVAL 178 MINUTE), 2, NULL, NOW(), NOW(), 0),
(1900000000000003303, 1900000000000003203, 'PERFORMANCE_OPTIMIZATION', '演示-分布式数据库性能优化任务', 'SUCCESS', 'SCHEDULED', DATE_SUB(NOW(), INTERVAL 4 HOUR), DATE_SUB(NOW(), INTERVAL 238 MINUTE), 2, NULL, NOW(), NOW(), 0);

INSERT IGNORE INTO fault_report
(id, report_code, task_id, execution_id, target_type, target_id, target_name_snapshot, alert_time, indicator_category, fault_type, risk_level, expected_start_time, expected_end_time, confidence, forecast_description, handle_suggestion, trend_json, process_status, created_at, updated_at, deleted)
VALUES
(1900000000000003401, 'DEMO-FAULT-ALERT-001', 1900000000000003201, 1900000000000003301, 'BUSINESS_SYSTEM', 1900000000000000101, '核心业务系统', DATE_SUB(NOW(), INTERVAL 90 MINUTE), '数据库连接池', '演示-连接池耗尽风险', 'HIGH', NOW(), DATE_ADD(NOW(), INTERVAL 2 HOUR), 92.50, '本条为演示数据。连接池使用率持续升高，等待线程数超过阈值。', '本条为演示数据。建议排查长连接、释放异常空闲连接，并临时扩容连接池上限。', '[{"time":"-15min","value":70},{"time":"-10min","value":82},{"time":"-5min","value":90},{"time":"0min","value":93}]', 'PENDING', NOW(), NOW(), 0),
(1900000000000003402, 'DEMO-FAULT-ALERT-002', 1900000000000003201, 1900000000000003301, 'DB_RESOURCE', 1900000000000000002, '分布式数据库-02 (192.168.1.11)', DATE_SUB(NOW(), INTERVAL 60 MINUTE), '慢SQL', '演示-慢 SQL 堆积风险', 'MEDIUM', NOW(), DATE_ADD(NOW(), INTERVAL 1 HOUR), 81.30, '本条为演示数据。慢 SQL 数量和锁等待次数持续升高。', '本条为演示数据。建议检查高频查询索引和批量任务锁等待情况。', '[{"time":"-15min","value":35},{"time":"-10min","value":52},{"time":"-5min","value":68},{"time":"0min","value":81}]', 'CONFIRMED', NOW(), NOW(), 0);

INSERT IGNORE INTO defect_report
(id, defect_code, task_id, execution_id, task_name_snapshot, target_type, target_id, target_name_snapshot, defect_type, severity_level, defect_problem, defect_data, defect_description, repair_suggestion, process_status, created_at, updated_at, deleted)
VALUES
(1900000000000003501, 'DEMO-DEFECT-001', 1900000000000003202, 1900000000000003302, '演示-财务系统缺陷检测任务', 'BUSINESS_SYSTEM', 1900000000000000102, '财务管理系统', '演示-索引缺失', 'HIGH', '本条为演示数据。payment_record 查询存在全表扫描风险。', '{"table":"payment_record","demo":true}', '本条为演示数据。高频查询未命中组合索引，月结期间可能放大响应时间。', '本条为演示数据。建议增加 tenant_id + bill_month + status 组合索引。', 'PENDING', NOW(), NOW(), 0),
(1900000000000003502, 'DEMO-DEFECT-002', 1900000000000003202, 1900000000000003302, '演示-财务系统缺陷检测任务', 'DB_RESOURCE', 1900000000000000002, '分布式数据库-02 (192.168.1.11)', '演示-日志增长异常', 'MEDIUM', '本条为演示数据。归档日志增长速率超过演示阈值。', '{"logGrowth":"demo-high"}', '本条为演示数据。日志清理策略可能未按预期执行。', '本条为演示数据。建议检查归档策略和日志保留周期。', 'FALSE_POSITIVE', NOW(), NOW(), 0);

INSERT IGNORE INTO opt_report
(id, report_code, task_id, execution_id, task_name_snapshot, target_type, target_id, target_name_snapshot, optimization_type, optimization_problem, sql_text, sql_fingerprint, execution_plan_json, slow_sql_metrics_json, optimization_content, confidence, process_status, execute_result, before_metrics_json, after_metrics_json, created_at, updated_at, deleted)
VALUES
(1900000000000003601, 'DEMO-OPT-001', 1900000000000003203, 1900000000000003303, '演示-分布式数据库性能优化任务', 'DB_RESOURCE', 1900000000000000002, '分布式数据库-02 (192.168.1.11)', '演示-SQL索引优化', '本条为演示数据。订单明细查询响应时间偏高。', 'SELECT * FROM order_detail WHERE tenant_id = ? AND order_time >= ? ORDER BY order_time DESC', 'demo-sql-fingerprint-001', '{"demo":true,"plan":"FULL TABLE SCAN"}', '{"avgTimeMs":4200,"demo":true}', '本条为演示数据。建议增加 tenant_id + order_time 组合索引，并避免 SELECT *。', 88.60, 'PENDING', '本条为演示数据，未真实执行 SQL。', '{"avgTimeMs":4200}', '{"avgTimeMs":900}', NOW(), NOW(), 0),
(1900000000000003602, 'DEMO-OPT-002', 1900000000000003203, 1900000000000003303, '演示-分布式数据库性能优化任务', 'BUSINESS_SYSTEM', 1900000000000000104, '供应链管理系统', '演示-批量写入优化', '本条为演示数据。批量写库吞吐量低于演示阈值。', NULL, 'demo-write-fingerprint-002', '{"demo":true}', '{"throughput":120,"demo":true}', '本条为演示数据。建议批量提交、降低单事务体量，并错峰执行。', 76.20, 'CONFIRMED', '本条为演示数据，作为建议确认展示。', '{"throughput":120}', '{"throughput":360}', NOW(), NOW(), 0);

INSERT IGNORE INTO result_status_log
(id, result_type, result_id, before_status, after_status, operation_remark, operator_id, operator_name_snapshot, created_at, updated_at, deleted)
VALUES
(1900000000000003701, 'FAULT', 1900000000000003402, 'PENDING', 'CONFIRMED', '本条为演示数据：模拟确认故障预警。', 1900000000000001101, '系统管理员', NOW(), NOW(), 0),
(1900000000000003702, 'DEFECT', 1900000000000003502, 'PENDING', 'FALSE_POSITIVE', '本条为演示数据：模拟缺陷误报处理。', 1900000000000001101, '系统管理员', NOW(), NOW(), 0),
(1900000000000003703, 'OPTIMIZATION', 1900000000000003602, 'PENDING', 'CONFIRMED', '本条为演示数据：模拟确认优化建议。', 1900000000000001101, '系统管理员', NOW(), NOW(), 0);

INSERT IGNORE INTO result_knowledge_ref
(id, result_type, result_id, knowledge_id, ref_type, relevance_score, created_at, updated_at, deleted)
VALUES
(1900000000000003801, 'FAULT', 1900000000000003401, 1900000000000003101, 'REFERENCED', 91.00, NOW(), NOW(), 0),
(1900000000000003802, 'DEFECT', 1900000000000003501, 1900000000000003102, 'REFERENCED', 88.00, NOW(), NOW(), 0),
(1900000000000003803, 'OPTIMIZATION', 1900000000000003601, 1900000000000003103, 'REFERENCED', 86.00, NOW(), NOW(), 0);

-- 扩展拟真演示数据（稳定 ID + 相对当前时间，重复启动幂等）
INSERT IGNORE INTO ledger_db_resource
(id, name, public_ip, private_ip, owner_name, status, monitor_status, monitor_message, monitor_last_checked_at, created_at, updated_at, deleted)
VALUES
(1900000000000000004, '政务云数据库-01 (192.168.2.21)', '58.48.101.21', '192.168.2.21', '陈晨', 'NORMAL', 'NORMAL', '监控接入正常', DATE_SUB(NOW(), INTERVAL 3 MINUTE), NOW(), NOW(), 0),
(1900000000000000005, '政务云数据库-02 (192.168.2.22)', '58.48.101.22', '192.168.2.22', '周宁', 'WARNING', 'ABNORMAL', '数据库 exporter 最近一次采集延迟', DATE_SUB(NOW(), INTERVAL 12 MINUTE), NOW(), NOW(), 0),
(1900000000000000006, '分析平台数据库-01 (192.168.3.31)', '58.48.102.31', '192.168.3.31', '孙凯', 'NORMAL', 'NORMAL', '监控接入正常', DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW(), NOW(), 0),
(1900000000000000007, '客户中心数据库-01 (192.168.3.32)', '58.48.102.32', '192.168.3.32', '刘洋', 'DANGER', 'ABNORMAL', '磁盘使用率超过 85%', DATE_SUB(NOW(), INTERVAL 8 MINUTE), NOW(), NOW(), 0),
(1900000000000000008, '灾备中心数据库-01 (10.20.1.41)', '58.48.103.41', '10.20.1.41', '赵敏', 'OFFLINE', 'ABNORMAL', '灾备链路维护中', DATE_SUB(NOW(), INTERVAL 45 MINUTE), NOW(), NOW(), 0),
(1900000000000000009, '统一消息数据库-01 (10.20.1.42)', '58.48.103.42', '10.20.1.42', '黄伟', 'NORMAL', 'NORMAL', '监控接入正常', DATE_SUB(NOW(), INTERVAL 2 MINUTE), NOW(), NOW(), 0);

INSERT IGNORE INTO ledger_db_monitor_node
(id, resource_id, node_name, node_role, private_ip, public_ip, database_type, node_exporter_target, db_exporter_target, node_status, created_at, updated_at, deleted)
VALUES
(1900000000000000011, 1900000000000000004, '政务云数据库-01-primary', 'PRIMARY', '192.168.2.21', '58.48.101.21', 'openGauss', '192.168.2.21:9100', '192.168.2.21:9187', 'NORMAL', NOW(), NOW(), 0),
(1900000000000000012, 1900000000000000005, '政务云数据库-02-primary', 'PRIMARY', '192.168.2.22', '58.48.101.22', 'openGauss', '192.168.2.22:9100', '192.168.2.22:9187', 'WARNING', NOW(), NOW(), 0),
(1900000000000000013, 1900000000000000006, '分析平台数据库-01-primary', 'PRIMARY', '192.168.3.31', '58.48.102.31', '达梦', '192.168.3.31:9100', '192.168.3.31:9161', 'NORMAL', NOW(), NOW(), 0),
(1900000000000000014, 1900000000000000007, '客户中心数据库-01-primary', 'PRIMARY', '192.168.3.32', '58.48.102.32', '人大金仓', '192.168.3.32:9100', '192.168.3.32:9187', 'WARNING', NOW(), NOW(), 0),
(1900000000000000015, 1900000000000000008, '灾备中心数据库-01-standby', 'STANDBY', '10.20.1.41', '58.48.103.41', 'openGauss', '10.20.1.41:9100', '10.20.1.41:9187', 'OFFLINE', NOW(), NOW(), 0),
(1900000000000000016, 1900000000000000009, '统一消息数据库-01-primary', 'PRIMARY', '10.20.1.42', '58.48.103.42', '达梦', '10.20.1.42:9100', '10.20.1.42:9161', 'NORMAL', NOW(), NOW(), 0);

INSERT IGNORE INTO ledger_db_monitoring_config
(id, resource_id, prometheus_base_url, prometheus_auth_ref, node_target_selector, database_target_selector, database_type, status, last_checked_at, last_error_message, created_at, updated_at, deleted)
VALUES
(1900000000000000021, 1900000000000000004, 'http://prometheus.monitoring.svc:9090', 'prom-demo', '{"instance":"192.168.2.21:9100"}', '{"instance":"192.168.2.21:9187"}', 'openGauss', 'HEALTHY', DATE_SUB(NOW(), INTERVAL 3 MINUTE), NULL, NOW(), NOW(), 0),
(1900000000000000022, 1900000000000000005, 'http://prometheus.monitoring.svc:9090', 'prom-demo', '{"instance":"192.168.2.22:9100"}', '{"instance":"192.168.2.22:9187"}', 'openGauss', 'ABNORMAL', DATE_SUB(NOW(), INTERVAL 12 MINUTE), '最近一次抓取超时', NOW(), NOW(), 0),
(1900000000000000023, 1900000000000000006, 'http://prometheus.monitoring.svc:9090', 'prom-demo', '{"instance":"192.168.3.31:9100"}', '{"instance":"192.168.3.31:9161"}', '达梦', 'HEALTHY', DATE_SUB(NOW(), INTERVAL 5 MINUTE), NULL, NOW(), NOW(), 0),
(1900000000000000024, 1900000000000000007, 'http://prometheus.monitoring.svc:9090', 'prom-demo', '{"instance":"192.168.3.32:9100"}', '{"instance":"192.168.3.32:9187"}', '人大金仓', 'ABNORMAL', DATE_SUB(NOW(), INTERVAL 8 MINUTE), '磁盘指标采集失败', NOW(), NOW(), 0),
(1900000000000000025, 1900000000000000008, 'http://prometheus-dr.svc:9090', 'prom-demo', '{"instance":"10.20.1.41:9100"}', '{"instance":"10.20.1.41:9187"}', 'openGauss', 'ABNORMAL', DATE_SUB(NOW(), INTERVAL 45 MINUTE), '灾备链路维护中', NOW(), NOW(), 0),
(1900000000000000026, 1900000000000000009, 'http://prometheus-dr.svc:9090', 'prom-demo', '{"instance":"10.20.1.42:9100"}', '{"instance":"10.20.1.42:9161"}', '达梦', 'HEALTHY', DATE_SUB(NOW(), INTERVAL 2 MINUTE), NULL, NOW(), NOW(), 0);

INSERT IGNORE INTO ledger_business_system
(id, name, database_id, owner_name, status, access_url, remark, created_at, updated_at, deleted)
VALUES
(1900000000000000105, '政务服务统一门户', 1900000000000000004, '陈晨', 'NORMAL', 'https://zwfw.example.com', '面向公众的一站式政务服务门户', NOW(), NOW(), 0),
(1900000000000000106, '移动支付清算平台', 1900000000000000005, '周宁', 'WARNING', 'https://pay.example.com', '支付订单、清算与渠道对账', NOW(), NOW(), 0),
(1900000000000000107, '数据分析与报表平台', 1900000000000000006, '孙凯', 'NORMAL', 'https://bi.example.com', '经营分析和监管报表生成', NOW(), NOW(), 0),
(1900000000000000108, '客户服务工单中心', 1900000000000000007, '刘洋', 'DANGER', 'https://service.example.com', '客服工单、回访和服务质量管理', NOW(), NOW(), 0),
(1900000000000000109, '异地灾备管理平台', 1900000000000000008, '赵敏', 'OFFLINE', NULL, '灾备演练、复制链路和切换管理', NOW(), NOW(), 0),
(1900000000000000110, '统一消息与通知中心', 1900000000000000009, '黄伟', 'NORMAL', 'https://msg.example.com', '短信、邮件和站内信统一发送', NOW(), NOW(), 0),
(1900000000000000111, '电子档案管理系统', 1900000000000000004, '马丽', 'NORMAL', 'https://archive.example.com', '电子档案归档、检索和借阅', NOW(), NOW(), 0),
(1900000000000000112, '采购协同门户', 1900000000000000006, '高峰', 'WARNING', 'https://procure.example.com', '供应商协同和采购订单管理', NOW(), NOW(), 0);

INSERT IGNORE INTO ledger_business_system_db
(id, business_system_id, db_resource_id, relation_type, created_at, updated_at, deleted)
VALUES
(1900000000000000121, 1900000000000000105, 1900000000000000004, 'PRIMARY', NOW(), NOW(), 0),
(1900000000000000122, 1900000000000000106, 1900000000000000005, 'PRIMARY', NOW(), NOW(), 0),
(1900000000000000123, 1900000000000000107, 1900000000000000006, 'PRIMARY', NOW(), NOW(), 0),
(1900000000000000124, 1900000000000000108, 1900000000000000007, 'PRIMARY', NOW(), NOW(), 0),
(1900000000000000125, 1900000000000000109, 1900000000000000008, 'STANDBY', NOW(), NOW(), 0),
(1900000000000000126, 1900000000000000110, 1900000000000000009, 'PRIMARY', NOW(), NOW(), 0),
(1900000000000000127, 1900000000000000111, 1900000000000000004, 'PRIMARY', NOW(), NOW(), 0),
(1900000000000000128, 1900000000000000112, 1900000000000000006, 'PRIMARY', NOW(), NOW(), 0);

INSERT IGNORE INTO bus_system_data_record
(id, system_id, collected_at, total_size_bytes, collect_status, file_name, record_summary, created_at, updated_at, deleted)
VALUES
(1900000000000000311, 1900000000000000105, DATE_SUB(NOW(), INTERVAL 1 DAY), 238445901, 'SUCCESS', 'gov-portal-20260901.csv', '办件、用户与电子证照增量采集完成。', NOW(), NOW(), 0),
(1900000000000000312, 1900000000000000105, DATE_SUB(NOW(), INTERVAL 2 DAY), 231884012, 'SUCCESS', 'gov-portal-20260831.csv', '政务服务日终数据采集完成。', NOW(), NOW(), 0),
(1900000000000000313, 1900000000000000106, DATE_SUB(NOW(), INTERVAL 1 DAY), 486214770, 'SUCCESS', 'payment-clearing-20260901.csv', '支付订单与渠道清算数据校验通过。', NOW(), NOW(), 0),
(1900000000000000314, 1900000000000000106, DATE_SUB(NOW(), INTERVAL 2 DAY), 472998311, 'FAILED', 'payment-clearing-20260831.csv', '演示数据：渠道对账文件晚到，等待重试。', NOW(), NOW(), 0),
(1900000000000000315, 1900000000000000107, DATE_SUB(NOW(), INTERVAL 1 DAY), 912884331, 'SUCCESS', 'bi-report-20260901.parquet', '经营指标和监管报表数据汇总完成。', NOW(), NOW(), 0),
(1900000000000000316, 1900000000000000108, DATE_SUB(NOW(), INTERVAL 1 DAY), 182774099, 'SUCCESS', 'service-ticket-20260901.csv', '工单、回访和满意度数据采集完成。', NOW(), NOW(), 0),
(1900000000000000317, 1900000000000000109, DATE_SUB(NOW(), INTERVAL 1 DAY), 66420128, 'FAILED', 'dr-replication-20260901.log', '演示数据：灾备链路维护，采集任务暂停。', NOW(), NOW(), 0),
(1900000000000000318, 1900000000000000110, DATE_SUB(NOW(), INTERVAL 1 DAY), 305119832, 'SUCCESS', 'message-center-20260901.csv', '通知发送回执与模板数据收集完成。', NOW(), NOW(), 0),
(1900000000000000319, 1900000000000000111, DATE_SUB(NOW(), INTERVAL 1 DAY), 128551093, 'SUCCESS', 'archive-index-20260901.csv', '档案目录和借阅记录采集完成。', NOW(), NOW(), 0),
(1900000000000000320, 1900000000000000112, DATE_SUB(NOW(), INTERVAL 1 DAY), 275884322, 'SUCCESS', 'procure-order-20260901.csv', '采购订单和供应商主数据采集完成。', NOW(), NOW(), 0);

INSERT IGNORE INTO db_instance_metric
(id, db_instance_id, sampled_at, cpu_usage, memory_usage, disk_iops, cache_hit_rate, created_at, updated_at, deleted)
VALUES
(1900000000000000416,1900000000000000004,DATE_SUB(NOW(),INTERVAL 6 HOUR),42.10,58.20,820,96.40,NOW(),NOW(),0),(1900000000000000417,1900000000000000004,DATE_SUB(NOW(),INTERVAL 4 HOUR),48.60,61.30,910,96.10,NOW(),NOW(),0),(1900000000000000418,1900000000000000004,DATE_SUB(NOW(),INTERVAL 2 HOUR),55.20,63.80,1050,95.70,NOW(),NOW(),0),(1900000000000000419,1900000000000000004,DATE_SUB(NOW(),INTERVAL 20 MINUTE),46.80,60.90,880,96.20,NOW(),NOW(),0),
(1900000000000000420,1900000000000000005,DATE_SUB(NOW(),INTERVAL 6 HOUR),67.40,72.80,1380,91.20,NOW(),NOW(),0),(1900000000000000421,1900000000000000005,DATE_SUB(NOW(),INTERVAL 4 HOUR),74.90,78.40,1660,89.80,NOW(),NOW(),0),(1900000000000000422,1900000000000000005,DATE_SUB(NOW(),INTERVAL 2 HOUR),82.30,84.10,2050,87.60,NOW(),NOW(),0),(1900000000000000423,1900000000000000005,DATE_SUB(NOW(),INTERVAL 20 MINUTE),76.10,80.20,1810,88.90,NOW(),NOW(),0),
(1900000000000000424,1900000000000000006,DATE_SUB(NOW(),INTERVAL 6 HOUR),35.20,54.70,720,97.80,NOW(),NOW(),0),(1900000000000000425,1900000000000000006,DATE_SUB(NOW(),INTERVAL 4 HOUR),39.80,56.10,760,97.60,NOW(),NOW(),0),(1900000000000000426,1900000000000000006,DATE_SUB(NOW(),INTERVAL 2 HOUR),44.30,59.40,840,97.20,NOW(),NOW(),0),(1900000000000000427,1900000000000000006,DATE_SUB(NOW(),INTERVAL 20 MINUTE),41.70,57.90,790,97.50,NOW(),NOW(),0),
(1900000000000000428,1900000000000000007,DATE_SUB(NOW(),INTERVAL 6 HOUR),71.80,79.20,1490,90.40,NOW(),NOW(),0),(1900000000000000429,1900000000000000007,DATE_SUB(NOW(),INTERVAL 4 HOUR),79.60,82.50,1730,89.10,NOW(),NOW(),0),(1900000000000000430,1900000000000000007,DATE_SUB(NOW(),INTERVAL 2 HOUR),88.40,87.30,2240,86.70,NOW(),NOW(),0),(1900000000000000431,1900000000000000007,DATE_SUB(NOW(),INTERVAL 20 MINUTE),91.20,89.10,2510,85.90,NOW(),NOW(),0),
(1900000000000000432,1900000000000000008,DATE_SUB(NOW(),INTERVAL 6 HOUR),12.00,41.30,210,98.90,NOW(),NOW(),0),(1900000000000000433,1900000000000000008,DATE_SUB(NOW(),INTERVAL 4 HOUR),10.40,40.80,180,99.10,NOW(),NOW(),0),(1900000000000000434,1900000000000000008,DATE_SUB(NOW(),INTERVAL 2 HOUR),0.00,40.80,0,99.10,NOW(),NOW(),0),(1900000000000000435,1900000000000000008,DATE_SUB(NOW(),INTERVAL 20 MINUTE),0.00,40.80,0,99.10,NOW(),NOW(),0),
(1900000000000000436,1900000000000000009,DATE_SUB(NOW(),INTERVAL 6 HOUR),51.20,65.30,1020,94.70,NOW(),NOW(),0),(1900000000000000437,1900000000000000009,DATE_SUB(NOW(),INTERVAL 4 HOUR),57.80,68.20,1170,94.10,NOW(),NOW(),0),(1900000000000000438,1900000000000000009,DATE_SUB(NOW(),INTERVAL 2 HOUR),63.40,70.10,1290,93.60,NOW(),NOW(),0),(1900000000000000439,1900000000000000009,DATE_SUB(NOW(),INTERVAL 20 MINUTE),54.90,66.80,1100,94.30,NOW(),NOW(),0);

INSERT IGNORE INTO db_instance_log
(id, db_instance_id, logged_at, log_level, log_content, created_at, updated_at, deleted)
VALUES
(1900000000000000511,1900000000000000004,DATE_SUB(NOW(),INTERVAL 25 MINUTE),'INFO','政务服务门户连接池巡检完成，活跃连接 126/400。',NOW(),NOW(),0),(1900000000000000512,1900000000000000004,DATE_SUB(NOW(),INTERVAL 2 HOUR),'INFO','备份窗口完成，增量日志归档 2.3GB。',NOW(),NOW(),0),(1900000000000000513,1900000000000000005,DATE_SUB(NOW(),INTERVAL 18 MINUTE),'WARN','支付清算批处理出现慢 SQL，平均响应 2.8 秒。',NOW(),NOW(),0),(1900000000000000514,1900000000000000005,DATE_SUB(NOW(),INTERVAL 3 HOUR),'ERROR','数据库 exporter 抓取超时，已进入重试队列。',NOW(),NOW(),0),(1900000000000000515,1900000000000000006,DATE_SUB(NOW(),INTERVAL 35 MINUTE),'INFO','报表平台分区统计信息更新成功。',NOW(),NOW(),0),(1900000000000000516,1900000000000000006,DATE_SUB(NOW(),INTERVAL 5 HOUR),'INFO','夜间 ETL 作业完成，写入 128 万行。',NOW(),NOW(),0),(1900000000000000517,1900000000000000007,DATE_SUB(NOW(),INTERVAL 12 MINUTE),'ERROR','客户工单查询磁盘空间不足 15%，自动扩容任务已创建。',NOW(),NOW(),0),(1900000000000000518,1900000000000000007,DATE_SUB(NOW(),INTERVAL 1 HOUR),'WARN','检测到锁等待会话 18 个，最长等待 43 秒。',NOW(),NOW(),0),(1900000000000000519,1900000000000000008,DATE_SUB(NOW(),INTERVAL 42 MINUTE),'WARN','灾备复制链路进入维护模式，暂停 WAL 发送。',NOW(),NOW(),0),(1900000000000000520,1900000000000000008,DATE_SUB(NOW(),INTERVAL 20 MINUTE),'ERROR','健康检查未收到 standby 响应。',NOW(),NOW(),0),(1900000000000000521,1900000000000000009,DATE_SUB(NOW(),INTERVAL 8 MINUTE),'INFO','消息发送回执消费延迟恢复至 120ms。',NOW(),NOW(),0),(1900000000000000522,1900000000000000009,DATE_SUB(NOW(),INTERVAL 4 HOUR),'WARN','短信供应商通道切换次数超过日常水平。',NOW(),NOW(),0);

INSERT IGNORE INTO ai_model (id,model_code,model_name,model_type,model_version,provider,description,status,created_at,updated_at,deleted) VALUES
(1900000000000003004,'FAULT-XG-2026','故障预测梯度增强模型','FAULT_PREDICTION','v2.3.1','信创智能实验室','连接池、锁等待和磁盘水位多指标预测。','ENABLED',NOW(),NOW(),0),
(1900000000000003005,'DEFECT-RULE-2026','SQL缺陷规则引擎','DEFECT_DETECTION','v1.8.0','数据库质量中心','融合执行计划和慢 SQL 样本的规则引擎。','ENABLED',NOW(),NOW(),0),
(1900000000000003006,'OPT-LLM-2026','性能优化大模型助手','PERFORMANCE_OPTIMIZATION','v3.0-beta','平台算法团队','结合 SQL 指纹生成可审计优化建议。','DISABLED',NOW(),NOW(),0);

INSERT IGNORE INTO ai_task (id,task_code,task_name,task_type,model_id,model_name_snapshot,target_type,target_id,target_name_snapshot,metric_codes,param_json,schedule_rule,status,last_exec_time,next_exec_time,execution_count,result_count,created_at,updated_at,deleted) VALUES
(1900000000000003204,'FAULT-GOV-001','政务门户连接池风险预测','FAULT_PREDICTION',1900000000000003004,'故障预测梯度增强模型','BUSINESS_SYSTEM',1900000000000000105,'政务服务统一门户','CPU,内存,数据库连接数,锁等待次数','{"windowHours":6}','[{"time":"07:30","repeatType":"daily"}]','ENABLED',DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_ADD(NOW(),INTERVAL 23 HOUR),18,3,NOW(),NOW(),0),
(1900000000000003205,'FAULT-CUSTOMER-001','客户中心磁盘水位预测','FAULT_PREDICTION',1900000000000003004,'故障预测梯度增强模型','DB_RESOURCE',1900000000000000007,'客户中心数据库-01 (192.168.3.32)','磁盘使用率,磁盘IOPS,写入延迟','{"windowHours":4}','[{"time":"09:00","repeatType":"daily"}]','ENABLED',DATE_SUB(NOW(),INTERVAL 3 HOUR),DATE_ADD(NOW(),INTERVAL 6 HOUR),12,2,NOW(),NOW(),0),
(1900000000000003206,'DEFECT-PAY-001','支付清算慢 SQL 检测','DEFECT_DETECTION',1900000000000003005,'SQL缺陷规则引擎','BUSINESS_SYSTEM',1900000000000000106,'移动支付清算平台','慢SQL数量,索引命中率,锁等待次数','{"sampleMinutes":30}','[{"time":"12:00","repeatType":"daily"}]','ENABLED',DATE_SUB(NOW(),INTERVAL 2 HOUR),DATE_ADD(NOW(),INTERVAL 10 HOUR),21,4,NOW(),NOW(),0),
(1900000000000003207,'DEFECT-ARCHIVE-001','电子档案表结构巡检','DEFECT_DETECTION',1900000000000003005,'SQL缺陷规则引擎','BUSINESS_SYSTEM',1900000000000000111,'电子档案管理系统','索引命中率,表膨胀率,全表扫描次数','{"ignoreTables":["tmp_*"]}','[{"time":"03:00","repeatType":"weekly"}]','DISABLED',DATE_SUB(NOW(),INTERVAL 2 DAY),NULL,6,1,NOW(),NOW(),0),
(1900000000000003208,'OPT-BI-001','报表平台聚合查询优化','PERFORMANCE_OPTIMIZATION',1900000000000003006,'性能优化大模型助手','BUSINESS_SYSTEM',1900000000000000107,'数据分析与报表平台','SQL响应时长,扫描行数,缓存命中率','{"topN":20}','[{"time":"22:00","repeatType":"daily"}]','DISABLED',DATE_SUB(NOW(),INTERVAL 1 DAY),NULL,8,5,NOW(),NOW(),0),
(1900000000000003209,'OPT-MESSAGE-001','消息中心写入吞吐优化','PERFORMANCE_OPTIMIZATION',1900000000000003006,'性能优化大模型助手','DB_RESOURCE',1900000000000000009,'统一消息数据库-01 (10.20.1.42)','TPS,锁等待次数,事务提交耗时','{"baselineDays":7}','[{"time":"23:30","repeatType":"daily"}]','ENABLED',DATE_SUB(NOW(),INTERVAL 5 HOUR),DATE_ADD(NOW(),INTERVAL 12 HOUR),14,2,NOW(),NOW(),0);

INSERT IGNORE INTO task_exec_record (id,task_id,task_type,task_name_snapshot,execution_status,trigger_type,start_time,end_time,input_start_time,input_end_time,result_count,error_message,created_at,updated_at,deleted) VALUES
(1900000000000003304,1900000000000003204,'FAULT_PREDICTION','政务门户连接池风险预测','SUCCESS','SCHEDULED',DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_SUB(NOW(),INTERVAL 58 MINUTE),DATE_SUB(NOW(),INTERVAL 7 HOUR),DATE_SUB(NOW(),INTERVAL 1 HOUR),2,NULL,NOW(),NOW(),0),
(1900000000000003305,1900000000000003205,'FAULT_PREDICTION','客户中心磁盘水位预测','RUNNING','MANUAL',DATE_SUB(NOW(),INTERVAL 8 MINUTE),NULL,DATE_SUB(NOW(),INTERVAL 2 HOUR),NOW(),0,NULL,NOW(),NOW(),0),
(1900000000000003306,1900000000000003206,'DEFECT_DETECTION','支付清算慢 SQL 检测','SUCCESS','SCHEDULED',DATE_SUB(NOW(),INTERVAL 2 HOUR),DATE_SUB(NOW(),INTERVAL 118 MINUTE),DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_SUB(NOW(),INTERVAL 2 HOUR),3,NULL,NOW(),NOW(),0),
(1900000000000003307,1900000000000003207,'DEFECT_DETECTION','电子档案表结构巡检','FAILED','SCHEDULED',DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_SUB(NOW(),INTERVAL 9 DAY),DATE_SUB(NOW(),INTERVAL 2 DAY),0,'执行节点连接超时',NOW(),NOW(),0),
(1900000000000003308,1900000000000003208,'PERFORMANCE_OPTIMIZATION','报表平台聚合查询优化','SUCCESS','MANUAL',DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_SUB(NOW(),INTERVAL 23 HOUR),DATE_SUB(NOW(),INTERVAL 8 DAY),DATE_SUB(NOW(),INTERVAL 1 DAY),2,NULL,NOW(),NOW(),0),
(1900000000000003309,1900000000000003209,'PERFORMANCE_OPTIMIZATION','消息中心写入吞吐优化','SUCCESS','SCHEDULED',DATE_SUB(NOW(),INTERVAL 5 HOUR),DATE_SUB(NOW(),INTERVAL 292 MINUTE),DATE_SUB(NOW(),INTERVAL 1 DAY),DATE_SUB(NOW(),INTERVAL 5 HOUR),1,NULL,NOW(),NOW(),0);

INSERT IGNORE INTO kb_instance (id,knowledge_code,knowledge_name,knowledge_type,knowledge_content,attachment_names,source_type,status,version,created_at,updated_at,deleted) VALUES
(1900000000000003104,'FAULT-DISK-001','磁盘水位持续升高处置手册','FAULT','适用客户中心数据库。现象：磁盘使用率连续 3 个采样点超过 85%。建议清理临时表、检查归档策略并扩容。','磁盘水位处置手册.pdf','MANUAL','ENABLED',2,NOW(),NOW(),0),
(1900000000000003105,'FAULT-LOCK-001','锁等待与长事务排查指南','FAULT','先定位阻塞会话，再核对事务开始时间和应用连接池，必要时按审批流程终止长事务。','锁等待排查清单.xlsx','MANUAL','ENABLED',1,NOW(),NOW(),0),
(1900000000000003106,'DEFECT-PARTITION-001','分区表统计信息失效缺陷','DEFECT','月初批量写入后统计信息过期会导致执行计划漂移，应在窗口期更新统计信息。','分区表巡检样例.docx','MANUAL','ENABLED',1,NOW(),NOW(),0),
(1900000000000003107,'DEFECT-ARCHIVE-001','归档任务失败识别规则','DEFECT','识别归档文件晚到、校验失败和重复归档，保留失败批次以便重试。','归档任务规则.md','MANUAL','ENABLED',1,NOW(),NOW(),0),
(1900000000000003108,'OPT-AGG-001','报表聚合查询优化案例','OPTIMIZATION','优先使用覆盖索引和预聚合表，避免跨分区全表扫描，并对比优化前后扫描行数。','聚合查询案例.pdf','RESULT_DEPOSIT','ENABLED',1,NOW(),NOW(),0),
(1900000000000003109,'OPT-BATCH-001','批量写入与事务拆分实践','OPTIMIZATION','将单事务控制在合理行数，采用批量提交并错峰执行，关注锁等待和日志增长。','批量写入实践.xlsx','MANUAL','ENABLED',1,NOW(),NOW(),0);

INSERT IGNORE INTO fault_report (id,report_code,task_id,execution_id,target_type,target_id,target_name_snapshot,alert_time,indicator_category,fault_type,risk_level,expected_start_time,expected_end_time,confidence,forecast_description,handle_suggestion,trend_json,process_status,created_at,updated_at,deleted) VALUES
(1900000000000003403,'FAULT-GOV-ALERT-001',1900000000000003204,1900000000000003304,'BUSINESS_SYSTEM',1900000000000000105,'政务服务统一门户',DATE_SUB(NOW(),INTERVAL 40 MINUTE),'数据库连接池','连接池耗尽风险','MEDIUM',NOW(),DATE_ADD(NOW(),INTERVAL 4 HOUR),86.40,'连接池使用率在高峰时段持续上升。','检查长事务并临时提升连接池上限。','[{"time":"-15min","value":65},{"time":"0min","value":86}]','PENDING',NOW(),NOW(),0),
(1900000000000003404,'FAULT-CUSTOMER-ALERT-001',1900000000000003205,1900000000000003305,'DB_RESOURCE',1900000000000000007,'客户中心数据库-01 (192.168.3.32)',DATE_SUB(NOW(),INTERVAL 18 MINUTE),'磁盘空间','磁盘水位超阈值','CRITICAL',NOW(),DATE_ADD(NOW(),INTERVAL 90 MINUTE),97.20,'磁盘使用率已达 91%，预计 2 小时内耗尽。','立即清理临时文件并执行扩容预案。','[{"time":"-15min","value":84},{"time":"0min","value":97}]','CONFIRMED',NOW(),NOW(),0),
(1900000000000003405,'FAULT-PAY-ALERT-001',1900000000000003206,1900000000000003306,'BUSINESS_SYSTEM',1900000000000000106,'移动支付清算平台',DATE_SUB(NOW(),INTERVAL 2 HOUR),'慢 SQL','清算批次响应变慢','HIGH',DATE_SUB(NOW(),INTERVAL 1 HOUR),DATE_ADD(NOW(),INTERVAL 2 HOUR),90.10,'月结批处理期间慢 SQL 数量突增。','检查支付流水索引和批次锁等待。','[{"time":"-15min","value":42},{"time":"0min","value":90}]','CLOSED',NOW(),NOW(),0);

INSERT IGNORE INTO defect_report (id,defect_code,task_id,execution_id,task_name_snapshot,target_type,target_id,target_name_snapshot,defect_type,severity_level,defect_problem,defect_data,defect_description,repair_suggestion,process_status,created_at,updated_at,deleted) VALUES
(1900000000000003503,'DEFECT-PAY-001',1900000000000003206,1900000000000003306,'支付清算慢 SQL 检测','BUSINESS_SYSTEM',1900000000000000106,'移动支付清算平台','组合索引缺失','HIGH','payment_order 查询出现全表扫描。','{"table":"payment_order","rowsExamined":1840000}','清算批次查询未命中 tenant_id、settle_date 组合索引。','新增 tenant_id + settle_date + status 组合索引。','PENDING',NOW(),NOW(),0),
(1900000000000003504,'DEFECT-ARCHIVE-002',1900000000000003207,1900000000000003307,'电子档案表结构巡检','BUSINESS_SYSTEM',1900000000000000111,'电子档案管理系统','统计信息过期','MEDIUM','archive_record 最近一次统计信息更新时间超过 14 天。','{"table":"archive_record","staleDays":16}','统计信息过期可能导致计划选择不稳定。','在低峰窗口执行 ANALYZE TABLE 并观察计划。','CLOSED',NOW(),NOW(),0),
(1900000000000003505,'DEFECT-CUSTOMER-001',1900000000000003205,1900000000000003305,'客户中心磁盘水位预测','DB_RESOURCE',1900000000000000007,'客户中心数据库-01 (192.168.3.32)','临时表未归档','CRITICAL','临时表空间增长速率异常。','{"growthGbPerHour":8.6}','批量导入失败后残留临时表未清理。','清理临时表并补充作业失败回滚逻辑。','CONFIRMED',NOW(),NOW(),0);

INSERT IGNORE INTO opt_report (id,report_code,task_id,execution_id,task_name_snapshot,target_type,target_id,target_name_snapshot,optimization_type,optimization_problem,sql_text,sql_fingerprint,execution_plan_json,slow_sql_metrics_json,optimization_content,confidence,process_status,execute_result,before_metrics_json,after_metrics_json,created_at,updated_at,deleted) VALUES
(1900000000000003603,'OPT-BI-003',1900000000000003208,1900000000000003308,'报表平台聚合查询优化','BUSINESS_SYSTEM',1900000000000000107,'数据分析与报表平台','预聚合表优化','月度经营报表跨 12 个分区扫描。','SELECT region, sum(amount) FROM trade_fact WHERE stat_date BETWEEN ? AND ? GROUP BY region','bi-agg-fingerprint-003','{"plan":"PARTITION_SCAN","scannedRows":92000000}','{"avgTimeMs":6800,"rows":92000000}','建立按月预聚合表并增加 region 覆盖索引。',93.40,'CONFIRMED','建议已确认，等待变更窗口。','{"avgTimeMs":6800}','{"avgTimeMs":1200}',NOW(),NOW(),0),
(1900000000000003604,'OPT-MSG-002',1900000000000003209,1900000000000003309,'消息中心写入吞吐优化','DB_RESOURCE',1900000000000000009,'统一消息数据库-01 (10.20.1.42)','批量提交优化','通知回执逐条提交导致事务开销高。',NULL,'msg-write-fingerprint-002','{"plan":"SINGLE_ROW_COMMIT"}','{"throughput":180}','改为每 500 条批量提交并错峰执行。',84.70,'PENDING','本条为演示建议，未真实执行。','{"throughput":180}','{"throughput":520}',NOW(),NOW(),0),
(1900000000000003605,'OPT-CUSTOMER-001',1900000000000003205,1900000000000003305,'客户中心磁盘水位预测','DB_RESOURCE',1900000000000000007,'客户中心数据库-01 (192.168.3.32)','归档策略优化','历史工单附件未按保留周期归档。',NULL,'customer-archive-fingerprint-001','{"plan":"FULL_SCAN"}','{"archiveBacklog":320000}','按季度归档附件并增加归档分区。',79.80,'FALSE_POSITIVE','经核实为演练期间临时数据，关闭建议。','{"archiveBacklog":320000}','{"archiveBacklog":18000}',NOW(),NOW(),0);

INSERT IGNORE INTO system_notification (id,receiver_id,receiver_name_snapshot,title,content,notice_type,priority,read_status,read_at,business_type,business_id,created_at,updated_at,deleted) VALUES
(1900000000000003901,1900000000000001101,'系统管理员','客户中心数据库达到严重水位','客户中心数据库磁盘使用率 91%，请在 90 分钟内完成扩容或清理。','ALERT','HIGH','UNREAD',NULL,'FAULT',1900000000000003404,NOW(),NOW(),0),
(1900000000000003902,1900000000000001101,'系统管理员','支付清算检测任务完成','本次检测发现 3 条慢 SQL 和 1 条索引缺失缺陷。','TASK','NORMAL','UNREAD',NULL,'TASK',1900000000000003206,NOW(),NOW(),0),
(1900000000000003903,1900000000000001101,'系统管理员','灾备链路进入维护模式','灾备中心数据库维护预计持续 45 分钟，期间不影响主中心业务。','SYSTEM','HIGH','READ',DATE_SUB(NOW(),INTERVAL 20 MINUTE),'DB_RESOURCE',1900000000000000008,NOW(),NOW(),0),
(1900000000000003904,1900000000000001101,'系统管理员','报表优化建议待确认','数据分析与报表平台新增一条聚合查询优化建议。','TASK','NORMAL','UNREAD',NULL,'OPTIMIZATION',1900000000000003603,NOW(),NOW(),0),
(1900000000000003905,1900000000000001101,'系统管理员','监控配置抓取失败','政务云数据库-02 最近一次 Prometheus 抓取超时，请检查 exporter。','ALERT','NORMAL','READ',DATE_SUB(NOW(),INTERVAL 8 MINUTE),'DB_RESOURCE',1900000000000000005,NOW(),NOW(),0),
(1900000000000003906,1900000000000001101,'系统管理员','平台例行备份完成','知识库与任务配置已完成夜间备份，备份文件校验通过。','SYSTEM','LOW','READ',DATE_SUB(NOW(),INTERVAL 6 HOUR),NULL,NULL,NOW(),NOW(),0);

INSERT IGNORE INTO login_log (id,user_id,username,login_time,login_ip,device_info,login_status,failure_reason,created_at,updated_at,deleted) VALUES
(1900000000000004001,1900000000000001101,'admin',DATE_SUB(NOW(),INTERVAL 8 MINUTE),'10.10.12.18','Chrome 126 / Windows 11','SUCCESS',NULL,NOW(),NOW(),0),
(1900000000000004002,NULL,'operator',DATE_SUB(NOW(),INTERVAL 35 MINUTE),'10.10.12.44','Chrome 126 / Windows 11','FAILED','密码错误',NOW(),NOW(),0),
(1900000000000004003,1900000000000001101,'admin',DATE_SUB(NOW(),INTERVAL 2 HOUR),'10.10.12.18','Chrome 126 / Windows 11','SUCCESS',NULL,NOW(),NOW(),0),
(1900000000000004004,NULL,'unknown',DATE_SUB(NOW(),INTERVAL 5 HOUR),'203.0.113.42','curl/8.4.0','FAILED','账号不存在',NOW(),NOW(),0);

INSERT IGNORE INTO operation_log (id,operator_id,operator_name_snapshot,module_name,operation_type,target_type,target_id,operation_content,operation_result,error_message,request_ip,user_agent,created_at,updated_at,deleted) VALUES
(1900000000000004101,1900000000000001101,'系统管理员','数据台账','UPDATE_STATUS','DB_RESOURCE',1900000000000000007,'确认客户中心数据库为高风险状态。','SUCCESS',NULL,'10.10.12.18','Chrome 126 / Windows 11',NOW(),NOW(),0),
(1900000000000004102,1900000000000001101,'系统管理员','AI故障预测','HANDLE_ALERT','FAULT',1900000000000003404,'确认磁盘水位告警并创建扩容工单。','SUCCESS',NULL,'10.10.12.18','Chrome 126 / Windows 11',NOW(),NOW(),0),
(1900000000000004103,1900000000000001101,'系统管理员','AI性能优化','CONFIRM_SUGGESTION','OPTIMIZATION',1900000000000003603,'确认报表聚合查询优化建议。','SUCCESS',NULL,'10.10.12.18','Chrome 126 / Windows 11',NOW(),NOW(),0),
(1900000000000004104,1900000000000001101,'系统管理员','监控配置','CHECK_PROMETHEUS','DB_RESOURCE',1900000000000000005,'手工校验 Prometheus 监控接入。','FAILED','抓取超时','10.10.12.18','Chrome 126 / Windows 11',NOW(),NOW(),0);
