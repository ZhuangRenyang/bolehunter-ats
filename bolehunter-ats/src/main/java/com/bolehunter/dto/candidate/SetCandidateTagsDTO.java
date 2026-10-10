package com.bolehunter.dto.candidate;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设置候选人标签
 */
@Data
@NoArgsConstructor
public class SetCandidateTagsDTO {

    /**
     * 标签id列表（空列表表示清空标签）
     */
    private List<Integer> tagIds;
}
