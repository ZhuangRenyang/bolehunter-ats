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
import com.bolehunter.mapper.CandidateMapper;
import com.bolehunter.mapper.CompanyMapper;
import com.bolehunter.mapper.RecommendationMapper;
import com.bolehunter.model.CandidateDO;
import com.bolehunter.model.CompanyDO;
import com.bolehunter.model.RecommendationDO;
import com.bolehunter.service.TaskService;
import com.bolehunter.service.WorkbenchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台聚合服务实现（数据量小，统计直接走内存分组）
 */
@Service
public class WorkbenchServiceImpl implements WorkbenchService {

    private static final List<String> STAGE_KEYS = Arrays.asList(
            "PENDING", "RECOMMENDED", "CLIENT_READ", "INTERVIEW_PENDING", "INTERVIEWING",
            "OFFER_PENDING", "OFFERED", "ONBOARDED", "PAID", "ARCHIVED");

    @Autowired
    private CandidateMapper candidateMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private TaskService taskService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> summary(Integer userId) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("candidateTotal", candidateMapper.selectCount(new QueryWrapper<CandidateDO>()));
        summary.put("clientTotal", companyMapper.selectCount(new QueryWrapper<CompanyDO>()));
        summary.put("recommendationTotal", recommendationMapper.selectCount(new QueryWrapper<RecommendationDO>()));

