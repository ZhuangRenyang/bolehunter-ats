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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.mapper.TagMapper;
import com.bolehunter.model.TagDO;
import com.bolehunter.service.TagService;
import com.bolehunter.core.exception.ForbiddenException;
import com.bolehunter.core.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 标签服务实现
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, TagDO> implements TagService {

    @Override
    public List<TagDO> getAllTags() {
        QueryWrapper<TagDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().orderByAsc(TagDO::getId);
        return this.list(wrapper);
    }

    @Override
    public boolean createTag(String name) {
        if (this.checkNameExist(name, null)) {
            throw new ForbiddenException(80220);
        }
        TagDO tag = TagDO.builder().name(name).build();
        return this.save(tag);
    }

    @Override
    public boolean updateTag(TagDO tag, String name) {
        if (!tag.getName().equals(name) && this.checkNameExist(name, tag.getId())) {
            throw new ForbiddenException(80220);
        }
        tag.setName(name);
        return this.updateById(tag);
    }

    @Override
    public boolean deleteTag(Integer id) {
        TagDO tag = this.getById(id);
        if (tag == null) {
            throw new NotFoundException(80210);
        }
        return this.removeById(id);
    }

    @Override
    public boolean checkNameExist(String name, Integer excludeId) {
        QueryWrapper<TagDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(TagDO::getName, name);
        if (excludeId != null) {
            wrapper.lambda().ne(TagDO::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }
}
