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
package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.dto.candidate.CandidateExcelRow;
import com.bolehunter.dto.candidate.CreateOrUpdateCandidateDTO;
import com.bolehunter.extension.resume.CandidateExcelHelper;
import com.bolehunter.extension.resume.ResumeParser;
import com.bolehunter.mapper.CandidateMapper;
import com.bolehunter.mapper.CandidateTagMapper;
import com.bolehunter.mapper.TagMapper;
import com.bolehunter.model.CandidateDO;
import com.bolehunter.model.CandidateTagDO;
import com.bolehunter.model.TagDO;
import com.bolehunter.service.CandidateService;
import com.bolehunter.vo.CandidateImportResultVO;
import com.bolehunter.core.exception.ForbiddenException;
import com.bolehunter.core.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 候选人服务实现
 */
@Slf4j
@Service
public class CandidateServiceImpl extends ServiceImpl<CandidateMapper, CandidateDO> implements CandidateService {

    private static final Pattern PHONE_CHECK = Pattern.compile("1[3-9]\\d{9}");
    private static final Pattern EMAIL_CHECK = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.]+");

    @Autowired
    private CandidateTagMapper candidateTagMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ResumeParser resumeParser;

    @Autowired
    private CandidateExcelHelper candidateExcelHelper;

    @Autowired
    private com.bolehunter.extension.resume.AiResumeParser aiResumeParser;

    @Autowired
    private com.bolehunter.service.FeatureFlagService featureFlagService;

    @Override
    public IPage<CandidateDO> getCandidatePage(String keyword, String education, String city,
                                               Integer ownerId, String source, List<Integer> tagIds,
                                               Integer page, Integer count) {
        com.bolehunter.common.mybatis.Page<CandidateDO> pager = new com.bolehunter.common.mybatis.Page<>(page, count);
        QueryWrapper<CandidateDO> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .and(keyword != null && !keyword.isEmpty(), q -> q
                        .like(CandidateDO::getName, keyword)
                        .or().like(CandidateDO::getPhone, keyword)
                        .or().like(CandidateDO::getEmail, keyword)
                        .or().like(CandidateDO::getCurrentCompany, keyword)
                        .or().like(CandidateDO::getCurrentPosition, keyword))
                .eq(education != null && !education.isEmpty(), CandidateDO::getEducation, education)
                .eq(city != null && !city.isEmpty(), CandidateDO::getCity, city)
                .eq(ownerId != null, CandidateDO::getOwnerId, ownerId)
                .eq(source != null && !source.isEmpty(), CandidateDO::getSource, source)
                .orderByDesc(CandidateDO::getId);
        if (tagIds != null && !tagIds.isEmpty()) {
            String joined = tagIds.stream()
                    .filter(id -> id != null && id > 0)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            if (!joined.isEmpty()) {
                wrapper.inSql("id", "SELECT candidate_id FROM candidate_tag WHERE tag_id IN ("
                        + joined + ") AND delete_time IS NULL");
            }
        }
        IPage<CandidateDO> result = this.page(pager, wrapper);
        fillTags(result.getRecords());
        return result;
    }

    @Override
    public CandidateDO getCandidateDetail(Integer id) {
        CandidateDO candidate = this.getById(id);
        if (candidate == null) {
            throw new NotFoundException(80170);
        }
        fillTags(Collections.singletonList(candidate));
        return candidate;
    }

    @Transactional
    @Override
    public boolean createCandidate(CreateOrUpdateCandidateDTO validator, Integer currentUserId) {
        assertNotDuplicate(validator.getPhone(), validator.getEmail(), null);
        CandidateDO candidate = CandidateDO.builder()
                .name(validator.getName())
                .phone(emptyToNull(validator.getPhone()))
                .email(emptyToNull(validator.getEmail()))
                .currentCompany(validator.getCurrentCompany())
                .currentPosition(validator.getCurrentPosition())
                .education(validator.getEducation())
                .city(validator.getCity())
                .source(validator.getSource() == null ? "sourcing" : validator.getSource())
                .ownerId(validator.getOwnerId() == null ? currentUserId : validator.getOwnerId())
                .remark(validator.getRemark())
                .build();
        boolean ok = this.save(candidate);
        if (ok && validator.getTagIds() != null && !validator.getTagIds().isEmpty()) {
            this.setTags(candidate.getId(), validator.getTagIds());
        }
        return ok;
    }

    @Transactional
    @Override
    public boolean updateCandidate(Integer id, CreateOrUpdateCandidateDTO validator) {
        CandidateDO candidate = this.getById(id);
        if (candidate == null) {
            throw new NotFoundException(80170);
        }
        assertNotDuplicate(validator.getPhone(), validator.getEmail(), id);
        candidate.setName(validator.getName());
        candidate.setPhone(emptyToNull(validator.getPhone()));
        candidate.setEmail(emptyToNull(validator.getEmail()));
        candidate.setCurrentCompany(validator.getCurrentCompany());
        candidate.setCurrentPosition(validator.getCurrentPosition());
        if (validator.getEducation() != null) {
            candidate.setEducation(validator.getEducation());
        }
        candidate.setCity(validator.getCity());
        if (validator.getSource() != null) {
            candidate.setSource(validator.getSource());
        }
        if (validator.getOwnerId() != null) {
            candidate.setOwnerId(validator.getOwnerId());
        }
        candidate.setRemark(validator.getRemark());
        boolean ok = this.updateById(candidate);
        if (ok && validator.getTagIds() != null) {
            this.setTags(id, validator.getTagIds());
        }
        return ok;
    }

    @Transactional
    @Override
    public boolean deleteCandidate(Integer id) {
        CandidateDO candidate = this.getById(id);
        if (candidate == null) {
            throw new NotFoundException(80170);
        }
        QueryWrapper<CandidateTagDO> relationWrapper = new QueryWrapper<>();
        relationWrapper.lambda().eq(CandidateTagDO::getCandidateId, id);
        candidateTagMapper.delete(relationWrapper);
        return this.removeById(id);
    }

    @Transactional
    @Override
    public boolean setTags(Integer candidateId, List<Integer> tagIds) {
        QueryWrapper<CandidateTagDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(CandidateTagDO::getCandidateId, candidateId);
        candidateTagMapper.delete(wrapper);
        if (tagIds == null || tagIds.isEmpty()) {
            return true;
        }
        List<TagDO> existTags = tagMapper.selectBatchIds(tagIds);
        for (TagDO tag : existTags) {
            CandidateTagDO relation = CandidateTagDO.builder()
                    .candidateId(candidateId)
                    .tagId(tag.getId())
                    .build();
            candidateTagMapper.insert(relation);
        }
        return true;
    }

    @Override
    public List<String> checkDuplicate(String phone, String email, Integer excludeId) {
        List<String> conflicts = new ArrayList<>();
        if (phone != null && !phone.isEmpty()) {
            QueryWrapper<CandidateDO> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(CandidateDO::getPhone, phone);
            if (excludeId != null) {
                wrapper.lambda().ne(CandidateDO::getId, excludeId);
            }
            if (this.count(wrapper) > 0) {
                conflicts.add("手机号已存在");
            }
        }
        if (email != null && !email.isEmpty()) {
            QueryWrapper<CandidateDO> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(CandidateDO::getEmail, email);
            if (excludeId != null) {
                wrapper.lambda().ne(CandidateDO::getId, excludeId);
            }
            if (this.count(wrapper) > 0) {
                conflicts.add("邮箱已存在");
            }
        }
        return conflicts;
    }

    @Override
    public CandidateImportResultVO importResume(MultipartFile file, Integer currentUserId) {
        if (file == null || file.isEmpty()) {
            throw new ForbiddenException(80190);
        }
        if (!resumeParser.supported(file.getOriginalFilename())) {
            throw new ForbiddenException(80200);
        }
        String text = resumeParser.extractText(file);
        CandidateDO draft = parseWithAiFallbackToRules(text, file.getOriginalFilename());
        if (draft.getName() == null || draft.getName().isEmpty()) {
            draft.setName("待补录_" + System.currentTimeMillis() % 100000);
        }
        CandidateImportResultVO.CandidateImportResultVOBuilder builder = CandidateImportResultVO.builder()
                .total(1);
        List<String> conflicts = checkDuplicate(draft.getPhone(), draft.getEmail(), null);
        if (!conflicts.isEmpty()) {
            return builder.successCount(0).duplicateCount(1).failCount(0)
                    .duplicates(Arrays.asList(draft.getName() + "：" + String.join("、", conflicts)))
                    .build();
        }
        try {
            draft.setResumeFile(resumeParser.saveResumeFile(file));
        } catch (Exception e) {
            // 文件保存失败不阻断导入，只记录
            log.warn("简历原件保存失败，继续导入: {}", file.getOriginalFilename());
        }
        draft.setOwnerId(currentUserId);
        // 简历全文入库作为 AI 检索语料（迭代20）
        draft.setResumeText(text);
        this.save(draft);
        return builder.successCount(1).duplicateCount(0).failCount(0).build();
    }

    /**
     * 简历解析编排（迭代19）：
     * 1. 功能开关 resume_parse 开启且 AI_API_KEY 已配置 → LLM 结构化抽取（AI 优先）
     * 2. 开关关闭 / Key 缺失 / AI 调用失败 → 降级为本地规则解析（行为与迭代2完全一致）
     * AI 结果仅覆盖非空字段，规则兜底字段不被空值冲掉。
     */
    private CandidateDO parseWithAiFallbackToRules(String text, String filename) {
        CandidateDO draft = resumeParser.parseText(text, filename);
        boolean aiEnabled = featureFlagService.isEnabled("resume_parse");
        if (!aiEnabled || !aiResumeParser.isAvailable()) {
            return draft;
        }
        try {
            Map<String, String> fields = aiResumeParser.parse(text);
            mergeNonBlank(draft::setName, fields.get("name"));
            mergeNonBlank(draft::setPhone, fields.get("phone"));
            mergeNonBlank(draft::setEmail, fields.get("email"));
            mergeNonBlank(draft::setCurrentCompany, fields.get("currentCompany"));
            mergeNonBlank(draft::setCurrentPosition, fields.get("currentPosition"));
            mergeNonBlank(draft::setCity, fields.get("city"));
            String education = candidateExcelHelper.educationToKey(fields.get("education"));
            mergeNonBlank(draft::setEducation, education);
            draft.setRemark("[AI解析] " + (draft.getRemark() == null ? "" : draft.getRemark()));
            log.info("简历 AI 解析完成: name={}, phone={}", fields.get("name"),
                    fields.get("phone") == null || fields.get("phone").isEmpty() ? "未识别" : "已识别");
        } catch (Exception e) {
            log.warn("AI 解析降级为规则解析: {}", e.getMessage());
        }
        return draft;
    }

    /** 仅当 AI 抽取值非空时覆盖（规则解析已有的高置信字段不被空值冲掉） */
    private void mergeNonBlank(java.util.function.Consumer<String> setter, String value) {
        if (value != null && !value.isBlank()) {
            setter.accept(value.trim());
        }
    }

    @Override
    public CandidateImportResultVO importExcel(MultipartFile file, Integer currentUserId) {
        if (file == null || file.isEmpty()) {
            throw new ForbiddenException(80190);
        }
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        if (!name.endsWith(".xls") && !name.endsWith(".xlsx")) {
            throw new ForbiddenException(80200);
        }
        List<CandidateExcelRow> rows = candidateExcelHelper.readRows(file);
        int success = 0;
        int duplicate = 0;
        int fail = 0;
        List<String> duplicates = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        // 文件内查重缓存：手机号/邮箱 -> 行号
        Map<String, Integer> seenPhones = new HashMap<>();
        Map<String, Integer> seenEmails = new HashMap<>();
        for (CandidateExcelRow row : rows) {
            String label = row.getName() == null ? ("第" + row.getRowIndex() + "行") : row.getName();
            if (row.getName() == null || row.getName().trim().isEmpty()) {
                fail++;
                failures.add("第" + row.getRowIndex() + "行：缺少姓名，已跳过");
                continue;
            }
            String phone = emptyToNull(row.getPhone());
            String email = emptyToNull(row.getEmail());
            if (phone != null && !PHONE_CHECK.matcher(phone).matches()) {
                fail++;
                failures.add("第" + row.getRowIndex() + "行：手机号格式不正确（" + phone + "）");
                continue;
            }
            if (email != null && !EMAIL_CHECK.matcher(email).matches()) {
                fail++;
                failures.add("第" + row.getRowIndex() + "行：邮箱格式不正确（" + email + "）");
                continue;
            }
            boolean dupInFile = (phone != null && seenPhones.containsKey(phone))
                    || (email != null && seenEmails.containsKey(email));
            List<String> conflicts = checkDuplicate(phone, email, null);
            if (dupInFile || !conflicts.isEmpty()) {
                duplicate++;
                duplicates.add(label + "：" + (dupInFile ? "与文件内其他行重复" : String.join("、", conflicts)));
                continue;
            }
            CandidateDO candidate = CandidateDO.builder()
                    .name(row.getName().trim())
                    .phone(phone)
                    .email(email)
                    .currentCompany(row.getCurrentCompany())
                    .currentPosition(row.getCurrentPosition())
                    .education(candidateExcelHelper.educationToKey(row.getEducationText()))
                    .city(row.getCity())
                    .source("sourcing")
                    .ownerId(currentUserId)
                    .remark(row.getRemark())
                    .build();
            this.save(candidate);
            success++;
            if (phone != null) {
                seenPhones.put(phone, row.getRowIndex());
            }
            if (email != null) {
                seenEmails.put(email, row.getRowIndex());
            }
        }
        return CandidateImportResultVO.builder()
                .total(rows.size())
                .successCount(success)
                .duplicateCount(duplicate)
                .failCount(fail)
                .duplicates(duplicates)
                .failures(failures)
                .build();
    }

    @Override
    public List<CandidateDO> getCandidateOptions() {
        QueryWrapper<CandidateDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().select(CandidateDO::getId, CandidateDO::getName, CandidateDO::getPhone,
                        CandidateDO::getCurrentCompany, CandidateDO::getCurrentPosition)
                .orderByDesc(CandidateDO::getId)
                .last("LIMIT 500");
        return this.list(wrapper);
    }

    // ---------------- 私有方法 ----------------

    private void assertNotDuplicate(String phone, String email, Integer excludeId) {
        List<String> conflicts = checkDuplicate(emptyToNull(phone), emptyToNull(email), excludeId);
        if (!conflicts.isEmpty()) {
            throw new ForbiddenException(80180);
        }
    }

    private String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    /**
     * 批量填充候选人的标签（标签名 + 标签id）
     */
    private void fillTags(List<CandidateDO> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return;
        }
        List<Integer> candidateIds = candidates.stream()
                .map(CandidateDO::getId)
                .collect(Collectors.toList());
        QueryWrapper<CandidateTagDO> relationWrapper = new QueryWrapper<>();
        relationWrapper.lambda().in(CandidateTagDO::getCandidateId, candidateIds);
        List<CandidateTagDO> relations = candidateTagMapper.selectList(relationWrapper);
        if (relations.isEmpty()) {
            candidates.forEach(c -> {
                c.setTagNames(new ArrayList<>());
                c.setTagIds(new ArrayList<>());
            });
            return;
        }
        List<Integer> tagIds = relations.stream()
                .map(CandidateTagDO::getTagId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> tagNameMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(TagDO::getId, TagDO::getName));
        Map<Integer, List<Integer>> candidateTagIds = new HashMap<>();
        Map<Integer, List<String>> candidateTagNames = new HashMap<>();
        for (CandidateTagDO relation : relations) {
            candidateTagIds.computeIfAbsent(relation.getCandidateId(), k -> new ArrayList<>())
                    .add(relation.getTagId());
            String tagName = tagNameMap.get(relation.getTagId());
            if (tagName != null) {
                candidateTagNames.computeIfAbsent(relation.getCandidateId(), k -> new ArrayList<>())
                        .add(tagName);
            }
        }
        for (CandidateDO candidate : candidates) {
            candidate.setTagIds(candidateTagIds.getOrDefault(candidate.getId(), new ArrayList<>()));
            candidate.setTagNames(candidateTagNames.getOrDefault(candidate.getId(), new ArrayList<>()));
        }
    }
}
