package com.bolehunter.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.common.mybatis.Page;
import com.bolehunter.common.util.PageUtil;
import com.bolehunter.mapper.InterviewMapper;
import com.bolehunter.model.InterviewDO;
import com.bolehunter.model.result.ApplicationResultDO;
import com.bolehunter.model.result.InterviewResultDO;
import com.bolehunter.service.InterviewService;
import com.bolehunter.vo.PageResponseVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;


/**
 * @author Eval
 */
@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, InterviewDO> implements InterviewService {

    @Resource
    private InterviewMapper interviewMapper;

    @Override
    public PageResponseVO<InterviewResultDO> getByHrId(Integer count, Integer page, Integer hrID) {
        Page<InterviewResultDO> pager = new Page<>(page, count);
        IPage<InterviewResultDO> paging = interviewMapper.getByHrId(pager, hrID);
        return PageUtil.build(paging);
    }

    @Override
    public PageResponseVO<InterviewResultDO> getByUserId(Integer count, Integer page, Integer userId) {
        Page<InterviewResultDO> pager = new Page<>(page, count);
        IPage<InterviewResultDO> paging = interviewMapper.getByUserId(pager, userId);
        return PageUtil.build(paging);
    }
}
