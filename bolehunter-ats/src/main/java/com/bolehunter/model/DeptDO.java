package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 部门（RuoYi 式树形，极简组织架构）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_dept")
@EqualsAndHashCode(callSuper = true)
public class DeptDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 父部门id，0 为顶级
     */
    private Integer parentId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 显示顺序
     */
    private Integer sort;

    /**
     * 负责人
     */
    private String leader;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;

    /**
     * 子部门（非数据库字段）
     */
    @TableField(exist = false)
    private List<DeptDO> children;
}
