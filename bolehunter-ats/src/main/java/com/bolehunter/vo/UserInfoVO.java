package com.bolehunter.vo;

import cn.hutool.core.bean.BeanUtil;
import com.bolehunter.model.GroupDO;
import com.bolehunter.model.UserDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户信息 view object
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO {

    private Integer id;

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
     * 用户类型: ADMIN-管理员 CONSULTANT-顾问 CLIENT_HR-客户HR READONLY-只读
     */
    private String userType;

    /**
     * 部门id
     */
    private Integer deptId;

    /**
     * 分组
     */
    private List<GroupDO> groups;

    public UserInfoVO(UserDO user, List<GroupDO> groups) {
        BeanUtil.copyProperties(user, this);
        this.groups = groups;
    }
}
