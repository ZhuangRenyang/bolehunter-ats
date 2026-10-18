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
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <div class="title-wrap">
        <el-button size="small" icon="Back" @click="$router.push('/candidate/list')">{{ t('client.back') }}</el-button>
        <span class="title" v-if="candidate">{{ candidate.name }}</span>
        <el-tag v-for="tagName in candidate ? candidate.tag_names : []" :key="tagName" size="small" class="tag-item">{{ tagName }}</el-tag>
      </div>
      <div>
        <el-button size="small" plain icon="PriceTag" @click="openTagDialog">{{ t('candidate.editTags') }}</el-button>
        <el-button size="small" plain icon="Clock" @click="changeHistoryVisible = true">{{ t('changeLog.openBtn') }}</el-button>
        <el-button type="primary" plain size="small" icon="Edit" @click="handleEdit">{{ t('common.edit') }}</el-button>
      </div>
    </div>

    <div class="section" v-if="candidate">
      <div class="section-title">{{ t('candidate.baseInfo') }}</div>
      <el-descriptions :column="3" border>
        <el-descriptions-item :label="t('candidate.colPhone')">{{ candidate.phone || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('candidate.colEmail')">{{ candidate.email || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('candidate.colCity')">{{ candidate.city || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('candidate.colCompany')">{{ candidate.current_company || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('candidate.colPosition')">{{ candidate.current_position || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('candidate.colEdu')">{{ dictMaps.education[candidate.education] || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('candidate.filterSource')">{{ dictMaps.candidate_source[candidate.source] || candidate.source }}</el-descriptions-item>
        <el-descriptions-item :label="t('common.owner')">{{ ownerMap[candidate.owner_id] || t('common.notAssigned') }}</el-descriptions-item>
        <el-descriptions-item :label="t('candidate.colRemark')" :span="3">{{ candidate.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="section" v-if="candidate">
      <div class="section-title">{{ t('candidate.resumeSection') }}</div>
      <el-link v-if="candidate.resume_file" type="primary" :href="resumeUrl" target="_blank" :underline="false">
        <el-icon><Document /></el-icon>&nbsp;{{ t('candidate.viewResume') }}
      </el-link>
      <span v-else class="empty-tip">{{ t('candidate.noResume') }}</span>
    </div>

    <el-dialog v-model="dialogVisible" :title="t('candidate.editTitle')" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="t('candidate.formName')" prop="name"><el-input v-model="form.name" maxlength="50" /></el-form-item>
        <el-form-item :label="t('candidate.colPhone')"><el-input v-model="form.phone" maxlength="20" /></el-form-item>
        <el-form-item :label="t('candidate.colEmail')"><el-input v-model="form.email" maxlength="100" /></el-form-item>
        <el-form-item :label="t('candidate.formCity')"><el-input v-model="form.city" maxlength="50" /></el-form-item>
        <el-form-item :label="t('candidate.colCompany')"><el-input v-model="form.currentCompany" maxlength="100" /></el-form-item>
        <el-form-item :label="t('candidate.colPosition')"><el-input v-model="form.currentPosition" maxlength="100" /></el-form-item>
        <el-form-item :label="t('candidate.colEdu')">
          <el-select v-model="form.education" clearable style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.education" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.owner')">
          <el-select v-model="form.ownerId" clearable filterable style="width: 100%">
            <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('candidate.colRemark')"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmSubmit">{{ t('common.confirm') }}</el-button>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tagDialogVisible" :title="t('candidate.editTags')" width="420px">
      <el-select v-model="selectedTagIds" multiple filterable style="width: 100%">
        <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
      </el-select>
      <template #footer>
        <el-button type="primary" @click="confirmTags">{{ t('common.confirm') }}</el-button>
        <el-button @click="tagDialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
  <DataChangeHistory v-model="changeHistoryVisible" table-name="candidate" :record-id="$route.params.id" />
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import DataChangeHistory from '@/components/DataChangeHistory.vue'
import { getCandidateDetail, getTags, setCandidateTags, updateCandidate } from '@/api/candidate'
import { getClientOwners } from '@/api/client'
import { getDictDataByType } from '@/api/setting'

const { t } = useI18n()
const route = useRoute()
const loading = ref(false)
const changeHistoryVisible = ref(false)
const candidateId = ref(null)
const candidate = ref(null)
const owners = ref([])
const ownerMap = ref({})
const tags = ref([])
const dictMaps = reactive({ education: {}, candidate_source: {} })
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({})
const rules = computed(() => ({ name: [{ required: true, message: t('candidate.ruleName'), trigger: 'blur' }] }))
const tagDialogVisible = ref(false)
const selectedTagIds = ref([])

const resumeUrl = computed(() =>
  candidate.value && candidate.value.resume_file
    ? `${window.location.origin}/assets/${candidate.value.resume_file}`
    : '',
)

function toMap(list) {
  const map = {}
  ;(list || []).forEach(item => { map[item.dict_value] = item.dict_label })
  return map
}

async function getDetail() {
  loading.value = true
  try {
    candidate.value = await getCandidateDetail(candidateId.value)
  } finally {
    loading.value = false
  }
}

function handleEdit() {
  Object.assign(form, {
    name: candidate.value.name, phone: candidate.value.phone, email: candidate.value.email,
    currentCompany: candidate.value.current_company, currentPosition: candidate.value.current_position,
    education: candidate.value.education, city: candidate.value.city, ownerId: candidate.value.owner_id,
    remark: candidate.value.remark,
  })
  dialogVisible.value = true
}

function confirmSubmit() {
  formRef.value.validate(async valid => {
    if (!valid) return
    const res = await updateCandidate(candidateId.value, form)
    ElMessage.success(res.message || t('common.saveOk'))
    dialogVisible.value = false
    await getDetail()
  })
}

function openTagDialog() {
  selectedTagIds.value = candidate.value ? candidate.value.tag_ids || [] : []
  tagDialogVisible.value = true
}

async function confirmTags() {
  const res = await setCandidateTags(candidateId.value, selectedTagIds.value)
  ElMessage.success(res.message || t('common.saveOk'))
  tagDialogVisible.value = false
  await getDetail()
}

onMounted(async () => {
  candidateId.value = Number(route.query.id)
  if (!candidateId.value) {
    ElMessage.error(t('candidate.missingId'))
    return
  }
  owners.value = await getClientOwners()
  const map = {}
  owners.value.forEach(u => { map[u.id] = u.nickname })
  ownerMap.value = map
  // 修复：getTags 此前未导入（onMounted 抛 ReferenceError 中断后续加载链）
  tags.value = await getTags()
  const [education, source] = await Promise.all([
    getDictDataByType('education'), getDictDataByType('candidate_source'),
  ])
  dictMaps.education = toMap(education)
  dictMaps.candidate_source = toMap(source)
  await getDetail()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}
.title {
  font-size: 16px;
  font-weight: 500;
}
.tag-item {
  margin-right: 6px;
}
.section {
  margin-top: 18px;
}
.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 10px;
}
.empty-tip {
  font-size: 12px;
  color: #909399;
}
</style>
