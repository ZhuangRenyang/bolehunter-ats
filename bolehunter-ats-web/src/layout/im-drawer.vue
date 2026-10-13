<!--
  BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
  Copyright (C) 2026 ZhuangRenyang
  GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
  Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats

  本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
  依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
  以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
-->
<template>
  <el-drawer v-model="inner" title="内部消息" size="440px">
    <template #header>
      <div class="im-head">
        <span>内部消息</span>
        <el-tag size="small" :type="ws.connected ? 'success' : 'info'">
          {{ ws.connected ? '实时连接已建立' : (ws.unavailable ? '实时推送不可用（已降级轮询）' : '连接中…') }}
        </el-tag>
      </div>
    </template>

    <!-- 会话列表 -->
    <div v-if="!peerId" class="im-list">
      <el-select v-model="picked" filterable placeholder="选择同事发起会话" size="small" style="width: 100%"
        @change="startConversation">
        <el-option v-for="c in colleagues" :key="c.id" :label="c.nickname || c.username" :value="c.id" />
      </el-select>

      <div v-for="c in conversations" :key="c.peerId" class="im-item" @click="openConversation(c)">
        <div class="im-row">
          <span class="im-name">{{ c.peerName }}</span>
          <span class="im-time">{{ c.lastTime }}</span>
        </div>
        <div class="im-row">
          <span class="im-last">{{ (c.outgoing ? '我：' : '') + c.lastContent }}</span>
          <el-badge v-if="c.unreadCount > 0" :value="c.unreadCount" class="im-badge" />
        </div>
      </div>
      <el-empty v-if="!conversations.length && !loading" description="暂无会话，从上方选择同事开始" :image-size="60" />
    </div>

    <!-- 聊天区 -->
    <div v-else class="im-chat">
      <div class="im-chat-head">
        <el-button size="small" @click="back">返回</el-button>
        <span class="im-peer">{{ peerName }}</span>
      </div>

      <div ref="msgBox" class="im-msgs">
        <div v-for="(m, i) in messages" :key="m.id || i" class="im-msg" :class="{ me: m.senderId === meId }">
          <div class="im-bubble">{{ m.content }}</div>
          <div class="im-meta">{{ m.senderName }} · {{ m.createTime }}</div>
        </div>
        <el-empty v-if="!messages.length" description="还没有消息，说点什么吧" :image-size="60" />
      </div>

      <div class="im-input">
        <el-input v-model="draft" type="textarea" :rows="2" resize="none" maxlength="2000" show-word-limit
          placeholder="输入消息，Enter 发送 / Ctrl+Enter 换行" @keydown.enter.exact.prevent="doSend" />
        <div class="im-actions">
          <el-button type="primary" size="small" :loading="sending" :disabled="!draft.trim()" @click="doSend">
            发送
          </el-button>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getColleagues, getConversations, getImHistory, getImUnreadTotal, markImRead, sendImMessage,
} from '@/api/im'
import { useWsStore } from '@/stores/ws'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue'])

const ws = useWsStore()
const inner = computed({
  get: () => props.modelValue,
  set: v => emit('update:modelValue', v),
})

const conversations = ref([])
const colleagues = ref([])
const messages = ref([])
const meId = ref(null)
const peerId = ref(null)
const peerName = ref('')
const draft = ref('')
const picked = ref(null)
const loading = ref(false)
const sending = ref(false)
const msgBox = ref(null)

async function loadBase() {
  try {
    const res = await getImUnreadTotal()
    meId.value = res?.me ?? null
  } catch (e) { /* 静默：后续会话列表仍可渲染 */ }
}

async function loadConversations() {
  loading.value = true
  try {
    conversations.value = (await getConversations()) || []
  } catch (e) {
    /* 接口失败保留原列表 */
  } finally {
    loading.value = false
  }
}

async function loadColleagues() {
  try {
    colleagues.value = (await getColleagues()) || []
  } catch (e) { /* 忽略 */ }
}

async function openConversation(peer) {
  peerId.value = peer.peerId
  peerName.value = peer.peerName
  messages.value = []
  try {
    const res = await getImHistory(peer.peerId)
    messages.value = res?.messages || []
  } catch (e) { /* 忽略 */ }
  await scrollBottom()
  // 打开即上报已读，并通过 WebSocket 给对方发送已读回执
  markImRead(peer.peerId).catch(() => {})
  // 清掉本地未读角标
  const item = conversations.value.find(c => c.peerId === peer.peerId)
  if (item) item.unreadCount = 0
}

function startConversation(userId) {
  const target = colleagues.value.find(c => c.id === userId)
  openConversation({ peerId: userId, peerName: (target && (target.nickname || target.username)) || '' })
  picked.value = null
}

function back() {
  peerId.value = null
  messages.value = []
  loadConversations()
}

async function doSend() {
  const text = draft.value.trim()
  if (!text || sending.value) return
  sending.value = true
  const target = peerId.value
  const sent = ws.send({ type: 'chat', receiverId: target, content: text })
  draft.value = ''
  try {
    if (!sent) {
      // WebSocket 不可用 → 降级 HTTP 发送，消息照样入库
      await sendImMessage(target, text)
    }
    // 无论走哪条通道，成功后由 chat 事件统一追加气泡（HTTP 通道则手动拉取）
    if (!sent) {
      const res = await getImHistory(target)
      messages.value = res?.messages || []
      await scrollBottom()
    }
  } catch (e) {
    ElMessage.error((e && e.message) || '发送失败')
    draft.value = text
  } finally {
    sending.value = false
  }
}

async function scrollBottom() {
  await nextTick()
  if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
}

// WebSocket 消息：命中当前会话则追加气泡，否则仅刷新会话列表（由列表的未读角标体现）
let offChat = null
let offError = null
watch(inner, async visible => {
  if (!visible) return
  await loadBase()
  await Promise.all([loadConversations(), loadColleagues()])
  if (!offChat) {
    offChat = ws.on('chat', async data => {
      if (!data) return
      const peer = data.senderId === meId.value ? data.receiverId : data.senderId
      if (peerId.value && peer === peerId.value) {
        messages.value.push(data)
        if (data.senderId !== meId.value) markImRead(peer).catch(() => {})
        await scrollBottom()
      }
      loadConversations()
    })
    offError = ws.on('error', msg => ElMessage.warning(typeof msg === 'string' ? msg : JSON.stringify(msg)))
  }
})

onBeforeUnmount(() => {
  if (offChat) offChat()
  if (offError) offError()
})
</script>

<style lang="scss" scoped>
.im-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.im-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.im-item {
  padding: 10px 12px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  cursor: pointer;

  &:hover {
    background: #f5f7fa;
  }
}

.im-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.im-name {
  font-weight: 600;
}

.im-time {
  color: #909399;
  font-size: 12px;
}

.im-last {
  color: #606266;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 300px;
}

.im-badge {
  margin-left: 8px;
}

.im-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.im-chat-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.im-peer {
  font-weight: 600;
}

.im-msgs {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.im-msg {
  display: flex;
  flex-direction: column;
  align-items: flex-start;

  &.me {
    align-items: flex-end;
  }
}

.im-bubble {
  max-width: 280px;
  padding: 8px 12px;
  border-radius: 8px;
  background: #f4f4f5;
  word-break: break-word;
  white-space: pre-wrap;

  .me & {
    background: #409eff;
    color: #fff;
  }
}

.im-meta {
  margin-top: 2px;
  font-size: 12px;
  color: #909399;
}

.im-input {
  border-top: 1px solid #ebeef5;
  padding-top: 10px;
}

.im-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
