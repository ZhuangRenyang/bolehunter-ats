package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.common.mybatis.Page;
import com.bolehunter.dto.communication.CreateCommunicationDTO;
import com.bolehunter.mapper.CommunicationMapper;
import com.bolehunter.mapper.UserMapper;
import com.bolehunter.model.CommunicationDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.CommunicationService;
import com.bolehunter.core.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 沟通记录服务实现
 */
@Service
public class CommunicationServiceImpl extends ServiceImpl<CommunicationMapper, CommunicationDO>
        implements CommunicationService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<CommunicationDO> getByRecommendation(Integer recommendationId, Integer page, Integer count) {
        Page<CommunicationDO> pager = new Page<>(page, count);
        QueryWrapper<CommunicationDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(CommunicationDO::getRecommendationId, recommendationId)
                .orderByDesc(CommunicationDO::getCommunicateTime);
        IPage<CommunicationDO> result = this.page(pager, wrapper);
        List<CommunicationDO> records = result.getRecords();
        if (!records.isEmpty()) {
            List<Integer> userIds = records.stream()
                    .map(CommunicationDO::getCommunicatorId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Integer, String> nameMap = userIds.isEmpty()
                    ? new HashMap<>()
                    : userMapper.selectBatchIds(userIds).stream()
                            .collect(Collectors.toMap(UserDO::getId, UserDO::getNickname));
            records.forEach(item -> item.setCommunicatorName(nameMap.get(item.getCommunicatorId())));
        }
        return result;
    }

    @Override
    public boolean createCommunication(CreateCommunicationDTO validator, Integer communicatorId) {
        CommunicationDO communication = CommunicationDO.builder()
                .recommendationId(validator.getRecommendationId())
                .type(validator.getType() == null ? "phone" : validator.getType())
                .content(validator.getContent())
                .communicatorId(communicatorId)
                .communicateTime(new Date())
                .build();
        return this.save(communication);
    }

    @Override
    public boolean deleteCommunication(Integer id) {
        CommunicationDO communication = this.getById(id);
        if (communication == null) {
            throw new NotFoundException(80270);
        }
        return this.removeById(id);
    }
}
