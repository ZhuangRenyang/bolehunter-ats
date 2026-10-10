/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.mapper.UserGroupMapper;
import com.bolehunter.model.UserGroupDO;
import com.bolehunter.service.MailService;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.core.exception.ParameterException;
import com.bolehunter.core.annotation.*;
import com.bolehunter.core.token.DoubleJWT;
import com.bolehunter.core.token.Tokens;
import com.bolehunter.common.LocalUser;
import com.bolehunter.common.util.ObserverGenerate;
import com.bolehunter.common.util.SingleUtil;
import com.bolehunter.dto.user.ChangePasswordDTO;
import com.bolehunter.dto.user.LoginDTO;
import com.bolehunter.dto.user.RegisterDTO;
import com.bolehunter.dto.user.UpdateInfoDTO;
import com.bolehunter.model.GroupDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.GroupService;
import com.bolehunter.service.UserIdentityService;
import com.bolehunter.service.UserService;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.UpdatedVO;
import com.bolehunter.vo.UserInfoVO;
import com.bolehunter.vo.UserPermissionVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recruit/user")
@PermissionModule(value = "用户")
@Validated
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private GroupService groupService;
    @Resource
    private UserIdentityService userIdentityService;
    @Resource
    private UserGroupMapper userGroupMapper;
    @Resource
    private DoubleJWT jwt;
    @Resource
    private MailService mailService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public CreatedVO register(@RequestBody @Validated RegisterDTO validator) {
        CreatedVO result = userService.userDataCheck(validator);
        if(result.getCode()!=400){
            userService.createUser(validator);
//            // 用户注册成功，创建一个该用户对应的观察者类
            ObserverGenerate.generate(validator.getUsername());
        }
        return result;
    }

    /**
     * 用户登陆
     */
    @PostMapping("/login")
    public Tokens login(@RequestBody @Validated LoginDTO validator) {
        UserDO user = userService.getUserByUsername(validator.getUsername());
        if (user == null) {
            throw new NotFoundException(10021);
        }
        boolean valid = userIdentityService.verifyUsernamePassword(
                user.getId(),
                user.getUsername(),
                validator.getPassword());
        if (!valid) {
            throw new ParameterException(10031);
        }
        // 保存当前登录用户到SingleUtil工具类中
        SingleUtil.userDO = user;
        return jwt.generateTokens(user.getId());
    }

    /**
     * 更新用户信息
     */
    @PutMapping
    @LoginRequired
    public UpdatedVO update(@RequestBody @Validated UpdateInfoDTO validator) {
        if (userService.updateUserInfo(validator)){
            return new UpdatedVO(6);
        }
        return new UpdatedVO(400);
    }

    /**
     * 修改密码
     */
    @PutMapping("/change_password")
    @LoginRequired
    public UpdatedVO updatePassword(@RequestBody @Validated ChangePasswordDTO validator) {
        userService.changeUserPassword(validator);
        return new UpdatedVO(4);
    }

    /**
     * 刷新令牌
     */
    @GetMapping("/refresh")
    @RefreshRequired
    public Tokens getRefreshToken() {
        UserDO user = LocalUser.getLocalUser();
        return jwt.generateTokens(user.getId());
    }

    /**
     * 查询拥有权限
     */
    @GetMapping("/permissions")
    @LoginRequired
    public UserPermissionVO getPermissions() {
        UserDO user = LocalUser.getLocalUser();
        boolean admin = groupService.checkIsRootByUserId(user.getId());
        boolean hr = groupService.checkHRExistById(user.getId());
        List<Map<String, List<Map<String, String>>>> permissions = userService.getStructuralUserPermissions(user.getId());
        UserPermissionVO userPermissions = new UserPermissionVO(user, permissions);
        userPermissions.setAdmin(admin);
        userPermissions.setHr(hr);
        return userPermissions;
    }

    /**
     * 查询用户角色
     */
    @GetMapping("/getGroup/{id}")
    @LoginRequired
    public int getUserGroup(@PathVariable Integer id) {
        LambdaQueryWrapper<UserGroupDO> queryWrapper=new QueryWrapper<UserGroupDO>().lambda();
        queryWrapper.eq(UserGroupDO::getUserId,id);
        UserGroupDO userGroupDO = userGroupMapper.selectOne(queryWrapper);
        return userGroupDO.getGroupId();
    }

    /**
     * 查询自己信息
     */
    @LoginRequired
    @GetMapping("/information")
    public UserInfoVO getInformation() {
        UserDO user = LocalUser.getLocalUser();
        List<GroupDO> groups = groupService.getUserGroupsByUserId(user.getId());
        return new UserInfoVO(user, groups);
    }

    /**
     * 请求邮箱验证码
     */
    @RequestMapping(value = "/getMailCode",method = RequestMethod.POST)
    public CreatedVO getMailCode(@RequestBody JSONObject mail){
        return mailService.SendCodeMail(mail.getString("mail"));
    }

    /**
     * 忘记密码
     */
    @RequestMapping(value = "forgot_password",method = RequestMethod.POST)
    public CreatedVO forgotPwd(@RequestBody JSONObject parma){
        return userService.forgotPwd(parma);
    }

    /**
     * 通过id获取到用户的名字和头像
     */
    @RequestMapping(value = "getID_UserData",method = RequestMethod.POST)
    public JSONObject getID_UserData(@RequestBody JSONObject parma){
        return userService.getID_user(parma.getInteger("data"));
    }

}
