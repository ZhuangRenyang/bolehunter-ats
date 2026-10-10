package com.bolehunter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.dto.flag.CreateOrUpdateFeatureFlagDTO;
import com.bolehunter.model.FeatureFlagDO;

import java.util.List;

/**
 * 功能开关服务
 */
public interface FeatureFlagService extends IService<FeatureFlagDO> {

    List<FeatureFlagDO> getAllFlags();

    boolean createFlag(CreateOrUpdateFeatureFlagDTO validator);

    boolean updateFlag(FeatureFlagDO flag, CreateOrUpdateFeatureFlagDTO validator);

    /**
     * 切换开关状态，返回切换后的状态（1 开 / 0 关）
     */
    int toggleFlag(Integer id);

    boolean checkKeyExist(String flagKey);
}
