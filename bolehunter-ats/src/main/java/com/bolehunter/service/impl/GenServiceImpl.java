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

import com.bolehunter.service.GenService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成服务实现
 * <p>流程：information_schema 元数据 → 字段映射（Java 类型/驼峰命名）→ FreeMarker 渲染 → zip 打包。</p>
 */
@Service
public class GenServiceImpl implements GenService {

    private static final Pattern NAME_SAFE = Pattern.compile("^[a-z][a-z0-9_]*$");
    private static final Set<String> EXCLUDED_TABLES = Set.of(
            "permission", "group", "group_permission", "user_group", "user_identity", "log",
            "sys_dept", "sys_dict_type", "sys_dict_data", "feature_flag", "custom_field",
            "gen_table", "gen_table_column", "qrtz_%");

    private final JdbcTemplate jdbcTemplate;
    private Configuration freemarker;

    public GenServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initFreemarker() {
        freemarker = new Configuration(Configuration.VERSION_2_3_32);
        freemarker.setClassForTemplateLoading(GenServiceImpl.class, "/gen-templates");
        freemarker.setDefaultEncoding("UTF-8");
    }

    @Override
    public List<Map<String, Object>> listTables() {
        String sql = "SELECT table_name, table_comment FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE' ORDER BY table_name";
        List<Map<String, Object>> all = jdbcTemplate.queryForList(sql);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : all) {
            String name = String.valueOf(row.get("table_name"));
            if (isExcluded(name)) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("tableName", name);
            item.put("tableComment", row.get("table_comment") == null ? "" : String.valueOf(row.get("table_comment")));
            result.add(item);
        }
        return result;
    }

    private boolean isExcluded(String tableName) {
        for (String ex : EXCLUDED_TABLES) {
            if (ex.endsWith("%") ? tableName.startsWith(ex.substring(0, ex.length() - 1)) : ex.equalsIgnoreCase(tableName)) {
                return true;
            }
        }
        return tableName.startsWith("sys_log");
    }

    @Override
    public Map<String, Object> preview(String tableName) {
        return buildModel(tableName, "", "", "");
    }

    @Override
    @SuppressWarnings("unchecked")
    public byte[] generate(Map<String, Object> params) throws Exception {
        String tableName = str(params.get("table_name"));
        String moduleName = str(params.get("module_name"));
        String className = str(params.get("class_name"));
        String businessName = str(params.get("business_name"));
        String author = str(params.get("author"));

        if (tableName.isEmpty() || moduleName.isEmpty() || className.isEmpty()) {
            throw new IllegalArgumentException("tableName / moduleName / className 不能为空");
        }
        if (!NAME_SAFE.matcher(moduleName).matches()) {
            throw new IllegalArgumentException("moduleName 仅允许小写字母、数字与下划线，且以字母开头");
        }
        Map<String, Object> model = buildModel(tableName, moduleName, className, businessName);
        ((Map<String, Object>) model.get("_meta")).put("author", author.isEmpty() ? "ZhuangRenyang" : author);

        String[] templates = {"DO", "Mapper.java", "Mapper.xml", "Service", "ServiceImpl", "Controller", "vue-list"};
        Map<String, String> outputs = new LinkedHashMap<>();
        for (String t : templates) {
            Template template = freemarker.getTemplate(t + ".ftl");
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            outputs.put(t, writer.toString());
        }
        return zip(outputs, moduleName, className, (Map<String, Object>) model.get("_meta"));
    }

    private byte[] zip(Map<String, String> outputs, String moduleName, String className, Map<String, Object> meta) throws Exception {
        String pkgPath = "com/bolehunter";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(bos)) {
            put(zos, moduleName + "/java/" + pkgPath + "/model/" + className + "DO.java", outputs.get("DO"));
            put(zos, moduleName + "/java/" + pkgPath + "/mapper/" + className + "Mapper.java", outputs.get("Mapper.java"));
            put(zos, moduleName + "/java/" + pkgPath + "/service/" + className + "Service.java", outputs.get("Service"));
            put(zos, moduleName + "/java/" + pkgPath + "/service/impl/" + className + "ServiceImpl.java", outputs.get("ServiceImpl"));
            put(zos, moduleName + "/java/" + pkgPath + "/controller/" + className + "Controller.java", outputs.get("Controller"));
            put(zos, moduleName + "/resources/mapper/" + className + "Mapper.xml", outputs.get("Mapper.xml"));
            put(zos, moduleName + "/web/src/view/" + moduleName + "/" + className + "List.vue", outputs.get("vue-list"));
            put(zos, moduleName + "/README.md", genReadme(meta));
        }
        return bos.toByteArray();
    }

    private void put(ZipOutputStream zos, String path, String content) throws Exception {
        zos.putNextEntry(new ZipEntry(path));
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private String genReadme(Map<String, Object> meta) {
        return "BoleHunter 生成代码说明\n"
                + "====================\n\n"
                + "模块：" + meta.get("moduleName") + "（" + meta.get("businessName") + "）\n"
                + "生成时间：" + meta.get("date") + "\n\n"
                + "接入步骤：\n"
                + "1. java/ 下各文件按包路径拷入 bolehunter-ats/src/main/java\n"
                + "2. resources/mapper/*.xml 拷入 bolehunter-ats/src/main/resources/mapper\n"
                + "3. web/src/view/ 下页面拷入 bolehunter-ats-web/src/view\n"
                + "4. 在 bolehunter-ats-web/src/router/index.js 注册路由\n"
                + "5. 按需在 Controller 上补充筛选条件与 @PermissionMeta 权限名\n\n"
                + "生成的代码遵循 AGPL-3.0-or-later，版权头已内置，请勿移除。\n";
    }

    /**
     * 构建模板数据模型：读列元数据并做 Java 类型/驼峰映射
     */
    private Map<String, Object> buildModel(String tableName, String moduleName, String className, String businessName) {
        String sql = "SELECT column_name, data_type, column_type, column_comment, is_nullable, column_key "
                + "FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? "
                + "ORDER BY ordinal_position";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tableName);
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("表不存在：" + tableName);
        }

        List<Map<String, Object>> columns = new ArrayList<>();
        boolean hasCreateTime = false;
        boolean hasUpdateTime = false;
        boolean hasDeleteTime = false;
        boolean hasId = false;
        String pkColumnName = "id";

        for (Map<String, Object> row : rows) {
            String col = String.valueOf(row.get("column_name"));
            String dataType = String.valueOf(row.get("data_type"));
            String comment = row.get("column_comment") == null ? "" : String.valueOf(row.get("column_comment"));

            if ("id".equalsIgnoreCase(col)) {
                hasId = true;
                pkColumnName = col;
            }
            if ("create_time".equals(col)) hasCreateTime = true;
            if ("update_time".equals(col)) hasUpdateTime = true;
            if ("delete_time".equals(col)) hasDeleteTime = true;

            Map<String, Object> colMap = new LinkedHashMap<>();
            colMap.put("columnName", col);
            colMap.put("fieldName", toCamel(col));
            colMap.put("javaType", toJavaType(dataType, String.valueOf(row.get("column_type"))));
            colMap.put("jdbcType", toJdbcType(dataType));
            colMap.put("comment", comment.isEmpty() ? toCamel(col) : comment);
            colMap.put("isPk", "PRI".equalsIgnoreCase(String.valueOf(row.get("column_key"))));
            colMap.put("isRequired", "NO".equalsIgnoreCase(String.valueOf(row.get("is_nullable"))) && !"PRI".equalsIgnoreCase(String.valueOf(row.get("column_key"))));
            colMap.put("isVarchar", "varchar".equalsIgnoreCase(dataType) || "char".equalsIgnoreCase(dataType) || "text".equalsIgnoreCase(dataType));
            colMap.put("isDate", "datetime".equalsIgnoreCase(dataType) || "date".equalsIgnoreCase(dataType) || "timestamp".equalsIgnoreCase(dataType));
            columns.add(colMap);
        }

        if (!hasId) {
            throw new IllegalArgumentException("表 " + tableName + " 缺少自增主键 id，暂不支持生成");
        }
        boolean hasBaseColumns = hasCreateTime && hasUpdateTime && hasDeleteTime;

        // 供查询条件/表单展示的列（排除主键与基类三列）
        List<Map<String, Object>> bizColumns = new ArrayList<>();
        for (Map<String, Object> col : columns) {
            boolean baseCol = Boolean.TRUE.equals(col.get("isPk")) || "create_time".equals(col.get("columnName"))
                    || "update_time".equals(col.get("columnName")) || "delete_time".equals(col.get("columnName"));
            if (!baseCol) {
                bizColumns.add(col);
            }
        }

        String finalClassName = className.isEmpty() ? toPascal(tableName) : className;
        String finalModuleName = moduleName.isEmpty() ? tableName : moduleName;

        Map<String, Object> model = new HashMap<>();
        model.put("packageName", "com.bolehunter");
        model.put("moduleName", finalModuleName);
        model.put("tableName", tableName);
        model.put("className", finalClassName);
        model.put("classNameCamel", toCamel(finalClassName));
        model.put("businessName", businessName.isEmpty() ? finalClassName : businessName);
        model.put("date", LocalDate.now().toString());
        model.put("hasBaseColumns", hasBaseColumns);
        model.put("columns", columns);
        model.put("bizColumns", bizColumns);
        model.put("pkFieldName", toCamel(pkColumnName));
        model.put("pkJavaType", toJavaType("bigint", "bigint"));

        Map<String, Object> meta = new HashMap<>();
        meta.put("moduleName", finalModuleName);
        meta.put("businessName", businessName.isEmpty() ? finalClassName : businessName);
        meta.put("date", LocalDate.now().toString());
        model.put("_meta", meta);
        return model;
    }

    /** 下划线 → 小驼峰 */
    private String toCamel(String name) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : name.toLowerCase().toCharArray()) {
            if (c == '_') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return sb.toString();
    }

    /** 下划线 → 大驼峰 */
    private String toPascal(String name) {
        String camel = toCamel(name);
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    private String toJavaType(String dataType, String columnType) {
        switch (dataType.toLowerCase()) {
            case "bigint":
                return "Long";
            case "int":
            case "integer":
            case "tinyint":
            case "smallint":
                return "Integer";
            case "decimal":
            case "numeric":
                return "java.math.BigDecimal";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "datetime":
            case "timestamp":
                return "Date";
            case "date":
                return "java.time.LocalDate";
            default:
                if (columnType != null && columnType.toLowerCase().startsWith("tinyint(1)")) {
                    return "Integer";
                }
                return "String";
        }
    }

    private String toJdbcType(String dataType) {
        switch (dataType.toLowerCase()) {
            case "bigint":
                return "BIGINT";
            case "int":
            case "integer":
            case "tinyint":
            case "smallint":
                return "INTEGER";
            case "datetime":
            case "timestamp":
                return "TIMESTAMP";
            case "date":
                return "DATE";
            case "decimal":
            case "numeric":
                return "DECIMAL";
            default:
                return "VARCHAR";
        }
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }
}
