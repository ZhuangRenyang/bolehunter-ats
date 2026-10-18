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
      <span class="page-title">{{ t('dict.title') }}</span>
      <el-button type="primary" plain size="small" icon="Plus" @click="handleTypeCreate">{{ t('dict.createType') }}</el-button>
    </div>
    <el-row :gutter="16">
      <!-- 左：类型（迭代 29d 样本册：每类型一卡，点击选中） -->
      <el-col :span="9">
        <div class="section-label">{{ t('dict.typeSection') }}</div>
        <div class="type-grid" v-loading="loading">
          <div v-for="td in typeData" :key="td.id" class="type-card"
            :class="{ active: currentType && currentType.id === td.id }" @click="handleTypeSelect(td)">
            <div class="type-card-main">
              <div class="type-card-name">{{ td.dict_name }}</div>
              <div class="type-card-key mono">{{ td.dict_type }}</div>
            </div>
            <div class="type-card-ops">
              <el-button size="small" text type="primary" @click.stop="handleTypeEdit(td)">{{ t('common.edit') }}</el-button>
              <el-button size="small" text type="danger" @click.stop="handleTypeDelete(td)">{{ t('common.delete') }}</el-button>
            </div>
          </div>
          <el-empty v-if="typeData.length === 0" :description="t('dict.selectTypeFirst')" :image-size="50" />
        </div>
        <div class="pagination">
          <el-pagination background layout="prev, pager, next" :total="typeTotal" :page-size="10"
            :current-page="typePage" @current-change="handleTypePage" />
        </div>
      </el-col>
      <!-- 右：数据 -->
      <el-col :span="15">
        <div class="section-label">{{ currentType ? t('dict.dataHeader', { name: currentType.dict_name }) : t('dict.selectTypeFirst') }}</div>
        <div class="data-header">
          <span></span>
          <el-button type="primary" plain size="small" icon="Plus" :disabled="!currentType"
            @click="handleDataCreate">{{ t('dict.createData') }}</el-button>
        </div>
        <el-table :data="dataItems" border v-loading="dataLoading">
          <el-table-column prop="dict_label" :label="t('dict.colLabel')" min-width="130" />
          <el-table-column prop="dict_value" :label="t('dict.colValue')" min-width="110" />
          <el-table-column prop="dict_sort" :label="t('dict.colSort')" width="70" align="center" />
          <el-table-column :label="t('common.status')" width="80" align="center">
            <template #default="scope">
              <el-tag size="small" :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? t('dict.statusNormal') : t('dict.statusDisabled') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="t('common.operation')" width="130" align="center">
            <template #default="scope">
              <el-button size="small" type="primary" plain @click="handleDataEdit(scope.row)">{{ t('common.edit') }}</el-button>
              <el-button size="small" type="danger" plain @click="handleDataDelete(scope.row)">{{ t('common.delete') }}</el-button>
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
    <el-dialog v-model="typeDialogVisible" :title="typeForm.id ? t('dict.editTypeTitle') : t('dict.createTypeTitle')" width="440px">
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item :label="t('dict.formDictName')" prop="dictName">
          <el-input v-model="typeForm.dictName" maxlength="64" />
        </el-form-item>
        <el-form-item :label="t('dict.formDictType')" prop="dictType">
          <el-input v-model="typeForm.dictType" maxlength="64" :placeholder="t('dict.typePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('common.remark')">
          <el-input v-model="typeForm.remark" type="textarea" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmType">{{ t('common.confirm') }}</el-button>
        <el-button @click="typeDialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- 数据弹窗 -->
    <el-dialog v-model="dataDialogVisible" :title="dataForm.id ? t('dict.editDataTitle') : t('dict.createDataTitle')" width="440px">
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item :label="t('dict.colLabel')" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" maxlength="64" />
        </el-form-item>
        <el-form-item :label="t('dict.colValue')" prop="dictValue">
          <el-input v-model="dataForm.dictValue" maxlength="64" />
        </el-form-item>
        <el-form-item :label="t('dict.colSort')">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item :label="t('common.status')">
          <el-switch v-model="dataForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item :label="t('common.remark')">
          <el-input v-model="dataForm.remark" type="textarea" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmData">{{ t('common.confirm') }}</el-button>
        <el-button @click="dataDialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  createDictData, createDictType, deleteDictData, deleteDictType, getDictDataPage,
  getDictTypePage, updateDictData, updateDictType,
} from '@/api/setting'

