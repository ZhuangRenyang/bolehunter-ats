package com.bolehunter.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 候选人导入结果报告
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateImportResultVO {

    /**
     * 总条数（文件行数/单文件计 1）
     */
    private int total;

    /**
     * 导入成功数
     */
    private int successCount;

    /**
     * 重复跳过数（手机号或邮箱已存在）
     */
    private int duplicateCount;

    /**
     * 解析/校验失败数
     */
    private int failCount;

    /**
     * 重复明细（候选人标识 + 冲突原因）
     */
    @Builder.Default
    private List<String> duplicates = new ArrayList<>();

    /**
     * 失败明细（原因）
     */
    @Builder.Default
    private List<String> failures = new ArrayList<>();
}
