package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 标签
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tag")
@EqualsAndHashCode(callSuper = true)
public class TagDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签名（同租户唯一）
     */
    private String name;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;
}
