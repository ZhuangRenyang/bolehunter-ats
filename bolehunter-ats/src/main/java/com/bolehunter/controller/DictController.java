package com.bolehunter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bolehunter.common.util.PageUtil;
import com.bolehunter.dto.dict.CreateOrUpdateDictDataDTO;
import com.bolehunter.dto.dict.CreateOrUpdateDictTypeDTO;
import com.bolehunter.model.DictDataDO;
import com.bolehunter.model.DictTypeDO;
import com.bolehunter.service.DictService;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import com.bolehunter.vo.PageResponseVO;
import com.bolehunter.vo.UpdatedVO;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 字典管理（RuoYi 式：类型 + 数据）
 */
@RestController
@RequestMapping("/recruit/dict")
@PermissionModule(value = "字典管理")
@Validated
public class DictController {

    @Autowired
    private DictService dictService;

    // ---------- 字典类型 ----------

    /**
     * 字典类型分页
     */
    @Logger(template = "查询字典类型分页")
    @GetMapping("/types/page")
    @PermissionMeta(value = "查询字典类型")
    public PageResponseVO<DictTypeDO> typePage(
            @RequestParam(name = "count", required = false, defaultValue = "10")
            @Min(value = 1, message = "{page.count.min}")
            @Max(value = 30, message = "{page.count.max}") Integer count,
            @RequestParam(name = "page", required = false, defaultValue = "0")
            @Min(value = 0, message = "{page.number.min}") Integer page) {
        IPage<DictTypeDO> iPage = dictService.getTypePage(page, count);
        return PageUtil.build(iPage);
    }

    /**
     * 全部字典类型（下拉用，仅需登录）
     */
    @GetMapping("/types")
    @LoginRequired
    public List<DictTypeDO> allTypes() {
        return dictService.getAllTypes();
    }

    /**
     * 新增字典类型
     */
    @Logger(template = "新增字典类型")
    @PostMapping("/types")
    @PermissionMeta(value = "新增字典类型")
    public CreatedVO createType(@RequestBody @Validated CreateOrUpdateDictTypeDTO validator) {
        dictService.createType(validator);
        return new CreatedVO(8100);
    }

    /**
     * 更新字典类型
     */
    @Logger(template = "更新字典类型")
    @PutMapping("/types/{id}")
    @PermissionMeta(value = "更新字典类型")
    public UpdatedVO updateType(@PathVariable @Positive(message = "{id.positive}") Integer id,
                                @RequestBody @Validated CreateOrUpdateDictTypeDTO validator) {
        DictTypeDO type = dictService.getTypeById(id);
        if (type == null) {
            throw new NotFoundException(80110);
        }
        dictService.updateType(type, validator);
        return new UpdatedVO(8110);
    }

    /**
     * 删除字典类型（连同其字典数据）
     */
    @Logger(template = "删除字典类型")
    @DeleteMapping("/types/{id}")
    @PermissionMeta(value = "删除字典类型")
    public DeletedVO deleteType(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        dictService.deleteType(id);
        return new DeletedVO(8120);
    }

    // ---------- 字典数据 ----------

    /**
     * 字典数据分页（可按类型过滤）
     */
    @Logger(template = "查询字典数据分页")
    @GetMapping("/data/page")
    @PermissionMeta(value = "查询字典数据")
    public PageResponseVO<DictDataDO> dataPage(
            @RequestParam(name = "dict_type", required = false) String dictType,
            @RequestParam(name = "count", required = false, defaultValue = "10")
            @Min(value = 1, message = "{page.count.min}")
            @Max(value = 30, message = "{page.count.max}") Integer count,
            @RequestParam(name = "page", required = false, defaultValue = "0")
            @Min(value = 0, message = "{page.number.min}") Integer page) {
        IPage<DictDataDO> iPage = dictService.getDataPage(dictType, page, count);
        return PageUtil.build(iPage);
    }

    /**
     * 按类型查询启用的字典数据（业务下拉用，仅需登录）
     */
    @GetMapping("/data/all/{dictType}")
    @LoginRequired
    public List<DictDataDO> dataByType(@PathVariable @NotBlank String dictType) {
        return dictService.getDataByType(dictType);
    }

    /**
     * 新增字典数据
     */
    @Logger(template = "新增字典数据")
    @PostMapping("/data")
    @PermissionMeta(value = "新增字典数据")
    public CreatedVO createData(@RequestBody @Validated CreateOrUpdateDictDataDTO validator) {
        dictService.createData(validator);
        return new CreatedVO(8130);
    }

    /**
     * 更新字典数据
     */
    @Logger(template = "更新字典数据")
    @PutMapping("/data/{id}")
    @PermissionMeta(value = "更新字典数据")
    public UpdatedVO updateData(@PathVariable @Positive(message = "{id.positive}") Integer id,
                                @RequestBody @Validated CreateOrUpdateDictDataDTO validator) {
        DictDataDO data = dictService.getDataById(id);
        if (data == null) {
            throw new NotFoundException(80120);
        }
        dictService.updateData(data, validator);
        return new UpdatedVO(8140);
    }

    /**
     * 删除字典数据
     */
    @Logger(template = "删除字典数据")
    @DeleteMapping("/data/{id}")
    @PermissionMeta(value = "删除字典数据")
    public DeletedVO deleteData(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        dictService.deleteData(id);
        return new DeletedVO(8150);
    }
}
