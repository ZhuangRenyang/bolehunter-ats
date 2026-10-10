package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.dto.flag.CreateOrUpdateFeatureFlagDTO;
import com.bolehunter.mapper.FeatureFlagMapper;
import com.bolehunter.model.FeatureFlagDO;
import com.bolehunter.service.FeatureFlagService;
import com.bolehunter.core.exception.ForbiddenException;
import com.bolehunter.core.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 功能开关服务实现
 */
@Service
public class FeatureFlagServiceImpl extends ServiceImpl<FeatureFlagMapper, FeatureFlagDO> implements FeatureFlagService {

    @Override
    public List<FeatureFlagDO> getAllFlags() {
        QueryWrapper<FeatureFlagDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().orderByAsc(FeatureFlagDO::getId);
        return this.list(wrapper);
    }

    @Override
    public boolean createFlag(CreateOrUpdateFeatureFlagDTO validator) {
        if (this.checkKeyExist(validator.getFlagKey())) {
            throw new ForbiddenException(80131);
        }
        FeatureFlagDO flag = FeatureFlagDO.builder()
                .flagKey(validator.getFlagKey())
                .flagName(validator.getFlagName())
                .description(validator.getDescription())
                .enabled(validator.getEnabled() == null ? 0 : validator.getEnabled())
                .build();
        return this.save(flag);
    }

    @Override
    public boolean updateFlag(FeatureFlagDO flag, CreateOrUpdateFeatureFlagDTO validator) {
        if (!flag.getFlagKey().equals(validator.getFlagKey()) && this.checkKeyExist(validator.getFlagKey())) {
            throw new ForbiddenException(80131);
        }
        flag.setFlagKey(validator.getFlagKey());
        flag.setFlagName(validator.getFlagName());
        flag.setDescription(validator.getDescription());
        if (validator.getEnabled() != null) {
            flag.setEnabled(validator.getEnabled());
        }
        return this.updateById(flag);
    }

    @Override
    public int toggleFlag(Integer id) {
        FeatureFlagDO flag = this.getById(id);
        if (flag == null) {
            throw new NotFoundException(80130);
        }
        int newEnabled = (flag.getEnabled() != null && flag.getEnabled() == 1) ? 0 : 1;
        flag.setEnabled(newEnabled);
        this.updateById(flag);
        return newEnabled;
    }

    @Override
    public boolean checkKeyExist(String flagKey) {
        QueryWrapper<FeatureFlagDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(FeatureFlagDO::getFlagKey, flagKey);
        return this.count(wrapper) > 0;
    }
}
