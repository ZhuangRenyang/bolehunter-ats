/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 字典数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_dict_data")
@EqualsAndHashCode(callSuper = true)
public class DictDataDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所属字典类型键
     */
    private String dictType;

    /**
     * 显示标签
     */
    private String dictLabel;

    /**
     * 存储值
     */
    private String dictValue;

    /**
     * 显示顺序
     */
    private Integer dictSort;

    /**
     * 状态: 1-正常 0-停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;
}
