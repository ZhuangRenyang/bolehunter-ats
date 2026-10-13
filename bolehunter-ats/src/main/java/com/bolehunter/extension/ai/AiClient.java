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
package com.bolehunter.extension.ai;

import com.bolehunter.service.AppSettingService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI 统一客户端（迭代20）：OpenAI 兼容协议的 chat 与 embeddings。
 * <p>配置优先级：后台（app_setting）> 环境变量（AI_BASE_URL/AI_MODEL/AI_API_KEY/AI_EMBEDDING_MODEL）。
 * embeddings 需要供应商支持（DeepSeek 无 embeddings，通义/OpenAI 有）；不可用时上层降级关键词检索。
 * API Key 永不写入日志。</p>
 */
@Component
public class AiClient {

    private static final Logger log = LoggerFactory.getLogger(AiClient.class);

    @Value("${ai.base-url:https://api.deepseek.com}")
    private String envBaseUrl;

    @Value("${ai.model:deepseek-chat}")
    private String envModel;

    @Value("${ai.api-key:}")
    private String envApiKey;

    @Value("${ai.embedding-model:text-embedding-v3}")
    private String envEmbeddingModel;

    @Value("${ai.timeout-seconds:30}")
    private int timeoutSeconds;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @org.springframework.beans.factory.annotation.Autowired
    private AppSettingService appSettingService;

    public String resolveApiKey() {
        String value = appSettingService.getValue(AppSettingService.KEY_AI_API_KEY, envApiKey);
        return value == null ? "" : value.trim();
    }

    public String resolveBaseUrl() {
        String value = appSettingService.getValue(AppSettingService.KEY_AI_BASE_URL, envBaseUrl);
        return value == null ? envBaseUrl : value.trim();
    }

    public String resolveModel() {
        String value = appSettingService.getValue(AppSettingService.KEY_AI_MODEL, envModel);
        return value == null ? envModel : value.trim();
    }

    public String resolveEmbeddingModel() {
        String value = appSettingService.getValue(AppSettingService.KEY_AI_EMBEDDING_MODEL, envEmbeddingModel);
        return value == null ? envEmbeddingModel : value.trim();
    }

    /** chat 是否可用（Key 已配置） */
    public boolean isChatAvailable() {
        return !resolveApiKey().isBlank();
    }

    /** embeddings 是否可用（Key 已配置；供应商是否支持由调用结果判断） */
    public boolean isEmbeddingAvailable() {
        return isChatAvailable();
    }

    /**
     * 对话补全
     *
     * @param system    系统提示词
     * @param user      用户输入
     * @param maxTokens 回复长度上限
     * @return 模型回复文本
     */
    public String chat(String system, String user, int maxTokens) {
        String apiKey = resolveApiKey();
        if (apiKey.isBlank()) {
            throw new IllegalStateException("AI_API_KEY 未配置（后台或环境变量）");
        }
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", resolveModel(),
                    "temperature", 0,
                    "max_tokens", maxTokens,
                    "messages", List.of(
                            Map.of("role", "system", "content", system),
                            Map.of("role", "user", "content", user))));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resolveBaseUrl() + "/chat/completions"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("LLM 接口返回 " + response.statusCode());
            }
            return objectMapper.readTree(response.body())
                    .path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.warn("AI chat 调用失败: {}", e.getMessage());
            throw new RuntimeException("AI 调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 文本向量化（OpenAI 兼容 /embeddings）
     *
     * @return 与入参顺序一致的向量列表
     */
    public List<double[]> embed(List<String> texts) {
        String apiKey = resolveApiKey();
        if (apiKey.isBlank()) {
            throw new IllegalStateException("AI_API_KEY 未配置");
        }
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", resolveEmbeddingModel(),
                    "input", texts));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(resolveBaseUrl() + "/embeddings"))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Embeddings 接口返回 " + response.statusCode());
            }
            JsonNode data = objectMapper.readTree(response.body()).path("data");
            List<double[]> vectors = new ArrayList<>();
            for (JsonNode item : data) {
                JsonNode arr = item.path("embedding");
                double[] vec = new double[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    vec[i] = arr.get(i).asDouble();
                }
                vectors.add(vec);
            }
            return vectors;
        } catch (Exception e) {
            log.warn("AI embeddings 调用失败: {}", e.getMessage());
            throw new RuntimeException("Embeddings 失败: " + e.getMessage(), e);
        }
    }
}
