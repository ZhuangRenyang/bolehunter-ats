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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bolehunter.common.LocalUser;
import com.bolehunter.common.util.PageUtil;
import com.bolehunter.dto.client.AssignOwnerDTO;
import com.bolehunter.dto.client.CreateOrUpdateClientContactDTO;
import com.bolehunter.dto.company.CreateOrUpdateCompanyDTO;
import com.bolehunter.model.ClientContactDO;
import com.bolehunter.model.CompanyDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.ClientContactService;
import com.bolehunter.service.ClientService;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import com.bolehunter.vo.PageResponseVO;
import com.bolehunter.vo.UpdatedVO;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;

/**
 * 客户 CRM 控制器（猎头视角的客户公司）
 * 老的 /recruit/company 企业认证接口全部保留，互不影响。
 */
@RestController
@RequestMapping("/recruit/client")
@PermissionModule(value = "客户管理")
@Validated
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientContactService clientContactService;

    /**
     * 新增客户（顾问建档，state 固定 1，负责人缺省为当前登录用户）
     */
    @Logger(template = "新增客户")
    @PostMapping("")
    @PermissionMeta(value = "新增客户")
    public CreatedVO create(@RequestBody @Validated CreateOrUpdateCompanyDTO validator) {
        UserDO user = LocalUser.getLocalUser();
        clientService.createClient(validator, user == null ? null : user.getId());
        return new CreatedVO(8300);
    }

    /**
     * 客户分页（关键词/行业/等级/负责人/合作状态）
     */
    @Logger(template = "查询客户列表")
    @GetMapping("/page")
    @PermissionMeta(value = "查询客户列表")
    public PageResponseVO<CompanyDO> page(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "industry", required = false) String industry,
            @RequestParam(name = "level", required = false) String level,
            @RequestParam(name = "owner_id", required = false) Integer ownerId,
            @RequestParam(name = "client_status", required = false) String clientStatus,
            @RequestParam(name = "count", required = false, defaultValue = "10")
            @Min(value = 1, message = "{page.count.min}")
            @Max(value = 30, message = "{page.count.max}") Integer count,
            @RequestParam(name = "page", required = false, defaultValue = "0")
            @Min(value = 0, message = "{page.number.min}") Integer page) {
        IPage<CompanyDO> iPage = clientService.getClientPage(keyword, industry, level, ownerId, clientStatus, page, count);
        return PageUtil.build(iPage);
    }

    /**
     * 客户详情聚合：company + contacts + positions
     */
    @Logger(template = "查询客户详情")
    @GetMapping("/{id}/detail")
    @PermissionMeta(value = "查询客户列表")
    public Map<String, Object> detail(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        return clientService.getClientDetail(id);
    }

    /**
     * 更新客户基本信息与客户字段
     */
    @Logger(template = "更新客户")
    @PutMapping("/{id}")
    @PermissionMeta(value = "更新客户")
    public UpdatedVO update(@PathVariable @Positive(message = "{id.positive}") Integer id,
                            @RequestBody @Validated CreateOrUpdateCompanyDTO validator) {
        CompanyDO company = clientService.getById(id);
        if (company == null) {
            throw new NotFoundException(80140);
        }
        clientService.updateClient(company, validator);
        return new UpdatedVO(8310);
    }

    /**
     * 删除客户（迭代24 操作审计增强）：名下存在推荐记录时后端拒绝（80290），
     * 前端另有输入客户名的强确认弹窗，双重防线
     */
    @Logger(template = "删除客户")
    @DeleteMapping("/{id}")
    @PermissionMeta(value = "删除客户")
    public DeletedVO delete(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        clientService.deleteClient(id);
        return new DeletedVO();
    }

    /**
     * 分配/变更负责顾问
     */
    @Logger(template = "分配客户负责人")
    @PutMapping("/{id}/owner")
    @PermissionMeta(value = "分配负责人")
    public UpdatedVO assignOwner(@PathVariable @Positive(message = "{id.positive}") Integer id,
                                 @RequestBody @Validated AssignOwnerDTO validator) {
        CompanyDO company = clientService.getById(id);
        if (company == null) {
            throw new NotFoundException(80140);
        }
        clientService.assignOwner(id, validator.getOwnerId());
        return new UpdatedVO(8320);
    }

    /**
     * 变更客户合作状态
     */
    @Logger(template = "变更客户合作状态")
    @PutMapping("/{id}/status")
    @PermissionMeta(value = "更新客户状态")
    public UpdatedVO changeStatus(@PathVariable @Positive(message = "{id.positive}") Integer id,
                                  @RequestParam("client_status")
                                  @Pattern(regexp = "potential|signing|suspended|lost", message = "{client.status.illegal}")
                                  String clientStatus) {
        CompanyDO company = clientService.getById(id);
        if (company == null) {
            throw new NotFoundException(80140);
        }
        clientService.changeClientStatus(id, clientStatus);
        return new UpdatedVO(8330);
    }

    /**
     * 可分配负责人选项（管理员/顾问）
     */
    @GetMapping("/owners")
    @LoginRequired
    public List<UserDO> owners() {
        return clientService.getOwnerOptions();
    }

    /**
     * 客户下拉选项（职位表单等使用）
     */
    @GetMapping("/options")
    @LoginRequired
    public List<CompanyDO> options() {
        return clientService.getClientOptions();
    }

    // ---------------- 客户联系人 ----------------

    /**
     * 某客户的联系人列表
     */
    @GetMapping("/{companyId}/contact")
    @LoginRequired
    public List<ClientContactDO> contacts(@PathVariable @Positive(message = "{id.positive}") Integer companyId) {
        return clientContactService.listByCompany(companyId);
    }

    /**
     * 新增联系人
     */
    @Logger(template = "新增客户联系人")
    @PostMapping("/{companyId}/contact")
    @PermissionMeta(value = "维护联系人")
    public CreatedVO createContact(@PathVariable @Positive(message = "{id.positive}") Integer companyId,
                                   @RequestBody @Validated CreateOrUpdateClientContactDTO validator) {
        CompanyDO company = clientService.getById(companyId);
        if (company == null) {
            throw new NotFoundException(80140);
        }
        clientContactService.createContact(companyId, validator);
        return new CreatedVO(8400);
    }

    /**
     * 更新联系人
     */
    @Logger(template = "更新客户联系人")
    @PutMapping("/{companyId}/contact/{contactId}")
    @PermissionMeta(value = "维护联系人")
    public UpdatedVO updateContact(@PathVariable @Positive(message = "{id.positive}") Integer companyId,
                                   @PathVariable @Positive(message = "{id.positive}") Integer contactId,
                                   @RequestBody @Validated CreateOrUpdateClientContactDTO validator) {
        ClientContactDO contact = clientContactService.getById(contactId);
        if (contact == null || !contact.getCompanyId().equals(companyId)) {
            throw new NotFoundException(80150);
        }
        clientContactService.updateContact(contact, validator);
        return new UpdatedVO(8410);
    }

    /**
     * 删除联系人
     */
    @Logger(template = "删除客户联系人")
    @DeleteMapping("/{companyId}/contact/{contactId}")
    @PermissionMeta(value = "维护联系人")
    public DeletedVO deleteContact(@PathVariable @Positive(message = "{id.positive}") Integer companyId,
                                   @PathVariable @Positive(message = "{id.positive}") Integer contactId) {
        ClientContactDO contact = clientContactService.getById(contactId);
        if (contact == null || !contact.getCompanyId().equals(companyId)) {
            throw new NotFoundException(80150);
        }
        clientContactService.deleteContact(contactId);
        return new DeletedVO(8420);
    }
}
