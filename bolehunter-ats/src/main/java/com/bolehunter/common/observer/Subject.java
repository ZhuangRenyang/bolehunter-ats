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
package com.bolehunter.common.observer;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhuangRenyang
 * @create 2021/4/10 23:26
 *
 * 被观察者抽象类
 */
@Data
public abstract class Subject {

    /**
     * 标识发布职位还是下架职位（0代表下架职位，1代表发布新职位）
     */
    public Integer state;

    /**
     * 职位名
     */
    public String positionName;

    /**
     * 观察者列表
     */
    public List<Observer> observers = new ArrayList<>();

    /**
     * 添加观察者（求职者关注企业时，企业需要添加求职者到观察者列表中）
     * @param o
     */
    public void addObserver(Observer o) {
        observers.add(o);
    }

    /**
     * 移除观察者（求职者取消关注企业时，企业需要将该求职者从观察者列表中移除）
     * @param o
     */
    public void removeObservers(Observer o) {
        observers.remove(o);
    }

    /**
     * 通知所有观察者（子类自己实现）
     */
    public abstract void notifyObservers();
}
