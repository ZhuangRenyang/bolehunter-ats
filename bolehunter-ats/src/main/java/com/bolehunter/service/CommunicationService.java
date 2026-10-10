package com.bolehunter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.dto.communication.CreateCommunicationDTO;
import com.bolehunter.model.CommunicationDO;

/**
 * 沟通记录服务
 */
public interface CommunicationService extends IService<CommunicationDO> {

    /**
     * 按推荐记录分页查询（时间倒序），附记录人昵称
     */
    IPage<CommunicationDO> getByRecommendation(Integer recommendationId, Integer page, Integer count);

    boolean createCommunication(CreateCommunicationDTO validator, Integer communicatorId);

    boolean deleteCommunication(Integer id);
}
