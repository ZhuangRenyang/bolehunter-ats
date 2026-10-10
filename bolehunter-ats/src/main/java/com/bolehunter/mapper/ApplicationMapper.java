package com.bolehunter.mapper;

import com.bolehunter.common.mybatis.Page;
import com.bolehunter.model.ApplicationDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bolehunter.model.result.ApplicationResultDO;
import com.baomidou.mybatisplus.core.metadata.IPage;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Eval
 * @since 2022-04-04
 */
public interface ApplicationMapper extends BaseMapper<ApplicationDO> {

    IPage<ApplicationResultDO> getByHrId(Page<ApplicationResultDO> page, Integer hrID, Integer state);

    boolean updateState(Integer id, Integer state);

    IPage<ApplicationResultDO> sortByGrade(Page<ApplicationResultDO> page, Integer hrID);

    IPage<ApplicationResultDO> getByUserId(Page<ApplicationResultDO> page, Integer userId);

}
