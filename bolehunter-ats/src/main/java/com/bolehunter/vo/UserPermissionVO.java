package com.bolehunter.vo;

import com.bolehunter.model.UserDO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;

/**
 * 用户 + 权限 view object
 */
@Data
public class UserPermissionVO {

    private Integer id;

    private String nickname;

    private String avatar;

    private Boolean admin;

    private Boolean hr;

    /**
     * 用户类型: ADMIN-管理员 CONSULTANT-顾问 CLIENT_HR-客户HR READONLY-只读
     */
    private String userType;

    private String email;

    private List<Map<String, List<Map<String, String>>>> permissions;

    public UserPermissionVO() {
    }

    public UserPermissionVO(UserDO userDO, List<Map<String, List<Map<String, String>>>> permissions) {
        BeanUtils.copyProperties(userDO, this);
        this.permissions = permissions;
    }

    public UserPermissionVO(UserDO userDO) {
        BeanUtils.copyProperties(userDO, this);
    }
}
