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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.common.LocalUser;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.core.audit.DataMaskingService;
import com.bolehunter.core.exception.ForbiddenException;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.bo.FileBO;
import com.bolehunter.model.CandidateDO;
import com.bolehunter.model.CollectLinkDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.CandidateService;
import com.bolehunter.service.CollectLinkService;
import com.bolehunter.service.FileService;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.UpdatedVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 候选人自助信息收集（迭代 36）：顾问生成免登录收集链接，
 * 候选人自助填写基本信息并上传简历，提交后进入待审核池。
 */
@RestController
public class CollectController {

    @Autowired
    private CollectLinkService collectLinkService;

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private FileService fileService;

    @Autowired
    private DataMaskingService dataMaskingService;

    // ============ 管理端（顾问，需登录） ============

    /**
     * 创建收集链接
     */
    @PostMapping("/recruit/admin/collect-link")
    @LoginRequired
    public Map<String, Object> create(@RequestBody(required = false) Map<String, String> body) {
        UserDO user = LocalUser.getLocalUser();
        CollectLinkDO link = new CollectLinkDO();
        link.setToken(CollectLinkService.generateToken());
        String title = body == null ? null : body.get("title");
        link.setTitle(title == null || title.isBlank() ? null : title.trim().substring(0, Math.min(100, title.trim().length())));
        link.setCreatedBy(user.getId());
        link.setStatus(1);
        collectLinkService.save(link);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", link.getId());
        result.put("token", link.getToken());
        return result;
    }

    /**
     * 当前顾问的收集链接列表
     */
    @GetMapping("/recruit/admin/collect-link/list")
    @LoginRequired
    public List<CollectLinkDO> list() {
        UserDO user = LocalUser.getLocalUser();
        return collectLinkService.list(new QueryWrapper<CollectLinkDO>()
            .eq("created_by", user.getId())
            .orderByDesc("create_time"));
    }

    /**
     * 开启/关闭收集链接（仅创建者可操作）
     */
    @PutMapping("/recruit/admin/collect-link/{id}")
    @LoginRequired
    public UpdatedVO toggle(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        UserDO user = LocalUser.getLocalUser();
        CollectLinkDO link = collectLinkService.getById(id);
        if (link == null || !user.getId().equals(link.getCreatedBy())) {
            throw new NotFoundException(80140);
        }
        Object status = body.get("status");
        if (status != null) {
            link.setStatus(Integer.valueOf(String.valueOf(status)));
            collectLinkService.updateById(link);
        }
        return new UpdatedVO();
    }

    // ============ 公开端（候选人免登录） ============

    /**
     * 校验收集链接有效性并返回收集说明（公开）
     */
    @GetMapping("/recruit/public/collect/{token}")
    public Map<String, Object> check(@PathVariable String token) {
        CollectLinkDO link = collectLinkService.getOne(
            new QueryWrapper<CollectLinkDO>().eq("token", token), false);
        boolean valid = link != null && collectLinkService.isValid(token);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("valid", valid);
        result.put("title", valid && link.getTitle() != null ? link.getTitle() : "");
        return result;
    }

    /**
     * 候选人自助提交（公开，multipart：基本信息 + 可选简历文件）
     * 提交即进入待审核池（audit_status=0），查重拒绝重复电话/邮箱。
     */
    @PostMapping("/recruit/public/collect/{token}")
    public CreatedVO submit(@PathVariable String token, MultipartHttpServletRequest request) {
        if (!collectLinkService.isValid(token)) {
            throw new ForbiddenException(80310, "收集链接已失效或已关闭，请联系你的顾问");
        }
        String name = trim(request.getParameter("name"));
        String phone = trim(request.getParameter("phone"));
        if (name == null || phone == null) {
            throw new ForbiddenException(80311, "姓名与电话为必填项");
        }
        String email = trim(request.getParameter("email"));
        // 查重：电话或邮箱已在库则拒绝
        List<String> duplicated = candidateService.checkDuplicate(phone, email, null);
        if (!duplicated.isEmpty()) {
            throw new ForbiddenException(80312, "该电话或邮箱已在候选人库中，无需重复提交");
        }
        // 简历文件（可选）
        String resumeUrl = null;
        MultiValueMap<String, MultipartFile> fileMap = request.getMultiFileMap();
        if (fileMap.containsKey("file") && fileMap.getFirst("file") != null
                && !fileMap.getFirst("file").isEmpty()) {
            List<FileBO> files = fileService.upload(fileMap);
            if (!files.isEmpty()) {
                resumeUrl = files.get(0).getUrl();
            }
        }
        // 创建待审核候选人
        CandidateDO candidate = new CandidateDO();
        candidate.setName(name.substring(0, Math.min(50, name.length())));
        candidate.setPhone(phone);
        candidate.setEmail(email);
        candidate.setCurrentCompany(trim(request.getParameter("currentCompany")));
        candidate.setCurrentPosition(trim(request.getParameter("currentPosition")));
        String education = trim(request.getParameter("education"));
        if (education != null) {
            candidate.setEducation(education);
        }
        candidate.setCity(trim(request.getParameter("city")));
        candidate.setRemark(trim(request.getParameter("intro")));
        candidate.setResumeFile(resumeUrl);
        candidate.setAuditStatus(0);
        candidate.setSource("apply");
        candidateService.save(candidate);
        return new CreatedVO();
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
