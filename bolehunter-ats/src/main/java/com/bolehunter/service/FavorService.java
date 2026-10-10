package com.bolehunter.service;

import com.bolehunter.model.FavorDO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.model.PositionDO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Eval
 * @since 2022-04-02
 */
public interface FavorService extends IService<FavorDO> {
    List<PositionDO> getAllByUserId(Integer userId);

    boolean cancelFavor(Integer userId, Integer positionId);

    boolean create(FavorDO favorDO);
}
