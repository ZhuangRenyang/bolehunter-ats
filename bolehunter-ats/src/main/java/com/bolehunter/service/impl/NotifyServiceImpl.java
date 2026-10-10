package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.model.NotifyDO;
import com.bolehunter.mapper.NotifyMapper;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.NotifyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Eval
 * @since 2022-04-16
 */
@Service
public class NotifyServiceImpl extends ServiceImpl<NotifyMapper, NotifyDO> implements NotifyService {

    @Resource
    private NotifyMapper notifyMapper;

    @Resource
    private UserService userService;

    @Override
    public boolean create(NotifyDO notifyDO) {
        return notifyMapper.insert(notifyDO) > 0;
    }

    @Override
    public List<NotifyDO> get(String userName, String isRead) {
        UserDO userDO = userService.getUserByNickname(userName);
        QueryWrapper<NotifyDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", userDO.getUsername()).eq("is_read", isRead);
        return notifyMapper.selectList(wrapper);
    }

    @Override
    public List<NotifyDO> getAll(String userName) {
        UserDO userDO = userService.getUserByNickname(userName);
        QueryWrapper<NotifyDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_name", userDO.getUsername());
        return notifyMapper.selectList(wrapper);
    }

    @Override
    public boolean update(String userName) {
        return notifyMapper.update(userName);
    }
}
