package com.bolehunter.mapper;

import com.bolehunter.model.FavorDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bolehunter.model.PositionDO;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Eval
 * @since 2022-04-02
 */
public interface FavorMapper extends BaseMapper<FavorDO> {
    List<PositionDO> getByUserId(Integer userId);

    FavorDO getByUserIdAndFavorId(Integer userId, Integer positionId);
}
