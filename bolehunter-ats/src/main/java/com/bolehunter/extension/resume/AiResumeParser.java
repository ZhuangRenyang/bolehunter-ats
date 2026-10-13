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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 简历 AI 解析器（迭代19：resume_parse 开关的 AI 增强通道）
 * <p>调用 OpenAI 兼容 chat/completions 接口（DeepSeek/通义/Kimi 等），对简历全文做结构化抽取。
 * 配置来自环境变量：AI_BASE_URL / AI_MODEL / AI_API_KEY；Key 为空或调用失败时由上层降级为规则解析。
 * 仅将简历文本发送给所配置的大模型服务商，部署者自行选择可信供应商。</p>
 */
@Component
public class AiResumeParser {

    private static final Logger log = LoggerFactory.getLogger(AiResumeParser.class);

    private static final String SYSTEM_PROMPT = """
            你是猎头 ATS 的简历解析引擎。从用户提供的简历文本中抽取以下字段，输出严格 JSON（不要输出任何解释或 markdown 代码块）：
            {"name":"姓名","phone":"手机号","email":"邮箱","currentCompany":"当前公司，无则空串","currentPosition":"当前职位，无则空串","education":"学历，取值只能是 doctor/master/bachelor/associate/other 之一","city":"当前所在城市，无则空串"}
            规则：找不到的字段一律输出空串；不得编造；手机号为 11 位大陆手机号。
            """;

    @Value("${ai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.timeout-seconds:30}")
    private int timeoutSeconds;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 是否具备调用条件（Key 已配置） */
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * AI 结构化解析简历文本
     *
     * @return 字段 map（name/phone/email/currentCompany/currentPosition/education/city），解析失败抛 RuntimeException
     */
    public Map<String, String> parse(String resumeText) {
        if (!isAvailable()) {
            throw new IllegalStateException("AI_API_KEY 未配置");
        }
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "temperature", 0,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user", "content", resumeText))));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("LLM 接口返回 " + response.statusCode());
            }
            String content = objectMapper.readTree(response.body())
                    .path("choices").get(0).path("message").path("content").asText();
            return extractJson(content);
        } catch (Exception e) {
            log.warn("AI 简历解析失败，将由上层降级为规则解析: {}", e.getMessage());
            throw new RuntimeException("AI 解析失败: " + e.getMessage(), e);
        }
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
            JsonNode node = objectMapper.readTree(json.substring(start, end + 1));
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
