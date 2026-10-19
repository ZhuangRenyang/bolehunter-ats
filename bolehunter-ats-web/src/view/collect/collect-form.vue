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
  <div class="collect-page">
    <el-card class="collect-card" v-loading="checking">
      <el-result v-if="submitted" icon="success" :title="t('collect.doneTitle')" :sub-title="t('collect.doneSub' )" />
      <template v-else-if="valid">
        <h2 class="collect-title">{{ title || t('collect.formTitle') }}</h2>
        <p class="collect-sub">{{ t('collect.sub') }}</p>
        <el-form :model="form" label-position="top">
          <el-form-item :label="t('collect.name')" required>
            <el-input v-model="form.name" maxlength="50" />
          </el-form-item>
          <el-form-item :label="t('collect.phone')" required>
            <el-input v-model="form.phone" maxlength="20" />
          </el-form-item>
          <el-form-item :label="t('collect.email')">
            <el-input v-model="form.email" maxlength="100" />
          </el-form-item>
          <el-form-item :label="t('collect.company')">
            <el-input v-model="form.currentCompany" maxlength="100" />
          </el-form-item>
          <el-form-item :label="t('collect.position')">
            <el-input v-model="form.currentPosition" maxlength="100" />
          </el-form-item>
          <el-form-item :label="t('collect.city')">
            <el-input v-model="form.city" maxlength="50" />
          </el-form-item>
          <el-form-item :label="t('collect.resume')">
            <input type="file" accept=".pdf,.doc,.docx" class="collect-file" @change="onFileChange" />
          </el-form-item>
          <el-form-item :label="t('collect.intro')">
            <el-input v-model="form.intro" type="textarea" :rows="3" maxlength="500" />
          </el-form-item>
          <el-button type="primary" size="large" style="width: 100%" :loading="submitting" @click="submit">
            {{ t('collect.submit') }}
          </el-button>
        </el-form>
      </template>
      <el-result v-else-if="!checking" icon="warning"
        :title="t('collect.invalidTitle')" :sub-title="t('collect.invalidSub')" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import defaultAxios from '@/api/request'

const { t } = useI18n()
const route = useRoute()
const token = route.params.token

const checking = ref(true)
const valid = ref(false)
const title = ref('')
const submitting = ref(false)
const submitted = ref(false)
const file = ref(null)

const form = reactive({
  name: '', phone: '', email: '', currentCompany: '',
  currentPosition: '', city: '', intro: '',
})

function onFileChange(e) {
  file.value = e.target.files[0] || null
}

onMounted(async () => {
  try {
    const res = await defaultAxios.get(`recruit/public/collect/${token}`)
    valid.value = !!res.data?.valid
    title.value = res.data?.title || ''
  } catch (e) {
    valid.value = false
  } finally {
    checking.value = false
  }
})

async function submit() {
  if (!form.name.trim() || !form.phone.trim()) {
    ElMessage.warning(t('collect.required'))
    return
  }
  submitting.value = true
  try {
    const fd = new FormData()
    fd.append('name', form.name.trim())
    fd.append('phone', form.phone.trim())
    if (form.email.trim()) fd.append('email', form.email.trim())
    if (form.currentCompany.trim()) fd.append('currentCompany', form.currentCompany.trim())
    if (form.currentPosition.trim()) fd.append('currentPosition', form.currentPosition.trim())
    if (form.city.trim()) fd.append('city', form.city.trim())
    if (form.intro.trim()) fd.append('intro', form.intro.trim())
    if (file.value) fd.append('file', file.value)
    await defaultAxios.post(`recruit/public/collect/${token}`, fd)
    submitted.value = true
    ElMessage.success(t('collect.ok'))
    valid.value = false
    checking.value = false
    // 提交成功后展示感谢态
    setTimeout(() => { location.reload() }, 1500)
  } catch (e) {
    // 错误提示由请求层统一处理
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.collect-page {
  min-height: 100vh;
  background: var(--paper);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 40px 16px;
}
.collect-card {
  width: 100%;
  max-width: 560px;
}
.collect-title {
  margin: 0 0 6px;
  font-size: 20px;
  color: var(--ink);
}
.collect-sub {
  font-size: 13px;
  color: var(--ink-soft);
  margin: 0 0 18px;
}
.collect-file {
  font-size: 13px;
}
</style>
