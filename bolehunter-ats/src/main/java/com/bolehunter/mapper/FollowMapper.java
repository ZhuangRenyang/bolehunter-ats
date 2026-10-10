package com.bolehunter.mapper;

import com.bolehunter.model.CompanyDO;
import com.bolehunter.model.FollowDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Eval
 * @since 2022-03-31
 */
public interface FollowMapper extends BaseMapper<FollowDO> {

    List<CompanyDO> getByUserId(Integer userId);

    FollowDO getByUserIdAndCompanyId(Integer userId, Integer companyId);
}
