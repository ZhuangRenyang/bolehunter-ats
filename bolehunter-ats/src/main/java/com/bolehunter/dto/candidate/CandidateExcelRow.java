package com.bolehunter.dto.candidate;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Excel 导入的候选人行数据（表头映射后）
 */
@Data
@NoArgsConstructor
public class CandidateExcelRow {

    /**
     * Excel 行号（1 开始，用于错误定位）
     */
    private int rowIndex;

    private String name;

    private String phone;

    private String email;

    private String currentCompany;

    private String currentPosition;

    /**
     * 原始学历文本（如"本科"，导入时映射为字典键）
     */
    private String educationText;

    private String city;

    private String remark;
}
