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
