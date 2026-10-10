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
