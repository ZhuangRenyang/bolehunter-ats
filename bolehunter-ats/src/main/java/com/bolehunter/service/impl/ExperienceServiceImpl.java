package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.mapper.ExperienceMapper;
import com.bolehunter.mapper.ProjectMapper;
import com.bolehunter.model.ExperienceDO;
import com.bolehunter.model.ProjectDO;
import com.bolehunter.service.ExperienceService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Eval
 * @since 2022-04-01
 */
@Service
public class ExperienceServiceImpl extends ServiceImpl<ExperienceMapper, ExperienceDO> implements ExperienceService {

    @Resource
    private ExperienceMapper experienceMapper;

    @Override
    public List<ExperienceDO> getByResumeId(Integer rid) {
        QueryWrapper<ExperienceDO> experienceDOQueryWrapper=new QueryWrapper<>();
        experienceDOQueryWrapper.lambda().eq(ExperienceDO::getResumeId,rid);
        List<ExperienceDO>  experienceDOS = experienceMapper.selectList(experienceDOQueryWrapper);
        return experienceDOS;
    }
}
