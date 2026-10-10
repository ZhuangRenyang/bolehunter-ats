/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.controller;

import com.bolehunter.service.NotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bolehunter.model.NotifyDO;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.UpdatedVO;
import java.util.List;

/**
* @author Eval
* @since 2022-04-16
*/
@RestController
@RequestMapping("/recruit/notify")
public class NotifyController {

    @Autowired
    private NotifyService notifyService;

    /**
     * 新增消息通知
     * @param notifyDO
     * @return
     */
    @PostMapping("")
    public CreatedVO create(@RequestBody NotifyDO notifyDO) {
        notifyService.create(notifyDO);
        return new CreatedVO();
    }

    /**
     * 根据用户名修改消息状态为已读
     * @param userName
     * @return
     */
    @PutMapping("")
    public UpdatedVO update(@RequestParam String userName) {
        notifyService.update(userName);
        return new UpdatedVO();
    }

    /**
     * 根据用户名和状态码查询该用户收到的所有通知
     * @param userName
     * @param isRead
     * @return
     */
    @GetMapping()
    public List<NotifyDO> get(@RequestParam String userName, @RequestParam String isRead) {
        List<NotifyDO> notifyDOS = notifyService.get(userName, isRead);
        return notifyDOS;
    }

    /**
     * 根据用户名查询该用户收到的所有通知
     * @param userName
     * @return
     */
    @GetMapping("/all")
    public List<NotifyDO> getAll(@RequestParam String userName) {
        List<NotifyDO> notifyDOS = notifyService.getAll(userName);
        return notifyDOS;
    }
}
