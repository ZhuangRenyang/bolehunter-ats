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

import com.bolehunter.model.CompanyDO;
import com.bolehunter.service.HrCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bolehunter.model.HrCompanyDO;
import com.bolehunter.vo.CreatedVO;
import jakarta.validation.constraints.Positive;

/**
* @author Eval
* @since 2022-04-05
*/
@RestController
@RequestMapping("/recruit/hr-company")
public class HrCompanyController {

    @Autowired
    private HrCompanyService hrCompanyService;

    @PostMapping("")
    public CreatedVO create(@RequestParam @Positive(message = "{id.positive}") Integer hrID, @RequestParam @Positive(message = "{id.positive}") Integer companyID) {
        hrCompanyService.create(hrID, companyID);
        return new CreatedVO(1);
    }

    /**
     * 根据hr_id查询hr对应的公司信息
     * @param hrID
     * @return
     */
    @GetMapping("/{hrID}")
    public CompanyDO get(@PathVariable(value = "hrID") @Positive(message = "{id.positive}") Integer hrID) {
        return hrCompanyService.getCompany(hrID);
    }

    /**
     * 根据hr_id查询记录
     * @param hrID
     * @return
     */
    @GetMapping("/search/{hrID}")
    public HrCompanyDO getByHrId(@PathVariable(value = "hrID") @Positive(message = "{id.positive}") Integer hrID) {
        return hrCompanyService.getByHrId(hrID);
    }
}
