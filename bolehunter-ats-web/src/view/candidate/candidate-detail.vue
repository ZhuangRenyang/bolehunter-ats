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
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <div class="title-wrap">
        <el-button size="small" icon="Back" @click="$router.push('/candidate/list')">返回</el-button>
        <span class="title" v-if="candidate">{{ candidate.name }}</span>
        <el-tag v-for="t in candidate ? candidate.tag_names : []" :key="t" size="small" class="tag-item">{{ t }}</el-tag>
      </div>
      <div>
        <el-button size="small" plain icon="PriceTag" @click="openTagDialog">编辑标签</el-button>
        <el-button type="primary" plain size="small" icon="Edit" @click="handleEdit">编辑</el-button>
      </div>
    </div>

    <div class="section" v-if="candidate">
      <div class="section-title">基本信息</div>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="电话">{{ candidate.phone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ candidate.email || '—' }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ candidate.city || '—' }}</el-descriptions-item>
        <el-descriptions-item label="当前公司">{{ candidate.current_company || '—' }}</el-descriptions-item>
        <el-descriptions-item label="当前职位">{{ candidate.current_position || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学历">{{ dictMaps.education[candidate.education] || '—' }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ dictMaps.candidate_source[candidate.source] || candidate.source }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ ownerMap[candidate.owner_id] || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">{{ candidate.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="section" v-if="candidate">
      <div class="section-title">简历原件</div>
      <el-link v-if="candidate.resume_file" type="primary" :href="resumeUrl" target="_blank" :underline="false">
        <el-icon><Document /></el-icon>&nbsp;查看简历文件
      </el-link>
      <span v-else class="empty-tip">未上传简历原件</span>
    </div>

    <el-dialog v-model="dialogVisible" title="编辑候选人" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" maxlength="50" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" maxlength="20" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" maxlength="100" /></el-form-item>
        <el-form-item label="所在城市"><el-input v-model="form.city" maxlength="50" /></el-form-item>
        <el-form-item label="当前公司"><el-input v-model="form.currentCompany" maxlength="100" /></el-form-item>
        <el-form-item label="当前职位"><el-input v-model="form.currentPosition" maxlength="100" /></el-form-item>
        <el-form-item label="学历">
          <el-select v-model="form.education" clearable style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.education" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.ownerId" clearable filterable style="width: 100%">
            <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmSubmit">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tagDialogVisible" title="编辑标签" width="420px">
      <el-select v-model="selectedTagIds" multiple filterable style="width: 100%">
        <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <template #footer>
        <el-button type="primary" @click="confirmTags">确 定</el-button>
        <el-button @click="tagDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCandidateDetail, getCandidateOptions, setCandidateTags, updateCandidate } from '@/api/candidate'
import { getClientOwners } from '@/api/client'
import { getDictDataByType } from '@/api/setting'

const route = useRoute()
const loading = ref(false)
const candidateId = ref(null)
const candidate = ref(null)
const owners = ref([])
const ownerMap = ref({})
const tags = ref([])
const dictMaps = reactive({ education: {}, candidate_source: {} })
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({})
const rules = { name: [{ required: true, message: '请输入候选人姓名', trigger: 'blur' }] }
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
    ElMessage.success(res.message || '保存成功')
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
  ElMessage.success(res.message || '保存成功')
  tagDialogVisible.value = false
  await getDetail()
}

onMounted(async () => {
  candidateId.value = Number(route.query.id)
  if (!candidateId.value) {
    ElMessage.error('缺少候选人id参数')
    return
  }
  owners.value = await getClientOwners()
  const map = {}
  owners.value.forEach(u => { map[u.id] = u.nickname })
  ownerMap.value = map
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
