package com.bolehunter.service;

import com.bolehunter.dto.category.CreateOrUpdateCategoryDTO;
import com.bolehunter.model.CategoryDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Eval
 * @since 2022-04-04
 */
public interface CategoryService extends IService<CategoryDO> {

    boolean createCategory(CreateOrUpdateCategoryDTO validator);

    boolean updateCategory(CategoryDO categoryDO, CreateOrUpdateCategoryDTO validator);

    boolean deleteById(Integer id);

    List<CategoryDO> getAll();

    List<CategoryDO> show();
}
