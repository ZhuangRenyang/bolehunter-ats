<!--
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 -->
<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span class="page-title">功能开关</span>
      <el-button type="primary" plain size="small" icon="Plus" @click="handleCreate">新增开关</el-button>
    </div>
    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="flag_name" label="名称" width="160" />
      <el-table-column prop="flag_key" label="开关键" width="180" />
      <el-table-column prop="description" label="说明" min-width="260" show-overflow-tooltip />
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-switch :model-value="scope.row.enabled === 1" @change="handleToggle(scope.row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑开关' : '新增开关'" width="440px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="开关键" prop="flagKey">
          <el-input v-model="form.flagKey" maxlength="64" :disabled="!!form.id" placeholder="如 resume_parse" />
        </el-form-item>
        <el-form-item label="名称" prop="flagName">
          <el-input v-model="form.flagName" maxlength="64" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" maxlength="255" />
        </el-form-item>
        <el-form-item label="是否开启">
          <el-switch v-model="form.enabled" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmSubmit">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createFlag, deleteFlag, getFlags, toggleFlag, updateFlag } from '@/api/setting'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, flagKey: '', flagName: '', description: '', enabled: 0 })
const rules = {
  flagKey: [{ required: true, message: '请输入开关键', trigger: 'blur' }],
  flagName: [{ required: true, message: '请输入开关名称', trigger: 'blur' }],
}

async function getAll() {
  loading.value = true
  try {
    tableData.value = await getFlags()
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  Object.assign(form, { id: null, flagKey: '', flagName: '', description: '', enabled: 0 })
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, {
    id: row.id, flagKey: row.flag_key, flagName: row.flag_name, description: row.description, enabled: row.enabled,
  })
  dialogVisible.value = true
}

function confirmSubmit() {
  formRef.value.validate(async valid => {
    if (!valid) return
    const res = form.id ? await updateFlag(form.id, form) : await createFlag(form)
    ElMessage.success(res.message || '保存成功')
    dialogVisible.value = false
    await getAll()
  })
}

async function handleToggle(row) {
  await toggleFlag(row.id)
  row.enabled = row.enabled === 1 ? 0 : 1
  ElMessage.success(row.enabled === 1 ? '已开启' : '已关闭')
}

function handleDelete(row) {
  ElMessageBox.confirm('是否删除该功能开关？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteFlag(row.id)
    ElMessage.success(res.message || '删除成功')
    await getAll()
  })
}

onMounted(getAll)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.page-title {
  font-size: 15px;
  font-weight: 500;
}
</style>
