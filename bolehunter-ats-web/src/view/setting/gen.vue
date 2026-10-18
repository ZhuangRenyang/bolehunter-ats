<!--
  BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
  Copyright (C) 2026 ZhuangRenyang
  GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
  Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
  本文件遵循 AGPL-3.0-or-later，请勿移除版权声明。
-->
<template>
  <div class="page">
    <div class="page-header">
      <h2>{{ t('gen.title') }}</h2>
    </div>

    <el-card shadow="never" style="margin-bottom: 14px">
      <el-form label-width="110px" style="max-width: 640px">
        <el-form-item :label="t('gen.selectTable')" required>
          <el-select v-model="form.tableName" filterable :placeholder="t('gen.selectTablePlaceholder')" style="width: 100%" @change="loadPreview">
            <el-option v-for="tb in tables" :key="tb.tableName" :value="tb.tableName"
              :label="tb.tableName + (tb.tableComment ? '（' + tb.tableComment + '）' : '')" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('gen.moduleName')" required>
          <el-input v-model="form.moduleName" :placeholder="t('gen.modulePlaceholder')" maxlength="40" />
        </el-form-item>
        <el-form-item :label="t('gen.className')" required>
          <el-input v-model="form.className" :placeholder="t('gen.classPlaceholder')" maxlength="40" />
        </el-form-item>
        <el-form-item :label="t('gen.businessName')">
          <el-input v-model="form.businessName" :placeholder="t('gen.businessPlaceholder')" maxlength="30" />
        </el-form-item>
        <el-form-item :label="t('gen.author')">
          <el-input v-model="form.author" :placeholder="t('gen.authorPlaceholder')" maxlength="30" />
        </el-form-item>
        <el-form-item :label="t('gen.softDelete')">
          <el-switch v-model="form.inheritBase" :disabled="!preview.tableName" />
          <span class="gen-tip">{{ t('gen.softDeleteTip') }}</span>
        </el-form-item>
        <el-form-item>
          <el-button type="success" icon="Tickets" :loading="previewingCode" :disabled="!form.tableName || !form.moduleName || !form.className"
            @click="handlePreviewCode">{{ t('gen.previewBtn') }}</el-button>
          <el-button type="primary" icon="Download" :loading="downloading" :disabled="!form.tableName || !form.moduleName || !form.className"
            @click="handleDownload">{{ t('gen.downloadBtn') }}</el-button>
          <span class="gen-tip">{{ t('gen.genTip') }}</span>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-bottom: 14px" v-if="codeFiles.length">
      <template #header>{{ t('gen.codePreviewHeader', { n: codeFiles.length }) }}</template>
      <el-tabs v-model="activeFile" type="border-card">
        <el-tab-pane v-for="f in codeFiles" :key="f.name" :name="f.name" :label="f.name.split('/').pop()">
          <pre class="code-view">{{ f.content }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-card shadow="never">
      <template #header>{{ preview.tableName ? t('gen.fieldHeaderWithTable', { table: preview.tableName }) : t('gen.fieldHeader') }}</template>
      <el-table :data="previewColumns" border size="small" v-loading="previewing">
        <el-table-column prop="columnName" :label="t('gen.colColumn')" width="180" />
        <el-table-column prop="fieldName" :label="t('gen.colField')" width="180" />
        <el-table-column prop="javaType" :label="t('gen.colJavaType')" width="160" />
        <el-table-column prop="jdbcType" :label="t('gen.colJdbcType')" width="110" />
        <el-table-column prop="comment" :label="t('gen.colComment')" min-width="150" show-overflow-tooltip />
        <el-table-column :label="t('gen.colPk')" width="70" align="center">
          <template #default="scope">{{ scope.row.isPk ? '✓' : '' }}</template>
        </el-table-column>
        <el-table-column :label="t('gen.colRequired')" width="70" align="center">
          <template #default="scope">{{ scope.row.isRequired ? '✓' : '' }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!preview.tableName && !previewing" :description="t('gen.emptyPreview')" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getGenTables, previewGenTable, downloadGen, previewCodeGen } from '@/api/setting'

const { t } = useI18n()
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
    if (!form.businessName && tables.value.find(tb => tb.tableName === tableName)?.tableComment) {
      form.businessName = tables.value.find(tb => tb.tableName === tableName).tableComment
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
    ElMessage.success(t('gen.downloadOk'))
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
