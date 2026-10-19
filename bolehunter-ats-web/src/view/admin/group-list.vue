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
      <span class="page-title">{{ t('role.title') }}</span>
      <el-button type="primary" plain size="small" icon="Plus" @click="handleCreate">{{ t('role.create') }}</el-button>
    </div>
    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" :label="t('role.colRoleName')" min-width="180" />
      <el-table-column prop="info" :label="t('role.colDesc')" min-width="240" show-overflow-tooltip />
      <el-table-column :label="t('common.operation')" width="280" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="handlePermission(scope.row)">{{ t('role.permBtn') }}</el-button>
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">{{ t('common.edit') }}</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">{{ t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建/编辑角色 -->
    <el-dialog v-model="groupDialogVisible" :title="form.id ? t('role.editTitle') : t('role.createTitle')" width="440px">
      <el-form label-width="90px">
        <el-form-item :label="t('role.formName')">
          <el-input v-model="form.name" maxlength="30" />
        </el-form-item>
        <el-form-item :label="t('role.formDesc')">
          <el-input v-model="form.info" type="textarea" maxlength="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmGroup">{{ t('common.confirm') }}</el-button>
        <el-button @click="groupDialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限 -->
    <el-dialog v-model="permDialogVisible" :title="t('role.permTitle')" width="560px">
      <div v-for="(perms, module) in allPermissions" :key="module" class="perm-module">
        <div class="perm-module-title">{{ module }}</div>
        <el-checkbox-group v-model="selectedPermissionIds">
          <el-checkbox v-for="p in perms" :key="p.id" :value="p.id">{{ p.name }}</el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button type="primary" @click="confirmPermissions">{{ t('common.save') }}</el-button>
        <el-button @click="permDialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  createGroup,
  deleteGroup,
  dispatchPermissions,
  getAllGroups,
  getAllPermissions,
  getGroupDetail,
  getGroupPage,
  removePermissions,
  updateGroup,
} from '@/api/user'

const { t } = useI18n()
const loading = ref(false)
const tableData = ref([])
const groupDialogVisible = ref(false)
const permDialogVisible = ref(false)
const allPermissions = ref({})
const selectedPermissionIds = ref([])
const currentGroup = ref(null)
const form = reactive({ id: null, name: '', info: '' })

async function getAll() {
  loading.value = true
  try {
    const res = await getGroupPage({ page: 0, count: 30 })
    tableData.value = res.items
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  form.id = null
  form.name = ''
  form.info = ''
  groupDialogVisible.value = true
}

function handleEdit(row) {
  form.id = row.id
  form.name = row.name
  form.info = row.info
  groupDialogVisible.value = true
}

async function confirmGroup() {
  if (form.id) {
    await updateGroup(form.id, { name: form.name, info: form.info })
  } else {
    await createGroup({ name: form.name, info: form.info, permission_ids: [] })
  }
  ElMessage.success(t('common.saveOk'))
  groupDialogVisible.value = false
  await getAll()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(t('role.confirmDelete'), t('common.tip'), { type: 'warning' })
  const res = await deleteGroup(row.id)
  ElMessage.success(res.message || t('common.deleted'))
  await getAll()
}

async function handlePermission(row) {
  currentGroup.value = row
  const detail = await getGroupDetail(row.id)
  const owned = (detail.permissions || []).map(p => p.id)
  const all = await getAllPermissions()
  allPermissions.value = all
  selectedPermissionIds.value = owned
  permDialogVisible.value = true
}

async function confirmPermissions() {
  const detail = await getGroupDetail(currentGroup.value.id)
  const ownedIds = (detail.permissions || []).map(p => p.id)
  const toAdd = selectedPermissionIds.value.filter(id => !ownedIds.includes(id))
  const toRemove = ownedIds.filter(id => !selectedPermissionIds.value.includes(id))
  if (toAdd.length) {
    await dispatchPermissions({ group_id: currentGroup.value.id, permission_ids: toAdd })
  }
  if (toRemove.length) {
    await removePermissions({ group_id: currentGroup.value.id, permission_ids: toRemove })
  }
  ElMessage.success(t('role.permUpdated'))
  permDialogVisible.value = false
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
.perm-module {
  margin-bottom: 12px;
}
.perm-module-title {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 6px;
}
</style>
