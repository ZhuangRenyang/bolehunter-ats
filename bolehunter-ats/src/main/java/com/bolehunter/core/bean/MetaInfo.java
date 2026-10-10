package com.bolehunter.core.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限元信息（模块 + 权限名）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaInfo {

    private String module;

    private String permission;
}
