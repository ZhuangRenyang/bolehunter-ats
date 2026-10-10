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
package com.bolehunter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.dto.dict.CreateOrUpdateDictDataDTO;
import com.bolehunter.dto.dict.CreateOrUpdateDictTypeDTO;
import com.bolehunter.model.DictDataDO;
import com.bolehunter.model.DictTypeDO;

import java.util.List;

/**
 * 字典服务（类型 + 数据）
 */
public interface DictService {

    // ---------- 字典类型 ----------
    IPage<DictTypeDO> getTypePage(Integer page, Integer count);

    List<DictTypeDO> getAllTypes();

    boolean createType(CreateOrUpdateDictTypeDTO validator);

    boolean updateType(DictTypeDO type, CreateOrUpdateDictTypeDTO validator);

    boolean deleteType(Integer id);

    // ---------- 字典数据 ----------
    IPage<DictDataDO> getDataPage(String dictType, Integer page, Integer count);

    DictTypeDO getTypeById(Integer id);

    DictDataDO getDataById(Integer id);

    List<DictDataDO> getDataByType(String dictType);

    boolean createData(CreateOrUpdateDictDataDTO validator);

    boolean updateData(DictDataDO data, CreateOrUpdateDictDataDTO validator);

    boolean deleteData(Integer id);
}
