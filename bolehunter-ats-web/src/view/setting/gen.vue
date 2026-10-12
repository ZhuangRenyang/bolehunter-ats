<!--
  BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
  Copyright (C) 2026 ZhuangRenyang
  GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
  Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
  本文件遵循 AGPL-3.0-or-later，请勿移除版权声明。
-->
<template>
  <div class="page">
    <div class="page-title">代码生成</div>

    <el-card shadow="never" style="margin-bottom: 14px">
      <el-form label-width="110px" style="max-width: 640px">
        <el-form-item label="选择数据表" required>
          <el-select v-model="form.tableName" filterable placeholder="选择或搜索表" style="width: 100%" @change="loadPreview">
            <el-option v-for="t in tables" :key="t.tableName" :value="t.tableName"
              :label="t.tableName + (t.tableComment ? '（' + t.tableComment + '）' : '')" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块名" required>
          <el-input v-model="form.moduleName" placeholder="如 candidate（URL 与包内子路径）" maxlength="40" />
        </el-form-item>
        <el-form-item label="类名" required>
          <el-input v-model="form.className" placeholder="如 Candidate（大驼峰，生成 DO/Service/Controller）" maxlength="40" />
        </el-form-item>
        <el-form-item label="功能中文名">
          <el-input v-model="form.businessName" placeholder="如 候选人（用于注释与页面标题）" maxlength="30" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="form.author" placeholder="默认 ZhuangRenyang" maxlength="30" />
        </el-form-item>
        <el-form-item label="软删除模式">
          <el-switch v-model="form.inheritBase" :disabled="!preview.tableName" />
          <span class="gen-tip">继承 BaseModel（含 create/update/delete_time 与逻辑删除）；表缺这三列时强制关闭</span>
        </el-form-item>
        <el-form-item>
          <el-button type="success" icon="Code" :loading="previewingCode" :disabled="!form.tableName || !form.moduleName || !form.className"
            @click="handlePreviewCode">预览生成代码</el-button>
          <el-button type="primary" icon="Download" :loading="downloading" :disabled="!form.tableName || !form.moduleName || !form.className"
            @click="handleDownload">生成并下载 zip</el-button>
          <span class="gen-tip">生成 DO / Mapper / Service / Controller / Vue 列表页，自动携带 AGPL 版权头</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-bottom: 14px" v-if="codeFiles.length">
      <template #header>生成代码预览（共 {{ codeFiles.length }} 个文件）</template>
      <el-tabs v-model="activeFile" type="border-card">
        <el-tab-pane v-for="f in codeFiles" :key="f.name" :name="f.name" :label="f.name.split('/').pop()">
          <pre class="code-view">{{ f.content }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-card shadow="never">
      <template #header>字段预览{{ preview.tableName ? '：' + preview.tableName : '' }}</template>
      <el-table :data="previewColumns" border size="small" v-loading="previewing">
        <el-table-column prop="columnName" label="列名" width="180" />
        <el-table-column prop="fieldName" label="字段名（驼峰）" width="180" />
        <el-table-column prop="javaType" label="Java 类型" width="160" />
        <el-table-column prop="jdbcType" label="JDBC 类型" width="110" />
        <el-table-column prop="comment" label="注释" min-width="150" show-overflow-tooltip />
        <el-table-column label="主键" width="70" align="center">
          <template #default="scope">{{ scope.row.isPk ? '✓' : '' }}</template>
        </el-table-column>
        <el-table-column label="必填" width="70" align="center">
          <template #default="scope">{{ scope.row.isRequired ? '✓' : '' }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!preview.tableName && !previewing" description="选择数据表后预览字段映射" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getGenTables, previewGenTable, downloadGen, previewCodeGen } from '@/api/setting'

const tables = ref([])
const preview = reactive({ tableName: '' })
const previewing = ref(false)
const downloading = ref(false)
const previewingCode = ref(false)
const codeFiles = ref([])
const activeFile = ref('')
const form = reactive({
  tableName: '',
  moduleName: '',
  className: '',
  businessName: '',
  author: '',
  inheritBase: false,
})


async function loadTables() {
  try {
    tables.value = await getGenTables()
  } catch (e) {
    tables.value = []
  }
}

async function loadPreview(tableName) {
  if (!tableName) return
  previewing.value = true
  try {
    const res = await previewGenTable(tableName)
    preview.tableName = tableName
    previewColumns.value = res.columns || []
    form.inheritBase = !!res.hasBaseColumns
    // 自动推断模块名与类名
    if (!form.moduleName) form.moduleName = tableName.replace(/_/g, '')
    if (!form.className) {
      const camel = tableName.split('_').filter(Boolean)
        .map(p => p.charAt(0).toUpperCase() + p.slice(1)).join('')
      form.className = camel
    }
    if (!form.businessName && tables.value.find(t => t.tableName === tableName)?.tableComment) {
      form.businessName = tables.value.find(t => t.tableName === tableName).tableComment
    }
  } catch (e) {
    preview.tableName = ''
    previewColumns.value = []
  } finally {
    previewing.value = false
  }
}

function genPayload() {
  return {
    table_name: form.tableName,
    module_name: form.moduleName,
    class_name: form.className,
    business_name: form.businessName,
    author: form.author || 'ZhuangRenyang',
    inherit_base: form.inheritBase,
  }
}

async function handlePreviewCode() {
  previewingCode.value = true
  try {
    const res = await previewCodeGen(genPayload())
    codeFiles.value = Object.entries(res || {}).map(([name, content]) => ({ name, content }))
    activeFile.value = codeFiles.value.length ? codeFiles.value[0].name : ''
  } catch (e) {
    console.log(e)
  } finally {
    previewingCode.value = false
  }
}

async function handleDownload() {
  downloading.value = true
  try {
    const blob = await downloadGen(genPayload())
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.download = `bolehunter-gen-${form.moduleName}.zip`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('生成成功，zip 已下载')
  } catch (e) {
    console.log(e)
  } finally {
    downloading.value = false
  }
}

onMounted(loadTables)
</script>

<style scoped>
.page-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}
.code-view {
  max-height: 420px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
  white-space: pre;
}
.gen-tip {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}
</style>
