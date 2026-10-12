<!--
  BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
  Copyright (C) 2026 ${_meta.author}
  GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
  Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
  由 BoleHunter 代码生成器生成于 ${date}，模块：${moduleName}（${businessName}）
  本文件遵循 AGPL-3.0-or-later，请勿移除版权声明。
-->
<template>
  <div class="page">
    <div class="page-title">${businessName}管理</div>

    <el-card shadow="never">
      <!-- 搜索栏 -->
      <div class="search-bar">
<#assign varcharCols = bizColumns?filter(c -> c.isVarchar) />
<#list varcharCols[0..*2] as c>
        <el-input v-model="query.${c.fieldName}" placeholder="按${c.comment}搜索" clearable style="width: 200px" @keyup.enter="loadPage(0)" />
</#list>
        <el-button type="primary" icon="Search" @click="loadPage(0)">搜索</el-button>
        <el-button type="success" icon="Plus" @click="openForm()">新增${businessName}</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="items" border stripe v-loading="loading">
<#list bizColumns as c>
        <el-table-column label="${c.comment}" prop="${c.fieldName}" min-width="120" show-overflow-tooltip />
</#list>
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="openForm(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination layout="total, prev, pager, next" :total="total" :page-size="count"
        :current-page="page + 1" @current-change="p => loadPage(p - 1)" style="margin-top: 12px; justify-content: flex-end" />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="formVisible" :title="form.id ? '编辑${businessName}' : '新增${businessName}'" width="560px">
      <el-form :model="form" label-width="110px">
<#list bizColumns as c>
        <el-form-item label="${c.comment}"<#if c.isRequired> required</#if>>
<#if c.isVarchar && c.columnType?? && c.columnType?lower_case?contains("text")>
          <el-input v-model="form.${c.fieldName}" type="textarea" :rows="3" />
<#elseif c.isDate>
          <el-date-picker v-model="form.${c.fieldName}" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
<#elseif !c.isVarchar>
          <el-input-number v-model="form.${c.fieldName}" style="width: 100%" />
<#else>
          <el-input v-model="form.${c.fieldName}" maxlength="100" />
</#if>
        </el-form-item>
</#list>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post, put, _delete } from '@/api/request'

const API = 'recruit/${moduleName}'

const items = ref([])
const total = ref(0)
const page = ref(0)
const count = 10
const loading = ref(false)
const formVisible = ref(false)
const saving = ref(false)
const query = reactive({})
const form = reactive({})

function buildQuery() {
  const params = { page: page.value, count }
<#list varcharCols[0..*2] as c>
  if (query.${c.fieldName}) params.${c.fieldName} = query.${c.fieldName}
</#list>
  return params
}

async function loadPage(p) {
  page.value = p
  loading.value = true
  try {
    const res = await get(`${'$'}{API}/page`, buildQuery())
    items.value = res.items || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function openForm(row) {
  Object.keys(form).forEach(k => delete form[k])
  if (row) {
    Object.assign(form, row)
  } else {
    form.id = null
<#list bizColumns as c>
    form.${c.fieldName} = null
</#list>
  }
  formVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (form.id) {
      await put(`${'$'}{API}/${'$'}{form.id}`, form)
      ElMessage.success('更新成功')
    } else {
      await post(API, form)
      ElMessage.success('新增成功')
    }
    formVisible.value = false
    await loadPage(page.value)
  } finally {
    saving.value = false
  }
}

function handleDelete(row) {
  ElMessageBox.confirm('确认删除该${businessName}吗？', '提示', { type: 'warning' }).then(async () => {
    await _delete(`${'$'}{API}/${'$'}{row.id}`)
    ElMessage.success('删除成功')
    await loadPage(page.value)
  }).catch(() => {})
}

onMounted(() => loadPage(0))
</script>

<style scoped>
.page-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
</style>
