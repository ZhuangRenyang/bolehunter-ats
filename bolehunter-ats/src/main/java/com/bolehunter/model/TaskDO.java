package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 任务（到期提醒；v1.0 工作台聚合查询，推送能力后续迭代接入）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task")
@EqualsAndHashCode(callSuper = true)
public class TaskDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务说明
     */
    private String content;

    /**
     * 到期时间
     */
    private Date dueTime;

    /**
     * 负责顾问用户id
     */
    private Integer ownerId;

    /**
     * 关联对象类型（recommendation/client/candidate）
     */
    private String relatedType;

    /**
     * 关联对象id
     */
    private Integer relatedId;

    /**
     * 状态：0待办 1完成
     */
    private Integer status;

    /**
     * 完成时间
     */
    private Date doneTime;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;

    /**
     * 负责人昵称（非数据库列，查询时填充）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String ownerName;
}
