<!--
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 -->
<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span class="page-title">部门管理</span>
      <el-button type="primary" plain size="small" icon="Plus" @click="handleCreate(0)">新增部门</el-button>
    </div>
    <el-table :data="tableData" border row-key="id" default-expand-all
      :tree-props="{ children: 'children' }" v-loading="loading">
      <el-table-column prop="dept_name" label="部门名称" min-width="200" />
      <el-table-column prop="sort" label="排序" width="80" align="center" />
      <el-table-column prop="leader" label="负责人" width="140" align="center" />
      <el-table-column prop="phone" label="联系电话" width="160" align="center" />
      <el-table-column label="操作" width="250" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="handleCreate(scope.row.id)">添加子部门</el-button>
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑部门' : '新增部门'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级部门">
          <el-select v-model="form.parentId" style="width: 100%">
            <el-option label="顶级部门" :value="0" />
            <el-option v-for="item in deptOptions" :key="item.id" :label="item.dept_name" :value="item.id"
              :disabled="form.id === item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" maxlength="50" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leader" maxlength="50" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" maxlength="20" />
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
import { createDept, deleteDept, getDeptList, getDeptTree, updateDept } from '@/api/setting'

const loading = ref(false)
const tableData = ref([])
const deptOptions = ref([])
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, parentId: 0, deptName: '', sort: 0, leader: '', phone: '' })
const rules = { deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }] }

async function getAll() {
  loading.value = true
  try {
    tableData.value = await getDeptTree()
    deptOptions.value = await getDeptList()
  } finally {
    loading.value = false
  }
}

function handleCreate(parentId) {
  Object.assign(form, { id: null, parentId: parentId || 0, deptName: '', sort: 0, leader: '', phone: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, {
    id: row.id,
    parentId: row.parent_id,
    deptName: row.dept_name,
    sort: row.sort,
    leader: row.leader,
    phone: row.phone,
  })
  dialogVisible.value = true
}

function confirmSubmit() {
  formRef.value.validate(async valid => {
    if (!valid) return
    const res = form.id ? await updateDept(form.id, form) : await createDept(form)
    ElMessage.success(res.message || '保存成功')
    dialogVisible.value = false
    await getAll()
  })
}

function handleDelete(row) {
  ElMessageBox.confirm('是否删除该部门？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteDept(row.id)
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
