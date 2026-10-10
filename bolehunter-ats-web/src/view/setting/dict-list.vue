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
      <span class="page-title">字典管理</span>
      <el-button type="primary" plain size="small" icon="Plus" @click="handleTypeCreate">新增字典类型</el-button>
    </div>
    <el-row :gutter="16">
      <!-- 左：类型 -->
      <el-col :span="9">
        <el-table :data="typeData" border highlight-current-row v-loading="loading" @current-change="handleTypeSelect">
          <el-table-column prop="dict_name" label="字典名称" min-width="110" />
          <el-table-column prop="dict_type" label="类型键" min-width="130" />
          <el-table-column label="操作" width="130" align="center">
            <template #default="scope">
              <el-button size="small" type="primary" plain @click.stop="handleTypeEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click.stop="handleTypeDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination background layout="prev, pager, next" :total="typeTotal" :page-size="10"
            :current-page="typePage" @current-change="handleTypePage" />
        </div>
      </el-col>
      <!-- 右：数据 -->
      <el-col :span="15">
        <div class="data-header">
          <span>{{ currentType ? `字典数据：${currentType.dict_name}` : '请选择左侧字典类型' }}</span>
          <el-button type="primary" plain size="small" icon="Plus" :disabled="!currentType"
            @click="handleDataCreate">新增数据</el-button>
        </div>
        <el-table :data="dataItems" border v-loading="dataLoading">
          <el-table-column prop="dict_label" label="标签" min-width="130" />
          <el-table-column prop="dict_value" label="键值" min-width="110" />
          <el-table-column prop="dict_sort" label="排序" width="70" align="center" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="130" align="center">
            <template #default="scope">
              <el-button size="small" type="primary" plain @click="handleDataEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleDataDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination">
          <el-pagination background layout="prev, pager, next" :total="dataTotal" :page-size="10"
            :current-page="dataPage" @current-change="handleDataPage" />
        </div>
      </el-col>
    </el-row>

    <!-- 类型弹窗 -->
    <el-dialog v-model="typeDialogVisible" :title="typeForm.id ? '编辑字典类型' : '新增字典类型'" width="440px">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" maxlength="64" />
        </el-form-item>
        <el-form-item label="类型键" prop="dictType">
          <el-input v-model="typeForm.dictType" maxlength="64" placeholder="如 client_level" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmType">确 定</el-button>
        <el-button @click="typeDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 数据弹窗 -->
    <el-dialog v-model="dataDialogVisible" :title="dataForm.id ? '编辑字典数据' : '新增字典数据'" width="440px">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item label="标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" maxlength="64" />
        </el-form-item>
        <el-form-item label="键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" maxlength="64" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="dataForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dataForm.remark" type="textarea" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmData">确 定</el-button>
        <el-button @click="dataDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createDictData, createDictType, deleteDictData, deleteDictType, getDictDataPage,
  getDictTypePage, updateDictData, updateDictType,
} from '@/api/setting'

const loading = ref(false)
const dataLoading = ref(false)
const typeData = ref([])
const typeTotal = ref(0)
const typePage = ref(1)
const currentType = ref(null)
const typeDialogVisible = ref(false)
const typeFormRef = ref()
const typeForm = reactive({ id: null, dictName: '', dictType: '', remark: '' })
const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入类型键', trigger: 'blur' }],
}
const dataItems = ref([])
const dataTotal = ref(0)
const dataPage = ref(1)
const dataDialogVisible = ref(false)
const dataFormRef = ref()
const dataForm = reactive({ id: null, dictType: '', dictLabel: '', dictValue: '', dictSort: 0, status: 1, remark: '' })
const dataRules = {
  dictLabel: [{ required: true, message: '请输入标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入键值', trigger: 'blur' }],
}

async function getTypes() {
  loading.value = true
  try {
    const res = await getDictTypePage({ page: typePage.value - 1, count: 10 })
    typeData.value = res.items
    typeTotal.value = res.total
  } finally {
    loading.value = false
  }
}

async function getDataItems() {
  if (!currentType.value) return
  dataLoading.value = true
  try {
    const res = await getDictDataPage({ dict_type: currentType.value.dict_type, page: dataPage.value - 1, count: 10 })
    dataItems.value = res.items
    dataTotal.value = res.total
  } finally {
    dataLoading.value = false
  }
}

function handleTypeSelect(row) {
  currentType.value = row
  dataPage.value = 1
  getDataItems()
}

async function handleTypePage(val) {
  typePage.value = val
  await getTypes()
}

async function handleDataPage(val) {
  dataPage.value = val
  await getDataItems()
}

function handleTypeCreate() {
  Object.assign(typeForm, { id: null, dictName: '', dictType: '', remark: '' })
  typeDialogVisible.value = true
}

function handleTypeEdit(row) {
  Object.assign(typeForm, { id: row.id, dictName: row.dict_name, dictType: row.dict_type, remark: row.remark })
  typeDialogVisible.value = true
}

function confirmType() {
  typeFormRef.value.validate(async valid => {
    if (!valid) return
    const res = typeForm.id ? await updateDictType(typeForm.id, typeForm) : await createDictType(typeForm)
    ElMessage.success(res.message || '保存成功')
    typeDialogVisible.value = false
    await getTypes()
  })
}

function handleTypeDelete(row) {
  ElMessageBox.confirm('删除类型将同时删除其全部字典数据，是否继续？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteDictType(row.id)
    ElMessage.success(res.message || '删除成功')
    if (currentType.value && currentType.value.id === row.id) {
      currentType.value = null
      dataItems.value = []
    }
    await getTypes()
  })
}

function handleDataCreate() {
  Object.assign(dataForm, {
    id: null, dictType: currentType.value.dict_type, dictLabel: '', dictValue: '', dictSort: 0, status: 1, remark: '',
  })
  dataDialogVisible.value = true
}

function handleDataEdit(row) {
  Object.assign(dataForm, {
    id: row.id, dictType: row.dict_type, dictLabel: row.dict_label, dictValue: row.dict_value,
    dictSort: row.dict_sort, status: row.status, remark: row.remark,
  })
  dataDialogVisible.value = true
}

function confirmData() {
  dataFormRef.value.validate(async valid => {
    if (!valid) return
    const res = dataForm.id ? await updateDictData(dataForm.id, dataForm) : await createDictData(dataForm)
    ElMessage.success(res.message || '保存成功')
    dataDialogVisible.value = false
    await getDataItems()
  })
}

function handleDataDelete(row) {
  ElMessageBox.confirm('是否删除该字典数据？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteDictData(row.id)
    ElMessage.success(res.message || '删除成功')
    await getDataItems()
  })
}

onMounted(getTypes)
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
.data-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 13px;
  color: #606266;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
