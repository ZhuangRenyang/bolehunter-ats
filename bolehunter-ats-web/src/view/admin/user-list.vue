<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span class="page-title">用户管理</span>
      <el-select v-model="groupId" placeholder="按角色筛选" clearable filterable style="width: 200px" @change="getAll">
        <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
      </el-select>
    </div>
    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="username" label="用户名" width="130" />
      <el-table-column prop="nickname" label="昵称" width="130" />
      <el-table-column label="用户类型" width="110" align="center">
        <template #default="scope">{{ typeNames[scope.row.user_type] || '顾问' }}</template>
      </el-table-column>
      <el-table-column label="所属角色" min-width="180">
        <template #default="scope">{{ groupNames(scope.row) }}</template>
      </el-table-column>
      <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="150" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageCount"
        :current-page="currentPage" @current-change="handlePageChange" />
    </div>

    <!-- 编辑用户弹窗：角色/类型/部门/密码 -->
    <el-dialog v-model="dialogVisible" title="编辑用户" width="480px">
      <el-form label-width="90px">
        <el-form-item label="用户名">{{ form.username }}</el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.groupIds" multiple filterable style="width: 100%">
            <el-option v-for="g in groups" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="form.userType" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="顾问" value="CONSULTANT" />
            <el-option label="客户HR" value="CLIENT_HR" />
            <el-option label="只读" value="READONLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="form.deptId" clearable style="width: 100%">
            <el-option v-for="d in depts" :key="d.id" :label="d.dept_name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="重置密码">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="留空表示不修改" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmEdit">保 存</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteUser, getAdminUsers, getAllGroups, resetUserPassword, updateAdminUser } from '@/api/user'
import { getDeptList } from '@/api/setting'

const typeNames = { ADMIN: '管理员', CONSULTANT: '顾问', CLIENT_HR: '客户HR', READONLY: '只读' }
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
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await getAll()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('此操作将永久删除该用户，是否继续？', '提示', { type: 'warning' })
  const res = await deleteUser(row.id)
  ElMessage.success(res.message || '删除成功')
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
