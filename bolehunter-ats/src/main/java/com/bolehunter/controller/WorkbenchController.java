package com.bolehunter.controller;

import com.bolehunter.common.LocalUser;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.WorkbenchService;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.core.annotation.PermissionModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 工作台聚合
 */
@RestController
@RequestMapping("/recruit/workbench")
@PermissionModule(value = "工作台")
public class WorkbenchController {

    @Autowired
    private WorkbenchService workbenchService;

    /**
     * 工作台汇总：统计 + 阶段分布 + 今日/逾期任务
     */
    @GetMapping("/summary")
    @LoginRequired
    public Map<String, Object> summary() {
        UserDO user = LocalUser.getLocalUser();
        return workbenchService.summary(user == null ? null : user.getId());
    }
}
