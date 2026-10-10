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

import com.bolehunter.dto.category.CreateOrUpdateCategoryDTO;
import com.bolehunter.model.CategoryDO;
import com.bolehunter.mapper.CategoryMapper;
import com.bolehunter.service.CategoryService;
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
 * @since 2022-04-04
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryDO> implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public boolean createCategory(CreateOrUpdateCategoryDTO validator) {
        CategoryDO  categoryDO = new CategoryDO();
        categoryDO.setName(validator.getName());
        categoryDO.setDescription(validator.getDescription());
        return categoryMapper.insert(categoryDO) > 0;
    }

    @Override
    public boolean updateCategory(CategoryDO categoryDO, CreateOrUpdateCategoryDTO validator) {
        categoryDO.setName(validator.getName());
        categoryDO.setDescription(validator.getDescription());
        return categoryMapper.updateById(categoryDO) > 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        return categoryMapper.deleteById(id) > 0;
    }

    @Override
    public List<CategoryDO> getAll() {
        return categoryMapper.selectList(null);
    }

    @Override
    public List<CategoryDO> show() {
        return categoryMapper.show();
    }
}
