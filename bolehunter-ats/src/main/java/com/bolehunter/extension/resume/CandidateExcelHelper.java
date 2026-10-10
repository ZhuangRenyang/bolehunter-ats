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
package com.bolehunter.extension.resume;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.dto.candidate.CandidateExcelRow;
import com.bolehunter.mapper.DictDataMapper;
import com.bolehunter.model.DictDataDO;
import com.bolehunter.core.exception.ForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 候选人 Excel 导入/模板助手（POI WorkbookFactory 统一处理 xls/xlsx）。
 * 模板表头：姓名*、手机号、邮箱、当前公司、当前职位、学历、城市、备注。
 */
@Slf4j
@Component
public class CandidateExcelHelper {

    private static final String[] HEADERS =
            {"姓名", "手机号", "邮箱", "当前公司", "当前职位", "学历", "城市", "备注"};

    @Autowired
    private DictDataMapper dictDataMapper;

    /**
     * 读取 Excel 行（首行为表头），返回行数据列表
     */
    public List<CandidateExcelRow> readRows(MultipartFile file) {
        List<CandidateExcelRow> rows = new ArrayList<>();
        try (InputStream in = file.getInputStream(); Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null || sheet.getRow(0) == null) {
                throw new ForbiddenException(80190);
            }
            Map<Integer, String> columnMap = resolveHeaders(sheet.getRow(0));
            DataFormatter formatter = new DataFormatter();
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isBlankRow(row, formatter)) {
                    continue;
                }
                CandidateExcelRow item = new CandidateExcelRow();
                item.setRowIndex(r + 1);
                item.setName(cellText(formatter, row, columnMap, "姓名"));
                item.setPhone(cellText(formatter, row, columnMap, "手机号"));
                item.setEmail(cellText(formatter, row, columnMap, "邮箱"));
                item.setCurrentCompany(cellText(formatter, row, columnMap, "当前公司"));
                item.setCurrentPosition(cellText(formatter, row, columnMap, "当前职位"));
                item.setEducationText(cellText(formatter, row, columnMap, "学历"));
                item.setCity(cellText(formatter, row, columnMap, "城市"));
                item.setRemark(cellText(formatter, row, columnMap, "备注"));
                rows.add(item);
            }
            return rows;
        } catch (ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel 读取失败: {}", file.getOriginalFilename(), e);
            throw new ForbiddenException(80190);
        }
    }

    /**
     * 学历文本转字典键（先精确匹配字典标签，再按关键词兜底）
     */
    public String educationToKey(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String t = text.trim();
        QueryWrapper<DictDataDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(DictDataDO::getDictType, "education").eq(DictDataDO::getStatus, 1);
        List<DictDataDO> dictItems = dictDataMapper.selectList(wrapper);
        for (DictDataDO item : dictItems) {
            if (item.getDictLabel().equals(t)) {
                return item.getDictValue();
            }
        }
        if (t.contains("博士")) {
            return "doctor";
        }
        if (t.contains("硕士") || t.contains("研究生")) {
            return "master";
        }
        if (t.contains("本科") || t.contains("学士")) {
            return "bachelor";
        }
        if (t.contains("大专") || t.contains("专科")) {
            return "associate";
        }
        return "other";
    }

    /**
     * 生成导入模板
     */
    public void writeTemplate(OutputStream out) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("候选人导入模板");
            Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                header.createCell(i).setCellValue(HEADERS[i] + (i == 0 ? "*" : ""));
            }
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("张三");
            sample.createCell(1).setCellValue("13800138000");
            sample.createCell(2).setCellValue("zhangsan@example.com");
            sample.createCell(3).setCellValue("某某科技有限公司");
            sample.createCell(4).setCellValue("Java开发工程师");
            sample.createCell(5).setCellValue("本科");
            sample.createCell(6).setCellValue("广东深圳");
            sample.createCell(7).setCellValue("示例数据，导入前请删除本行");
            wb.write(out);
        } catch (Exception e) {
            log.error("生成导入模板失败", e);
            throw new ForbiddenException(80190);
        }
    }

    /**
     * 表头定位：列索引 -> 标准字段名
     */
    private Map<Integer, String> resolveHeaders(Row headerRow) {
        Map<Integer, String> map = new HashMap<>();
        DataFormatter formatter = new DataFormatter();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            String text = formatter.formatCellValue(headerRow.getCell(c)).trim()
                    .replace("*", "");
            for (String header : HEADERS) {
                if (text.contains(header.replace("*", ""))) {
                    map.put(c, header);
                    break;
                }
            }
        }
        return map;
    }

    private String cellText(DataFormatter formatter, Row row, Map<Integer, String> columnMap, String header) {
        for (Map.Entry<Integer, String> entry : columnMap.entrySet()) {
            if (entry.getValue().equals(header)) {
                String value = formatter.formatCellValue(row.getCell(entry.getKey())).trim();
                return value.isEmpty() ? null : value;
            }
        }
        return null;
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            String value = formatter.formatCellValue(row.getCell(c));
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
