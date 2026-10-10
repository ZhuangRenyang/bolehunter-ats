package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.mapper.CompanyMapper;
import com.bolehunter.model.CompanyDO;
import com.bolehunter.model.HrCompanyDO;
import com.bolehunter.mapper.HrCompanyMapper;
import com.bolehunter.service.HrCompanyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Eval
 * @since 2022-04-05
 */
@Service
public class HrCompanyServiceImpl extends ServiceImpl<HrCompanyMapper, HrCompanyDO> implements HrCompanyService {

    @Resource
    private HrCompanyMapper hrCompanyMapper;

    @Resource
    private CompanyMapper companyMapper;

    @Override
    public CompanyDO getCompany(Integer hrID) {
        // 根据hrID查询hr_company表，是否有对应的记录
        QueryWrapper<HrCompanyDO> wrapper = new QueryWrapper<>();
        wrapper.eq("hr_id", hrID);
        HrCompanyDO hrCompanyDO = hrCompanyMapper.selectOne(wrapper);
        if (hrCompanyDO == null) {
            throw new NotFoundException(60000);
        }
        // 查询公司详细信息
        CompanyDO companyDO = companyMapper.selectById(hrCompanyDO.getCompanyId());
        return companyDO;
    }

    @Override
    public boolean create(Integer hrID, Integer companyID) {
        QueryWrapper<HrCompanyDO> wrapper = new QueryWrapper<>();
        wrapper.eq("hr_id", hrID);
        // 每个hr只能认证一次公司信息
        if(hrCompanyMapper.selectCount(wrapper)>1){
            return false;
        }
        HrCompanyDO hrCompanyDO = new HrCompanyDO();
        hrCompanyDO.setHrId(hrID);
        hrCompanyDO.setCompanyId(companyID);
        return hrCompanyMapper.insert(hrCompanyDO) > 0;
    }

    @Override
    public HrCompanyDO getByHrId(Integer hrID) {
        QueryWrapper<HrCompanyDO> wrapper = new QueryWrapper<>();
        wrapper.eq("hr_id", hrID);
        HrCompanyDO hrCompanyDO = hrCompanyMapper.selectOne(wrapper);
        return hrCompanyDO;
    }
}
