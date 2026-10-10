/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bolehunter.common.mybatis.Page;
import com.bolehunter.dto.position.CreateOrUpdatePositionDTO;
import com.bolehunter.model.PositionDO;
import com.bolehunter.mapper.PositionMapper;
import com.bolehunter.model.result.PositionResultDO;
import com.bolehunter.service.PositionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Eval
 * @since 2022-04-02
 */
@Service
public class PositionServiceImpl extends ServiceImpl<PositionMapper, PositionDO> implements PositionService {

    @Resource
    private PositionMapper positionMapper;

    @Override
    public PositionResultDO getById(Integer id) {
        return positionMapper.getById(id);
    }

    @Override
    public boolean updateState(Integer id, Integer state) {
        return positionMapper.updateState(id, state);
    }

    @Override
    public boolean updateHits(Integer id) {
        return positionMapper.updateHits(id);
    }

    @Override
    public PositionDO findById(Integer id) {
        return positionMapper.selectById(id);
    }

    @Override
    public boolean createPosition(CreateOrUpdatePositionDTO validator) {
        PositionDO positionDO = new PositionDO();
        positionDO.setTitle(validator.getTitle());
        positionDO.setRequirement(validator.getRequirement());
        positionDO.setQuantity(validator.getQuantity());
        positionDO.setCity(validator.getCity());
        positionDO.setSalaryUp(validator.getSalaryUp());
        positionDO.setSalaryDown(validator.getSalaryDown());
        positionDO.setCategoryId(validator.getCategoryId());
        positionDO.setHrId(validator.getHrId());
        positionDO.setCompanyId(validator.getCompanyId());
        positionDO.setEduBack(validator.getEduBack());
        return positionMapper.insert(positionDO) > 0;
    }

    @Override
    public boolean updatePosition(PositionDO positionDO, CreateOrUpdatePositionDTO validator) {
        positionDO.setTitle(validator.getTitle());
        positionDO.setRequirement(validator.getRequirement());
        positionDO.setQuantity(validator.getQuantity());
        positionDO.setCity(validator.getCity());
        positionDO.setSalaryUp(validator.getSalaryUp());
        positionDO.setSalaryDown(validator.getSalaryDown());
        positionDO.setCategoryId(validator.getCategoryId());
        positionDO.setHrId(validator.getHrId());
        positionDO.setCompanyId(validator.getCompanyId());
        positionDO.setEduBack(validator.getEduBack());
        // 更新职位信息之后，需要管理员重新审核，把职位状态state改为0
        positionDO.setState(0);
        return positionMapper.updateById(positionDO) > 0;
    }

    @Override
    public List<PositionResultDO> sort() {
        return positionMapper.sortByHits();
    }

    @Override
    public IPage<PositionDO> getPage(Page<PositionDO> pager, Integer state) {
        return positionMapper.getPage(pager,state);
    }

    @Override
    public IPage<PositionResultDO> getPageByKeyword(Page<PositionResultDO> pager, String keyword) {
        return positionMapper.getPageByKeyword(pager,keyword);
    }
}
