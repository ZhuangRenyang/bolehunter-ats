package com.bolehunter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.dto.task.CreateTaskDTO;
import com.bolehunter.model.TaskDO;

import java.util.List;

/**
 * 任务服务
 */
public interface TaskService extends IService<TaskDO> {

    boolean createTask(CreateTaskDTO validator, Integer ownerId);

    /**
     * 完成任务
     */
    boolean doneTask(Integer id);

    boolean deleteTask(Integer id);

    /**
     * 我的待办任务
     *
     * @param scope today=今日到期(含逾期前) overdue=已逾期 all=全部待办
     */
    List<TaskDO> getMyTodo(Integer ownerId, String scope);

    /**
     * 按关联对象查询任务（recommendation/client/candidate + id）
     */
    List<TaskDO> getByRelated(String relatedType, Integer relatedId);
}
