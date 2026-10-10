package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 字典数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_dict_data")
@EqualsAndHashCode(callSuper = true)
public class DictDataDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所属字典类型键
     */
    private String dictType;

    /**
     * 显示标签
     */
    private String dictLabel;

    /**
     * 存储值
     */
    private String dictValue;

    /**
     * 显示顺序
     */
    private Integer dictSort;

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
