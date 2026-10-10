/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bolehunter.service.RedisService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Param eval
 * @Time 2022/8/27 16:15
 */
@Service
public class RedisServiceImpl implements RedisService {
    @Resource
    RedisTemplate redisTemplate;

    /** 设置的时间单位是 分钟 */
    void set(String key,Object value){
        redisTemplate.opsForValue().set(key,value);
    }
    void set(String key,Object value,Integer time){
        redisTemplate.opsForValue().set(key,value,time, TimeUnit.MINUTES);
    }

    Object get(String key){
        return redisTemplate.opsForValue().get(key);
    }

    void del(String key){
        redisTemplate.delete(key);
    }

    @Override  /** 验证码 5分钟过期 */
    public void setMailCode(String code) {
        set("mailCode_"+code,code,5);
    }

    @Override
    public String getMailCode(String code) {
        Object data = get("mailCode_"+code);
        if (data==null){
            return null;
        }
        return String.valueOf(data);
    }

    @Override
    public void delMailCode(String code) {
        // 先判断这个验证码是否被删除了
        if (getMailCode(code).isEmpty()){
            return;
        }
        // 如果没有被删除, 那么执行删除操作
        del("mailCode_"+code);
    }

    @Override
    public void saveChatData(String token, String data) {
        set("chat_"+token,data);
    }

    @Override
    public String getChatData(String token) {
        Object data = get("chat_"+token);
        if (data !=null){
            return String.valueOf(data);
        }
        return "null";
    }
}
