package com.xinchuang.diagnosis.ai.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.xinchuang.diagnosis.ai.engine.FaultPredictionExecutionStateService.ExecutionContext;
import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.FaultReport;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import com.xinchuang.diagnosis.ai.mapper.AiTaskMapper;
import com.xinchuang.diagnosis.ai.mapper.FaultReportMapper;
import com.xinchuang.diagnosis.ai.mapper.TaskExecRecordMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

@ExtendWith(MockitoExtension.class)
class FaultPredictionExecutionStateServiceTest {
    @Mock private TaskExecRecordMapper executionMapper;
    @Mock private AiTaskMapper taskMapper;
    @Mock private FaultReportMapper reportMapper;
    private FaultPredictionExecutionStateService service;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"), TaskExecRecord.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "test"), AiTask.class);
        service = new FaultPredictionExecutionStateService(executionMapper, taskMapper, reportMapper,
            new FaultPredictionMetricRegistry(), new ObjectMapper());
    }

    @Test
    void claimReturnsNullWhenAnotherWorkerAlreadyClaimedExecution() {
        when(executionMapper.update(any(), any())).thenReturn(0);

        ExecutionContext result = service.claim(2L);

        assertEquals(null, result);
        verify(executionMapper, never()).selectById(2L);
    }

    @Test
    void completesExecutionAndPersistsStrongestAnomalyAsAlert() {
        AiTask task = task();
        TaskExecRecord record = runningRecord();
        when(executionMapper.selectById(2L)).thenReturn(record);
        when(reportMapper.selectCount(any())).thenReturn(0L, 1L);

        FaultPredictionInput input = input();
        service.complete(new ExecutionContext(task, record), input, response());

        ArgumentCaptor<FaultReport> reportCaptor = ArgumentCaptor.forClass(FaultReport.class);
        verify(reportMapper).insert(reportCaptor.capture());
        FaultReport report = reportCaptor.getValue();
        assertEquals("CPU时序异常", report.getFaultType());
        assertEquals("MEDIUM", report.getRiskLevel());
        assertEquals("75.00", report.getConfidence().toPlainString());
        assertTrue(report.getTrendJson().contains("100.0"));
        assertEquals("SUCCESS", record.getExecutionStatus());
        assertEquals(1, record.getResultCount());
        verify(executionMapper).updateById(record);
        verify(taskMapper).update(any(), any());
    }

    private AiTask task() {
        AiTask task = new AiTask(); task.setId(1L); task.setTargetType("DB_RESOURCE"); task.setTargetId(10L);
        task.setTargetNameSnapshot("核心数据库"); task.setMetricCodes("CPU");
        return task;
    }

    private TaskExecRecord runningRecord() {
        TaskExecRecord record = new TaskExecRecord(); record.setId(2L); record.setTaskId(1L);
        record.setExecutionStatus("RUNNING"); record.setTaskType("FAULT_PREDICTION");
        return record;
    }

    private FaultPredictionInput input() {
        ZoneId zone = ZoneId.of("Asia/Shanghai");
        LocalDateTime start = LocalDateTime.of(2026, 8, 27, 10, 0);
        TimeSeriesInferenceRequest request = new TimeSeriesInferenceRequest(); request.setRequestId("execution-2");
        request.setTimestamps(List.of(start.atZone(zone).toEpochSecond(), start.plusMinutes(1).atZone(zone).toEpochSecond(),
            start.plusMinutes(2).atZone(zone).toEpochSecond()));
        request.setFeatures(Map.of("cpu_usage", List.of(10.0, 10.0, 100.0)));
        return new FaultPredictionInput(request, start, start.plusHours(1));
    }

    private TimeSeriesInferenceResponse response() {
        TimeSeriesInferenceResponse.AnomalyPoint weaker = anomaly(1, 10.0, 1.6);
        TimeSeriesInferenceResponse.AnomalyPoint strongest = anomaly(2, 100.0, 2.0);
        TimeSeriesInferenceResponse response = new TimeSeriesInferenceResponse(); response.setRequestId("execution-2");
        response.setContractVersion("1.0"); response.setPointCount(3); response.setAnomalies(List.of(weaker, strongest));
        return response;
    }

    private TimeSeriesInferenceResponse.AnomalyPoint anomaly(int index, double value, double score) {
        TimeSeriesInferenceResponse.AnomalyPoint point = new TimeSeriesInferenceResponse.AnomalyPoint();
        point.setMetricCode("cpu_usage"); point.setIndex(index);
        point.setTimestamp(input().request().getTimestamps().get(index)); point.setValue(value); point.setScore(score);
        return point;
    }
}
