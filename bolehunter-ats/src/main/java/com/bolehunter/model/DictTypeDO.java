package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 字典类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_dict_type")
@EqualsAndHashCode(callSuper = true)
public class DictTypeDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型键（唯一）
     */
    private String dictType;

    /**
     * 状态: 1-正常 0-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;
}
