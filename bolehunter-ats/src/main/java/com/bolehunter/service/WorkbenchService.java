package com.bolehunter.service;

import java.util.List;
import java.util.Map;

/**
 * 工作台聚合服务
 */
public interface WorkbenchService {

    /**
     * 工作台聚合：统计计数 + 阶段分布 + 今日到期/已逾期任务
     */
    Map<String, Object> summary(Integer userId);
}
