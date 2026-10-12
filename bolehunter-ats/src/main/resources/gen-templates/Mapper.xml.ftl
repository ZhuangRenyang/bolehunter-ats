<?xml version="1.0" encoding="UTF-8"?>
<!--
  BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
  Copyright (C) 2026 ${_meta.author}
  GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
  Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
  由 BoleHunter 代码生成器生成于 ${date}，模块：${moduleName}（${businessName}）
  本文件遵循 AGPL-3.0-or-later，请勿移除版权声明。
-->
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${packageName}.mapper.${className}Mapper">

    <resultMap id="BaseResultMap" type="${packageName}.model.${className}DO">
<#list columns as c>
        <result column="${c.columnName}" property="${c.fieldName}"/>
</#list>
    </resultMap>

    <sql id="Base_Column_List">
<#list columns as c>${c.columnName}<#if c_has_next>, </#if></#list>
    </sql>

</mapper>
