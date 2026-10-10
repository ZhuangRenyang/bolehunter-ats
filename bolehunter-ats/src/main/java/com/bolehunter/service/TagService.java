package com.bolehunter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.model.TagDO;

import java.util.List;

/**
 * 标签服务
 */
public interface TagService extends IService<TagDO> {

    List<TagDO> getAllTags();

    boolean createTag(String name);

    boolean updateTag(TagDO tag, String name);

    boolean deleteTag(Integer id);

    boolean checkNameExist(String name, Integer excludeId);
}
