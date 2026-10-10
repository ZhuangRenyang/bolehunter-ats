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
import com.bolehunter.common.mybatis.Page;
import com.bolehunter.dto.dict.CreateOrUpdateDictDataDTO;
import com.bolehunter.dto.dict.CreateOrUpdateDictTypeDTO;
import com.bolehunter.mapper.DictDataMapper;
import com.bolehunter.mapper.DictTypeMapper;
import com.bolehunter.model.DictDataDO;
import com.bolehunter.model.DictTypeDO;
import com.bolehunter.service.DictService;
import com.bolehunter.core.exception.ForbiddenException;
import com.bolehunter.core.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典服务实现
 */
@Service
public class DictServiceImpl implements DictService {

    @Autowired
    private DictTypeMapper dictTypeMapper;

    @Autowired
    private DictDataMapper dictDataMapper;

    // ---------- 字典类型 ----------

    @Override
    public IPage<DictTypeDO> getTypePage(Integer page, Integer count) {
        Page<DictTypeDO> pager = new Page<>(page, count);
        QueryWrapper<DictTypeDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().orderByAsc(DictTypeDO::getId);
        return dictTypeMapper.selectPage(pager, wrapper);
    }

    @Override
    public List<DictTypeDO> getAllTypes() {
        QueryWrapper<DictTypeDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().orderByAsc(DictTypeDO::getId);
        return dictTypeMapper.selectList(wrapper);
    }

    @Override
    public boolean createType(CreateOrUpdateDictTypeDTO validator) {
        if (checkTypeExist(validator.getDictType())) {
            throw new ForbiddenException(80111);
        }
        DictTypeDO type = DictTypeDO.builder()
                .dictName(validator.getDictName())
                .dictType(validator.getDictType())
                .status(validator.getStatus() == null ? 1 : validator.getStatus())
                .remark(validator.getRemark())
                .build();
        return dictTypeMapper.insert(type) > 0;
    }

    @Override
    public boolean updateType(DictTypeDO type, CreateOrUpdateDictTypeDTO validator) {
        if (!type.getDictType().equals(validator.getDictType()) && checkTypeExist(validator.getDictType())) {
            throw new ForbiddenException(80111);
        }
        type.setDictName(validator.getDictName());
        type.setDictType(validator.getDictType());
        if (validator.getStatus() != null) {
            type.setStatus(validator.getStatus());
        }
        type.setRemark(validator.getRemark());
        return dictTypeMapper.updateById(type) > 0;
    }

    @Transactional
    @Override
    public boolean deleteType(Integer id) {
        DictTypeDO type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw new NotFoundException(80110);
        }
        // 同步删除该类型下的字典数据
        QueryWrapper<DictDataDO> dataWrapper = new QueryWrapper<>();
        dataWrapper.lambda().eq(DictDataDO::getDictType, type.getDictType());
        dictDataMapper.delete(dataWrapper);
        return dictTypeMapper.deleteById(id) > 0;
    }

    // ---------- 字典数据 ----------

    @Override
    public IPage<DictDataDO> getDataPage(String dictType, Integer page, Integer count) {
        Page<DictDataDO> pager = new Page<>(page, count);
        QueryWrapper<DictDataDO> wrapper = new QueryWrapper<>();
        if (dictType != null && !dictType.isEmpty()) {
            wrapper.lambda().eq(DictDataDO::getDictType, dictType);
        }
        wrapper.lambda().orderByAsc(DictDataDO::getDictSort).orderByAsc(DictDataDO::getId);
        return dictDataMapper.selectPage(pager, wrapper);
    }

    @Override
    public DictTypeDO getTypeById(Integer id) {
        return dictTypeMapper.selectById(id);
    }

    @Override
    public DictDataDO getDataById(Integer id) {
        return dictDataMapper.selectById(id);
    }

    @Override
    public List<DictDataDO> getDataByType(String dictType) {
        QueryWrapper<DictDataDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(DictDataDO::getDictType, dictType)
                .eq(DictDataDO::getStatus, 1)
                .orderByAsc(DictDataDO::getDictSort)
                .orderByAsc(DictDataDO::getId);
        return dictDataMapper.selectList(wrapper);
    }

    @Override
    public boolean createData(CreateOrUpdateDictDataDTO validator) {
        DictDataDO data = DictDataDO.builder()
                .dictType(validator.getDictType())
                .dictLabel(validator.getDictLabel())
                .dictValue(validator.getDictValue())
                .dictSort(validator.getDictSort() == null ? 0 : validator.getDictSort())
                .status(validator.getStatus() == null ? 1 : validator.getStatus())
                .remark(validator.getRemark())
                .build();
        return dictDataMapper.insert(data) > 0;
    }

    @Override
    public boolean updateData(DictDataDO data, CreateOrUpdateDictDataDTO validator) {
        data.setDictType(validator.getDictType());
        data.setDictLabel(validator.getDictLabel());
        data.setDictValue(validator.getDictValue());
        if (validator.getDictSort() != null) {
            data.setDictSort(validator.getDictSort());
        }
        if (validator.getStatus() != null) {
            data.setStatus(validator.getStatus());
        }
        data.setRemark(validator.getRemark());
        return dictDataMapper.updateById(data) > 0;
    }

    @Override
    public boolean deleteData(Integer id) {
        DictDataDO data = dictDataMapper.selectById(id);
        if (data == null) {
            throw new NotFoundException(80120);
        }
        return dictDataMapper.deleteById(id) > 0;
    }

    private boolean checkTypeExist(String dictType) {
        QueryWrapper<DictTypeDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(DictTypeDO::getDictType, dictType);
        return dictTypeMapper.selectCount(wrapper) > 0;
    }
}
