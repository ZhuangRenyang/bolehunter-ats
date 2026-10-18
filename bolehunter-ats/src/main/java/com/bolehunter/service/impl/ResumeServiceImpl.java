/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bolehunter.dto.resume.CreateOrUpdateResumeDTO;
import com.bolehunter.mapper.*;
import com.bolehunter.model.*;
import com.bolehunter.service.EducationService;
import com.bolehunter.service.ResumeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.service.UserService;
import com.bolehunter.vo.ResumeVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ZhuangRenyang
 * @since 2022-03-14
 */
@Service
public class ResumeServiceImpl extends ServiceImpl<ResumeMapper, ResumeDO> implements ResumeService {

    @Resource
    private ResumeMapper resumeMapper;

    @Resource
    private UserService userService;

    @Resource
    private EducationMapper educationMapper;

    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private ExperienceMapper experienceMapper;

    //根据用户Id获取用户简历信息
    @Override
    public ResumeVO getByUserId(Integer userId) {
        LambdaQueryWrapper<ResumeDO> queryWrapper = new LambdaQueryWrapper<ResumeDO>();
        queryWrapper.eq(ResumeDO::getUserId,userId);
        queryWrapper.isNull(ResumeDO::getDeleteTime);

        ResumeDO resume = resumeMapper.selectOne(queryWrapper);
        ResumeVO res=new ResumeVO();
        UserDO user=userService.getById(userId);
        if(resume!=null){
            QueryWrapper<EducationDO> educationDOQueryWrapper= new QueryWrapper<>();
            educationDOQueryWrapper.lambda().eq(EducationDO::getResumeId,resume.getId());
            List<EducationDO> educationDOS = educationMapper.selectList(educationDOQueryWrapper);
            QueryWrapper<ProjectDO> projectDOQueryWrapper=new QueryWrapper<>();
            projectDOQueryWrapper.lambda().eq(ProjectDO::getResumeId,resume.getId());
            List<ProjectDO> projectDOS = projectMapper.selectList(projectDOQueryWrapper);

            QueryWrapper<ExperienceDO> experienceDOQueryWrapper=new QueryWrapper<>();
            experienceDOQueryWrapper.lambda().eq(ExperienceDO::getResumeId,resume.getId());
            List<ExperienceDO> experienceDOS = experienceMapper.selectList(experienceDOQueryWrapper);
            if(resume.getId()!=null){
                res.setResumeId(resume.getId());
            }
            if(resume.getAbility()!=null){
                res.setAbility(resume.getAbility());
            }
            if(resume.getEnglish()!=null){
                res.setEnglish(resume.getEnglish());
            }
            if(resume.getHome()!=null){
                res.setHome(resume.getHome());
            }
            if(resume.getJobIntention()!=null){
                res.setJobIntention(resume.getJobIntention());
            }
            if(resume.getAbility()!=null){
                res.setAbility(resume.getAbility());
            }
            if(resume.getPersonalSummary()!=null){
                res.setPersonalSummary(resume.getPersonalSummary());
            }
            if(user.getEmail()!=null){
                res.setEmail(user.getEmail());
            }
            if(user.getEmail()!=null){
                res.setEmail(user.getEmail());
            }
            if(user.getAvatar()!=null){
                res.setAvatar(user.getAvatar());
            }
            if(user.getNickname()!=null){
                res.setName(user.getNickname());
            }
            if(user.getTel()!=null){
                res.setTel(user.getTel());
            }
            if(resume.getAge()!=null){
                res.setAge(resume.getAge());
            }
            if(resume.getSex()!=null){
                res.setSex(resume.getSex());
            }
            res.setEducations(educationDOS);
            res.setProjects(projectDOS);
            res.setExperiences(experienceDOS);
        }

        return res;
    }

    @Override
    public boolean createResume(CreateOrUpdateResumeDTO validator) {
        ResumeDO resumeDO = new ResumeDO();
        resumeDO.setAbility(validator.getAbility());
        resumeDO.setPersonalSummary(validator.getPersonalSummary());
        resumeDO.setUserId(validator.getUserId());
        resumeDO.setEnglish(validator.getEnglish());
        resumeDO.setJobIntention(validator.getJobIntention());
        return resumeMapper.insert(resumeDO) > 0;
    }

    @Override
    public boolean updateResume(ResumeDO resumeDO, CreateOrUpdateResumeDTO validator) {
        resumeDO.setAbility(validator.getAbility());
        resumeDO.setPersonalSummary(validator.getPersonalSummary());
        resumeDO.setUserId(validator.getUserId());
        resumeDO.setEnglish(validator.getEnglish());
        resumeDO.setJobIntention(validator.getJobIntention());
        resumeDO.setAge(validator.getAge());
        resumeDO.setSex(validator.getSex());
        resumeDO.setHome(validator.getHome());
        return resumeMapper.updateById(resumeDO) > 0;
    }

    @Override
    public ResumeDO getSimpleByUserId(Integer userId) {
        LambdaQueryWrapper<ResumeDO> queryWrapper = new LambdaQueryWrapper<ResumeDO>();
        queryWrapper.eq(ResumeDO::getUserId,userId);
        queryWrapper.isNull(ResumeDO::getDeleteTime);

        ResumeDO resume = resumeMapper.selectOne(queryWrapper);
        return resume;
    }


}
