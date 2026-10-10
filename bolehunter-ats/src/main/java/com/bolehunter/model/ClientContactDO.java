package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 客户联系人
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("client_contact")
@EqualsAndHashCode(callSuper = true)
public class ClientContactDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所属客户公司id
     */
    private Integer companyId;

    /**
     * 姓名
     */
    private String name;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 职位
     */
    private String title;

    /**
     * 是否主联系人: 1-是 0-否（同公司仅一个）
     */
    private Integer isPrimary;

    /**
     * 备注
     */
    private String remark;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;
}
