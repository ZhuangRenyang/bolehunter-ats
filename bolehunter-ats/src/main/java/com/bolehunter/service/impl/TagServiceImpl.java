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
