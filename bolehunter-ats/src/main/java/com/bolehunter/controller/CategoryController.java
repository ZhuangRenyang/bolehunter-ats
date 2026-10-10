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
package com.bolehunter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.core.annotation.GroupRequired;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import com.bolehunter.common.mybatis.Page;
import com.bolehunter.common.util.PageUtil;
import com.bolehunter.dto.category.CreateOrUpdateCategoryDTO;
import com.bolehunter.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.bolehunter.model.CategoryDO;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import com.bolehunter.vo.PageResponseVO;
import com.bolehunter.vo.UpdatedVO;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import java.util.List;


/**
* @author Eval
* @since 2022-04-04
*/
@RestController
@RequestMapping("/recruit/category")
@PermissionModule(value = "职位分类")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增职位分类
     * @param validator
     * @return
     */
    @Logger(template = "新增职位类型")
    @PostMapping("")
    @GroupRequired
    @PermissionMeta(value = "新增分类")
    public CreatedVO create(@RequestBody @Validated CreateOrUpdateCategoryDTO validator) {
        categoryService.createCategory(validator);
        return new CreatedVO(5000);
    }

    /**
     * 更新职位分类信息
     * @param id
     * @return
     */
    @Logger(template = "更新职位类型")
    @PutMapping("/{id}")
    @GroupRequired
    @PermissionMeta(value = "更新分类")
    public UpdatedVO update(@PathVariable @Positive(message = "{id.positive}") Integer id, @RequestBody @Validated CreateOrUpdateCategoryDTO validator) {
        CategoryDO categoryDO = categoryService.getById(id);
        if (categoryDO == null) {
            throw new NotFoundException(50000);
        }
        categoryService.updateCategory(categoryDO, validator);
        return new UpdatedVO(5100);
    }

    /**
     * 根据id删除职位分类
     * @param id
     * @return
     */
    @Logger(template = "删除职位类型")
    @DeleteMapping("/{id}")
    @GroupRequired
    @PermissionMeta(value = "删除分类")
    public DeletedVO delete(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        CategoryDO categoryDO = categoryService.getById(id);
        if (categoryDO == null) {
            throw new NotFoundException(50000);
        }
        categoryService.deleteById(categoryDO.getId());
        return new DeletedVO(5200);
    }

    /**
     * 根据id查询职位分类信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public CategoryDO get(@PathVariable(value = "id") @Positive(message = "{id.positive}") Integer id) {
        CategoryDO categoryDO = categoryService.getById(id);
        if (categoryDO == null) {
            throw new NotFoundException(50000);
        }
        return categoryDO;
    }

    /**
     * 查询所有职位分类
     * @return
     */
    @GetMapping("")
    public List<CategoryDO> getAll() {
        List<CategoryDO> categoryDOS = categoryService.getAll();
        if (categoryDOS == null || categoryDOS.size() == 0) {
            throw new NotFoundException(51000);
        }
        return categoryDOS;
    }

    /**
     * 分页查询所有职位分类列表
     * @param count
     * @param page
     * @return
     */
    @Logger(template = "查看职位类型列表")
    @GetMapping("/page")
    @GroupRequired
    @PermissionMeta(value = "查看分类列表")
    public PageResponseVO<CategoryDO> page(
            @RequestParam(name = "count", required = false, defaultValue = "10")
            @Min(value = 1, message = "{page.count.min}")
            @Max(value = 30, message = "{page.count.max}") Integer count,
            @RequestParam(name = "page", required = false, defaultValue = "0")
            @Min(value = 0, message = "{page.number.min}") Integer page
    ) {
        Page<CategoryDO> pager = new Page<>( page,count);
        IPage<CategoryDO> paging = categoryService.getBaseMapper().selectPage(pager, null);
        return PageUtil.build(paging);
    }

    /**
     * 查询部分职位分类用于首页展示
     * @return
     */
    @GetMapping("/show")
    public List<CategoryDO> show() {
        List<CategoryDO> categoryDOS = categoryService.show();
        if (categoryDOS == null || categoryDOS.size() == 0) {
            throw new NotFoundException(51000);
        }
        return categoryDOS;
    }
}
