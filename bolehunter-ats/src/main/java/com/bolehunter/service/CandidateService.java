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
package com.bolehunter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.dto.candidate.CreateOrUpdateCandidateDTO;
import com.bolehunter.model.CandidateDO;
import com.bolehunter.vo.CandidateImportResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 候选人服务
 */
public interface CandidateService extends IService<CandidateDO> {

    /**
     * 多条件分页筛选：关键词（姓名/电话/邮箱/公司/职位）+ 学历/城市/负责人/来源/标签
     */
    IPage<CandidateDO> getCandidatePage(String keyword, String education, String city,
                                        Integer ownerId, String source, List<Integer> tagIds,
                                        Integer page, Integer count);

    /**
     * 详情（附带标签）
     */
    CandidateDO getCandidateDetail(Integer id);

    boolean createCandidate(CreateOrUpdateCandidateDTO validator, Integer currentUserId);

    boolean updateCandidate(Integer id, CreateOrUpdateCandidateDTO validator);

    boolean deleteCandidate(Integer id);

    /**
     * 设置候选人标签（全量覆盖；空列表表示清空）
     */
    boolean setTags(Integer candidateId, List<Integer> tagIds);

    /**
     * 查重：返回冲突描述列表（空列表表示不重复）
     */
    List<String> checkDuplicate(String phone, String email, Integer excludeId);

    /**
     * 单份简历文件导入（pdf/doc/docx），返回导入报告
     */
    CandidateImportResultVO importResume(MultipartFile file, Integer currentUserId);

    /**
     * Excel 批量导入（xls/xlsx），返回导入报告
     */
    CandidateImportResultVO importExcel(MultipartFile file, Integer currentUserId);

    /**
     * 候选人下拉选项（id/name/phone/current_company，供推荐选择）
     */
    List<CandidateDO> getCandidateOptions();
}
