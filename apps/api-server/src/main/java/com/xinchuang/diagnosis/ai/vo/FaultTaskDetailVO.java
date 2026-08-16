package com.xinchuang.diagnosis.ai.vo;

import com.xinchuang.diagnosis.ai.entity.AiTask;
import com.xinchuang.diagnosis.ai.entity.TaskExecRecord;
import java.util.List;

public class FaultTaskDetailVO extends FaultTaskVO {
    private List<TaskExecRecord> executionRecords;

    public static FaultTaskDetailVO from(AiTask task, List<TaskExecRecord> executionRecords) {
        FaultTaskDetailVO vo = new FaultTaskDetailVO();
        vo.populate(task); vo.executionRecords = executionRecords; return vo;
    }
    public List<TaskExecRecord> getExecutionRecords() { return executionRecords; }
}
