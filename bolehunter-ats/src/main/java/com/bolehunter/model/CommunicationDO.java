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
 * 沟通记录（挂推荐记录，顾问逐次代录与客户 HR 的沟通）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("communication")
@EqualsAndHashCode(callSuper = true)
public class CommunicationDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 推荐记录id
     */
    private Integer recommendationId;

    /**
     * 沟通方式（字典 communication_type）
     */
    private String type;

    /**
     * 沟通内容
     */
    private String content;

    /**
     * 记录人用户id
     */
    private Integer communicatorId;

    /**
     * 沟通时间
     */
    private Date communicateTime;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;

    /**
     * 记录人昵称（非数据库列，查询时填充）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String communicatorName;
}
