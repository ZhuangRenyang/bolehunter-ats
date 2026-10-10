package com.bolehunter.service;

import com.bolehunter.dto.company.CreateOrUpdateCompanyDTO;
import com.bolehunter.model.CompanyDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Eval
 * @since 2022-04-01
 */
public interface CompanyService extends IService<CompanyDO> {

    boolean updateCompany(CompanyDO company, CreateOrUpdateCompanyDTO validator);

    boolean updateState(Integer id, Integer state);

    boolean createCompany(CreateOrUpdateCompanyDTO validator);

    CompanyDO getByName(String name);

    List<CompanyDO> findHotCompany();
}
