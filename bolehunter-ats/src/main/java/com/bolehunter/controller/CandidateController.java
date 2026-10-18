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
import com.bolehunter.dto.candidate.CreateOrUpdateCandidateDTO;
import com.bolehunter.dto.candidate.SetCandidateTagsDTO;
import com.bolehunter.extension.resume.CandidateExcelHelper;
import com.bolehunter.model.CandidateDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.CandidateService;
import com.bolehunter.vo.CandidateImportResultVO;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import com.bolehunter.vo.PageResponseVO;
import com.bolehunter.vo.UpdatedVO;
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
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.util.List;

/**
 * 候选人管理（带认证的 REST API：读接口需登录，写接口需权限）
 */
@RestController
@RequestMapping("/recruit/candidate")
@PermissionModule(value = "候选人管理")
@Validated
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private com.bolehunter.core.audit.DataMaskingService dataMaskingService;

    @Autowired
    private CandidateExcelHelper candidateExcelHelper;

    /**
     * 新增候选人（手机号/邮箱查重）
     */
    @Logger(template = "新增候选人")
    @PostMapping("")
    @PermissionMeta(value = "新增候选人")
    public CreatedVO create(@RequestBody @Validated CreateOrUpdateCandidateDTO validator) {
        UserDO user = LocalUser.getLocalUser();
        candidateService.createCandidate(validator, user == null ? null : user.getId());
        return new CreatedVO(8500);
    }

    /**
     * 更新候选人
     */
    @Logger(template = "更新候选人")
    @PutMapping("/{id}")
    @PermissionMeta(value = "更新候选人")
    public UpdatedVO update(@PathVariable @Positive(message = "{id.positive}") Integer id,
                            @RequestBody @Validated CreateOrUpdateCandidateDTO validator) {
        candidateService.updateCandidate(id, validator);
        return new UpdatedVO(8510);
    }

    /**
     * 删除候选人
     */
    @Logger(template = "删除候选人")
    @DeleteMapping("/{id}")
    @PermissionMeta(value = "删除候选人")
    public DeletedVO delete(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        candidateService.deleteCandidate(id);
        return new DeletedVO(8520);
    }

    /**
     * 候选人详情（含标签）
     */
    @GetMapping("/{id}")
    @LoginRequired
    public CandidateDO detail(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        return candidateService.getCandidateDetail(id);
    }

    /**
     * 候选人分页多条件筛选
     */
    @Logger(template = "查询候选人列表")
    @GetMapping("/page")
    @PermissionMeta(value = "查询候选人列表")
    public PageResponseVO<CandidateDO> page(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "education", required = false) String education,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "owner_id", required = false) Integer ownerId,
            @RequestParam(name = "source", required = false) String source,
            @RequestParam(name = "tag_ids", required = false) List<Integer> tagIds,
            @RequestParam(name = "count", required = false, defaultValue = "10")
            @Min(value = 1, message = "{page.count.min}")
            @Max(value = 30, message = "{page.count.max}") Integer count,
            @RequestParam(name = "page", required = false, defaultValue = "0")
            @Min(value = 0, message = "{page.number.min}") Integer page) {
        IPage<CandidateDO> iPage = candidateService.getCandidatePage(keyword, education, city,
                ownerId, source, tagIds, page, count);
        // 迭代 33：数据脱敏（feature_flag data_masking 开启且操作人非管理员时，列表手机号/邮箱脱敏）
        if (dataMaskingService.shouldMask(com.bolehunter.common.LocalUser.getLocalUser())) {
            iPage.getRecords().forEach(c -> {
                c.setPhone(com.bolehunter.core.audit.DataMaskingService.maskPhone(c.getPhone()));
                c.setEmail(com.bolehunter.core.audit.DataMaskingService.maskEmail(c.getEmail()));
            });
        }
        return PageUtil.build(iPage);
    }

    /**
     * 实时查重（新建/编辑表单用）
     */
    @GetMapping("/duplicate/check")
    @LoginRequired
    public List<String> duplicateCheck(
            @RequestParam(name = "phone", required = false) String phone,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "exclude_id", required = false) Integer excludeId) {
        return candidateService.checkDuplicate(phone, email, excludeId);
    }

    /**
     * 设置候选人标签（全量覆盖）
     */
    @Logger(template = "设置候选人标签")
    @PutMapping("/{id}/tags")
    @PermissionMeta(value = "更新候选人")
    public UpdatedVO setTags(@PathVariable @Positive(message = "{id.positive}") Integer id,
                             @RequestBody @Validated SetCandidateTagsDTO validator) {
        candidateService.setTags(id, validator.getTagIds());
        return new UpdatedVO(8510);
    }

    /**
     * 简历文件导入（pdf/doc/docx，单文件，解析后自动入库）
     */
    @Logger(template = "导入简历")
    @PostMapping("/import/resume")
    @PermissionMeta(value = "导入候选人")
    public CandidateImportResultVO importResume(@RequestParam("file") MultipartFile file) {
        UserDO user = LocalUser.getLocalUser();
        return candidateService.importResume(file, user == null ? null : user.getId());
    }

    /**
     * Excel 批量导入（xls/xlsx，跳过重复并返回报告）
     */
    @Logger(template = "Excel 导入候选人")
    @PostMapping("/import/excel")
    @PermissionMeta(value = "导入候选人")
    public CandidateImportResultVO importExcel(@RequestParam("file") MultipartFile file) {
        UserDO user = LocalUser.getLocalUser();
        return candidateService.importExcel(file, user == null ? null : user.getId());
    }

    /**
     * 候选人下拉选项（推荐/职位表单等使用）
     */
    @GetMapping("/options")
    @LoginRequired
    public List<CandidateDO> options() {
        return candidateService.getCandidateOptions();
    }

    /**
     * 下载 Excel 导入模板
     */
    @GetMapping("/import/template")
    @LoginRequired
    public void importTemplate(HttpServletResponse response) throws IOException {
        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=candidate_import_template.xlsx");
        candidateExcelHelper.writeTemplate(response.getOutputStream());
    }
}