        // 阶段分布：全量取 current_stage 字段后内存分组
        QueryWrapper<RecommendationDO> stageWrapper = new QueryWrapper<>();
        stageWrapper.lambda().select(RecommendationDO::getCurrentStage).last("LIMIT 2000");
        Map<String, Long> stageCount = new HashMap<>();
        recommendationMapper.selectList(stageWrapper)
                .forEach(item -> stageCount.merge(item.getCurrentStage(), 1L, Long::sum));
        List<Map<String, Object>> stageStats = new ArrayList<>();
        for (String stage : STAGE_KEYS) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stage", stage);
            item.put("count", stageCount.getOrDefault(stage, 0L));
            stageStats.add(item);
        }
        summary.put("stageStats", stageStats);

        summary.put("todayTasks", taskService.getMyTodo(userId, "today"));
        summary.put("overdueTasks", taskService.getMyTodo(userId, "overdue"));
        return summary;
    }

    // ==================== 数据看板增强（迭代14） ====================

    /** 已读及之后的阶段（客户已读转化口径） */
    private static final String READ_STAGES =
            "'CLIENT_READ','INTERVIEW_PENDING','INTERVIEWING','OFFER_PENDING','OFFERED','ONBOARDED','PAID'";
    /** 面试及之后的阶段 */
    private static final String INTERVIEW_STAGES =
            "'INTERVIEWING','OFFER_PENDING','OFFERED','ONBOARDED','PAID'";
    /** Offer 及之后的阶段 */
    private static final String OFFER_STAGES = "'OFFERED','ONBOARDED','PAID'";

    @Override
    public Map<String, Object> dashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("funnelTrend", funnelTrend());
        dashboard.put("consultantRanking", consultantRanking());
        dashboard.put("clientConversion", clientConversion(20));
        return dashboard;
    }

    /** 近 30 天 vs 前 30 天各阶段新增推荐数（MySQL NOW 窗口，避免 JVM 时区差异） */
    private List<Map<String, Object>> funnelTrend() {
        String sql = "SELECT current_stage, "
                + "SUM(CASE WHEN create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) AS recent, "
                + "SUM(CASE WHEN create_time < DATE_SUB(CURDATE(), INTERVAL 30 DAY) "
                + "AND create_time >= DATE_SUB(CURDATE(), INTERVAL 60 DAY) THEN 1 ELSE 0 END) AS prev "
                + "FROM recommendation WHERE delete_time IS NULL "
                + "AND create_time >= DATE_SUB(CURDATE(), INTERVAL 60 DAY) GROUP BY current_stage";
        Map<String, long[]> byStage = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            byStage.put(rs.getString("current_stage"),
                    new long[]{rs.getLong("recent"), rs.getLong("prev")});
        });
        List<Map<String, Object>> trend = new ArrayList<>();
        for (String stage : STAGE_KEYS) {
            long[] counts = byStage.getOrDefault(stage, new long[]{0L, 0L});
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stage", stage);
            item.put("recent", counts[0]);
            item.put("prev", counts[1]);
            trend.add(item);
        }
        return trend;
    }

    /** 本自然月顾问业绩排行（推荐/已读/面试/Offer/回款） */
    private List<Map<String, Object>> consultantRanking() {
        String sql = "SELECT r.owner_id, IFNULL(u.nickname, CONCAT('用户#', r.owner_id)) AS nickname, "
                + "COUNT(*) AS total, "
                + "SUM(CASE WHEN r.current_stage IN (" + READ_STAGES + ") THEN 1 ELSE 0 END) AS read_cnt, "
                + "SUM(CASE WHEN r.current_stage IN (" + INTERVIEW_STAGES + ") THEN 1 ELSE 0 END) AS interview_cnt, "
                + "SUM(CASE WHEN r.current_stage IN (" + OFFER_STAGES + ") THEN 1 ELSE 0 END) AS offer_cnt, "
                + "SUM(CASE WHEN r.current_stage = 'PAID' THEN 1 ELSE 0 END) AS paid_cnt "
                + "FROM recommendation r LEFT JOIN user u ON u.id = r.owner_id AND u.delete_time IS NULL "
                + "WHERE r.delete_time IS NULL AND r.create_time >= DATE_FORMAT(CURDATE(), '%Y-%m-01') "
                + "GROUP BY r.owner_id, u.nickname ORDER BY total DESC, paid_cnt DESC LIMIT 10";
        List<Map<String, Object>> rows = new ArrayList<>();
        jdbcTemplate.query(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("owner_id", rs.getInt("owner_id"));
            row.put("nickname", rs.getString("nickname"));
            row.put("total", rs.getLong("total"));
            row.put("read_cnt", rs.getLong("read_cnt"));
            row.put("interview_cnt", rs.getLong("interview_cnt"));
            row.put("offer_cnt", rs.getLong("offer_cnt"));
            row.put("paid_cnt", rs.getLong("paid_cnt"));
            rows.add(row);
        });
        return rows;
    }

    /** 客户转化率（按公司：推荐→面试→Offer→回款），取推荐量前 N 名 */
    private List<Map<String, Object>> clientConversion(int limit) {
        String sql = "SELECT c.id AS client_id, c.name AS client_name, "
                + "COUNT(r.id) AS total, "
                + "SUM(CASE WHEN r.current_stage IN (" + READ_STAGES + ") THEN 1 ELSE 0 END) AS read_cnt, "
                + "SUM(CASE WHEN r.current_stage IN (" + INTERVIEW_STAGES + ") THEN 1 ELSE 0 END) AS interview_cnt, "
                + "SUM(CASE WHEN r.current_stage IN (" + OFFER_STAGES + ") THEN 1 ELSE 0 END) AS offer_cnt, "
                + "SUM(CASE WHEN r.current_stage = 'PAID' THEN 1 ELSE 0 END) AS paid_cnt "
                + "FROM company c LEFT JOIN recommendation r ON r.client_id = c.id AND r.delete_time IS NULL "
                + "WHERE c.delete_time IS NULL "
                + "GROUP BY c.id, c.name ORDER BY total DESC LIMIT " + (limit > 0 ? Math.min(limit, 100) : 20);
        List<Map<String, Object>> rows = new ArrayList<>();
        jdbcTemplate.query(sql, rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            long total = rs.getLong("total");
            long paid = rs.getLong("paid_cnt");
            row.put("client_id", rs.getInt("client_id"));
            row.put("client_name", rs.getString("client_name"));
            row.put("total", total);
            row.put("read_cnt", rs.getLong("read_cnt"));
            row.put("interview_cnt", rs.getLong("interview_cnt"));
            row.put("offer_cnt", rs.getLong("offer_cnt"));
            row.put("paid_cnt", paid);
            row.put("paid_rate", total == 0 ? 0.0 : Math.round(paid * 1000.0 / total) / 10.0);
            rows.add(row);
        });
        return rows;
    }

    @Override
    public String clientConversionCsv() {
        List<Map<String, Object>> rows = clientConversion(100);
        StringBuilder csv = new StringBuilder();
        csv.append('\ufeff');
        csv.append("客户公司,推荐数,客户已读,面试及之后,Offer及之后,回款数,回款转化率(%)\n");
        for (Map<String, Object> row : rows) {
            csv.append(csvCell(String.valueOf(row.get("client_name")))).append(',')
                    .append(row.get("total")).append(',')
                    .append(row.get("read_cnt")).append(',')
                    .append(row.get("interview_cnt")).append(',')
                    .append(row.get("offer_cnt")).append(',')
                    .append(row.get("paid_cnt")).append(',')
                    .append(row.get("paid_rate")).append('\n');
        }
        return csv.toString();
    }

    /** CSV 单元格转义：包含逗号/引号/换行时用引号包裹 */
    private String csvCell(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
