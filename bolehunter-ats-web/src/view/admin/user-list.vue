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
      <el-select v-model="groupId" :placeholder="t('user.filterRole')" clearable filterable style="width: 200px" @change="getAll">
        <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
      </el-select>
    </div>
    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="username" :label="t('user.colUsername')" width="130" />
      <el-table-column prop="nickname" :label="t('user.colNickname')" width="130" />
      <el-table-column :label="t('user.colUserType')" width="110" align="center">
        <template #default="scope">{{ typeName(scope.row.user_type) }}</template>
      </el-table-column>
      <el-table-column :label="t('user.colGroups')" min-width="180">
        <template #default="scope">{{ groupNames(scope.row) }}</template>
      </el-table-column>
      <el-table-column prop="email" :label="t('user.colEmail')" min-width="160" show-overflow-tooltip />
      <el-table-column :label="t('common.operation')" width="150" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">{{ t('common.edit') }}</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">{{ t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageCount"
        :current-page="currentPage" @current-change="handlePageChange" />
    </div>

    <!-- 编辑用户弹窗：角色/类型/部门/密码 -->
    <el-dialog v-model="dialogVisible" :title="t('user.dialogTitle')" width="480px">
      <el-form label-width="90px">
        <el-form-item :label="t('user.fieldUsername')">{{ form.username }}</el-form-item>
        <el-form-item :label="t('user.fieldGroups')">
          <el-select v-model="form.groupIds" multiple filterable style="width: 100%">
            <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('user.fieldUserType')">
          <el-select v-model="form.userType" style="width: 100%">
            <el-option v-for="(label, key) in USER_TYPE_KEYS" :key="key" :label="t(label)" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('user.fieldDept')">
          <el-select v-model="form.deptId" clearable style="width: 100%">
            <el-option v-for="d in depts" :key="d.id" :label="d.dept_name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('user.fieldResetPwd')">
          <el-input v-model="form.newPassword" type="password" show-password :placeholder="t('user.pwdPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmEdit">{{ t('common.save') }}</el-button>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { deleteUser, getAdminUsers, getAllGroups, resetUserPassword, updateAdminUser } from '@/api/user'
import { getDeptList } from '@/api/setting'

const { t } = useI18n()

// 用户类型枚举 → 语言包 user.type.*（显示与下拉共用）
const USER_TYPE_KEYS = {
  ADMIN: 'user.type.ADMIN', CONSULTANT: 'user.type.CONSULTANT',
  CLIENT_HR: 'user.type.CLIENT_HR', READONLY: 'user.type.READONLY',
}

function typeName(type) {
  const key = `user.type.${type}`
  const translated = t(key)
  // 未知类型兜底展示原始枚举值
  return translated === key ? (type || '') : translated
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageCount = 10
const groupId = ref('')
const groups = ref([])
const depts = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, username: '', groupIds: [], userType: 'CONSULTANT', deptId: null, newPassword: '' })

function groupNames(row) {
  return (row.groups || []).map(g => g.name).join('，') || '—'
}

async function getAll() {
  loading.value = true
  try {
    const res = await getAdminUsers({ group_id: groupId.value || undefined, page: currentPage.value - 1, count: pageCount })
    tableData.value = res.items
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function handlePageChange(val) {
  currentPage.value = val
  await getAll()
}

function handleEdit(row) {
  form.id = row.id
  form.username = row.username
  form.groupIds = (row.groups || []).map(g => g.id)
  form.userType = row.user_type || 'CONSULTANT'
  form.deptId = row.dept_id || null
  form.newPassword = ''
  dialogVisible.value = true
}

async function confirmEdit() {
  await updateAdminUser(form.id, {
    group_ids: form.groupIds,
    user_type: form.userType || undefined,
    dept_id: form.deptId || undefined,
  })
  if (form.newPassword) {
    await resetUserPassword(form.id, { new_password: form.newPassword })
  }
  ElMessage.success(t('common.saveOk'))
  dialogVisible.value = false
  await getAll()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(t('user.confirmDelete'), t('common.tip'), { type: 'warning' })
  const res = await deleteUser(row.id)
  ElMessage.success(res.message || t('common.deleted'))
  await getAll()
}

onMounted(async () => {
  await getAll()
  groups.value = await getAllGroups()
  try {
    depts.value = await getDeptList()
  } catch (e) {
    depts.value = []
  }
})
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
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
