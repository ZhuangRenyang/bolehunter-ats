package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Eval
 * @since 2022-03-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("`user`")
@EqualsAndHashCode(callSuper = true)
public class UserDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = -1463999384554707735L;

    /**
     * 用户名，唯一
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 头像url
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 电话
     */
    private String tel;

    /**
     * 租户id（开源版固定为 1，预留多租户）
     */
    private Integer tenantId;

    /**
     * 用户类型: ADMIN-管理员 CONSULTANT-顾问 CLIENT_HR-客户HR READONLY-只读
     */
    private String userType;

    /**
     * 部门id
     */
    private Integer deptId;

}
