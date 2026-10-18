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
  <div class="login-shell">
    <!-- 左：深色画布（神经网络节点脉冲，迭代 29c） -->
    <div class="login-canvas">
      <canvas ref="netCanvas" class="net-canvas"></canvas>
      <div class="brand-line">
        <div class="brand-logo">伯</div>
        <span>BoleHunter 伯乐猎</span>
      </div>
      <div class="slogan">
        {{ t('login.slogan') }}
        <small>{{ t('login.sloganSub') }}</small>
      </div>
      <div class="login-plate m-plate">
        <div class="cell">
          <span class="k">SERVICE</span>
          <span><span class="dot"></span>{{ t('login.plateHealthy') }}</span>
        </div>
        <div class="cell">
          <span class="k">STACK</span>
          <span>Spring Boot 3.5 · Vue 3</span>
        </div>
        <div class="cell">
          <span class="k">LICENSE</span>
          <span>AGPL-3.0</span>
        </div>
      </div>
    </div>

    <!-- 右：珍珠玻璃登录卡 -->
    <div class="login-side">
      <div class="login-card">
        <h1>{{ t('login.title') }}</h1>
        <p class="sub">{{ t('login.sub') }}</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" :placeholder="t('login.username')" clearable>
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" :placeholder="t('login.password')" show-password>
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">{{ t('login.btn') }}</el-button>
        </el-form>
        <div class="foot">AGPL-3.0 · {{ t('login.foot') }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { saveTokens } from '@/api/request'
import { login } from '@/api/user'
import { useUserStore } from '@/stores/user'
// 神经网络背景（迭代 29c）：节点呼吸 + 连线脉冲，页面隐藏自动暂停
import { createNeuralNet } from '@/composables/neural'

const { t } = useI18n()
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const netCanvas = ref()
let neural = null
onMounted(() => {
  if (netCanvas.value) {
    neural = createNeuralNet(netCanvas.value, {
      colors: { pulse: '#9db9f7' },
    })
  }
})
onBeforeUnmount(() => {
  if (neural) neural.destroy()
})

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
// computed 包一层：切换语言后校验文案即时跟随
const rules = computed(() => ({
  username: [{ required: true, message: t('login.ruleUsername'), trigger: 'blur' }],
  password: [{ required: true, message: t('login.rulePassword'), trigger: 'blur' }],
}))

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    saveTokens(res.access_token, res.refresh_token)
    await userStore.loadPermissions()
    ElMessage.success(t('login.success'))
    router.push(route.query.redirect || '/workbench')
  } catch (e) {
    // 错误提示由请求层统一处理
  } finally {
    loading.value = false
  }
}
</script>