const { t } = useI18n()
const loading = ref(false)
const dataLoading = ref(false)
const typeData = ref([])
const typeTotal = ref(0)
const typePage = ref(1)
const currentType = ref(null)
const typeDialogVisible = ref(false)
const typeFormRef = ref()
const typeForm = reactive({ id: null, dictName: '', dictType: '', remark: '' })
const typeRules = computed(() => ({
  dictName: [{ required: true, message: t('dict.ruleDictName'), trigger: 'blur' }],
  dictType: [{ required: true, message: t('dict.ruleDictType'), trigger: 'blur' }],
}))
const dataItems = ref([])
const dataTotal = ref(0)
const dataPage = ref(1)
const dataDialogVisible = ref(false)
const dataFormRef = ref()
const dataForm = reactive({ id: null, dictType: '', dictLabel: '', dictValue: '', dictSort: 0, status: 1, remark: '' })
const dataRules = computed(() => ({
  dictLabel: [{ required: true, message: t('dict.ruleLabel'), trigger: 'blur' }],
  dictValue: [{ required: true, message: t('dict.ruleValue'), trigger: 'blur' }],
}))

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
    ElMessage.success(res.message || t('common.saveOk'))
    typeDialogVisible.value = false
    await getTypes()
  })
}

function handleTypeDelete(row) {
  ElMessageBox.confirm(t('dict.confirmDeleteType'), t('common.tip'), { type: 'warning' }).then(async () => {
    const res = await deleteDictType(row.id)
    ElMessage.success(res.message || t('common.deleted'))
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
    ElMessage.success(res.message || t('common.saveOk'))
    dataDialogVisible.value = false
    await getDataItems()
  })
}

function handleDataDelete(row) {
  ElMessageBox.confirm(t('dict.confirmDeleteData'), t('common.tip'), { type: 'warning' }).then(async () => {
    const res = await deleteDictData(row.id)
    ElMessage.success(res.message || t('common.deleted'))
    await getDataItems()
  })
}

onMounted(getTypes)
</script>

<style scoped>
.data-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 13px;
  color: var(--ink-soft);
}
/* 样本册：字典类型卡片（迭代 29d） */
.type-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 200px;
}
.type-card {
  background: var(--pearl);
  border: 1px solid var(--glass-line);
  border-left: 3px solid var(--cat-color, var(--ink-faint));
  border-radius: var(--r-sm);
  padding: 10px 12px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  transition: border-color 0.15s, box-shadow 0.15s, transform 0.15s;
}
.type-card:nth-child(5n + 1) { --cat-color: var(--cat-1); }
.type-card:nth-child(5n + 2) { --cat-color: var(--cat-2); }
.type-card:nth-child(5n + 3) { --cat-color: var(--cat-3); }
.type-card:nth-child(5n + 4) { --cat-color: var(--cat-4); }
.type-card:nth-child(5n + 5) { --cat-color: var(--cat-5); }
.type-card:hover {
  box-shadow: var(--e2);
  transform: translateY(-1px);
}
.type-card.active {
  border-color: var(--blue);
  box-shadow: 0 0 0 1px var(--blue) inset, var(--e2);
}
.type-card-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
}
.type-card-key {
  font-size: 11px;
  color: var(--ink-soft);
  margin-top: 2px;
}
.type-card-ops {
  flex-shrink: 0;
  display: flex;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
