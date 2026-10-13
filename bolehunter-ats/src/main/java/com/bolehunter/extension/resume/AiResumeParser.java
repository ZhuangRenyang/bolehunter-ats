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
package com.bolehunter.extension.resume;

import com.bolehunter.extension.ai.AiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 简历 AI 解析器（迭代19）：调用 AiClient 做结构化抽取。
 * <p>配置优先级与降级策略见 AiClient 与 CandidateServiceImpl 编排；
 * Key 未配置/调用失败时由上层降级为规则解析。API Key 永不写入日志。</p>
 */
@Component
public class AiResumeParser {

    private static final String SYSTEM_PROMPT = """
            你是猎头 ATS 的简历解析引擎。从用户提供的简历文本中抽取以下字段，输出严格 JSON（不要输出任何解释或 markdown 代码块）：
            {"name":"姓名","phone":"手机号","email":"邮箱","currentCompany":"当前公司，无则空串","currentPosition":"当前职位，无则空串","education":"学历，取值只能是 doctor/master/bachelor/associate/other 之一","city":"当前所在城市，无则空串"}
            规则：找不到的字段一律输出空串；不得编造；手机号为 11 位大陆手机号。
            """;

    @Autowired
    private AiClient aiClient;

    /** 是否具备调用条件（后台或环境变量任一处已配置 Key） */
    public boolean isAvailable() {
        return aiClient.isChatAvailable();
    }

    /**
     * AI 结构化解析简历文本
     *
     * @return 字段 map（name/phone/email/currentCompany/currentPosition/education/city），解析失败抛 RuntimeException
     */
    public Map<String, String> parse(String resumeText) {
        String content = aiClient.chat(SYSTEM_PROMPT, resumeText, 500);
        return extractJson(content);
    }

    /**
     * 从模型输出中稳健抽取 JSON：剥 markdown 围栏、定位首尾大括号，字段缺失填空串
     */
    Map<String, String> extractJson(String content) {
        String json = content == null ? "" : content.trim();
        // 剥 ```json ... ``` 围栏
        if (json.startsWith("```")) {
            json = json.replaceAll("^```[a-zA-Z]*\\s*", "").replaceAll("```\\s*$", "").trim();
        }
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new RuntimeException("模型未返回 JSON: " + content);
        }
        try {
            com.fasterxml.jackson.databind.JsonNode node =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(json.substring(start, end + 1));
            Map<String, String> fields = new LinkedHashMap<>();
            for (String key : List.of("name", "phone", "email", "currentCompany", "currentPosition", "education", "city")) {
                fields.put(key, node.path(key).asText(""));
            }
            return fields;
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败: " + e.getMessage(), e);
        }
    }
}
