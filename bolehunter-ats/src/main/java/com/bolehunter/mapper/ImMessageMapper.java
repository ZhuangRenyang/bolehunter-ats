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
package com.bolehunter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bolehunter.model.ImMessageDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 内部即时消息 Mapper
 * <p>自定义查询手写 SQL；注意自定义语句不会自动追加逻辑删除条件，故显式带 {@code delete_time IS NULL}。</p>
 */
public interface ImMessageMapper extends BaseMapper<ImMessageDO> {

    /**
     * 单聊历史：取两人之间双向往来的最近 N 条（先按 id 倒序截断再正序返回，便于前端按时间渲染）
     */
    @Select("SELECT t.* FROM (SELECT * FROM im_message WHERE delete_time IS NULL AND "
        + "((sender_id = #{userA} AND receiver_id = #{userB}) OR (sender_id = #{userB} AND receiver_id = #{userA})) "
        + "ORDER BY id DESC LIMIT #{limit}) t ORDER BY t.id ASC")
    List<ImMessageDO> selectConversation(@Param("userA") Integer userA,
                                         @Param("userB") Integer userB,
                                         @Param("limit") Integer limit);

    /**
     * 会话列表：按"对方"分组，取每个会话的最后一条消息
     */
    @Select("SELECT m.* FROM im_message m INNER JOIN ("
        + "SELECT MAX(id) AS max_id FROM im_message WHERE delete_time IS NULL "
        + "AND (sender_id = #{userId} OR receiver_id = #{userId}) "
        + "GROUP BY CASE WHEN sender_id = #{userId} THEN receiver_id ELSE sender_id END"
        + ") latest ON m.id = latest.max_id ORDER BY m.id DESC LIMIT #{limit}")
    List<ImMessageDO> selectLastMessageEach(@Param("userId") Integer userId,
                                            @Param("limit") Integer limit);
}
