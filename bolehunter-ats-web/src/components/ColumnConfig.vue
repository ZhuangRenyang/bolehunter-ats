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
  <el-popover trigger="click" width="230" placement="bottom-end">
    <template #reference>
      <el-button size="small" plain icon="Setting">{{ t('columnConfig.title') }}</el-button>
    </template>
    <div class="cc-body">
      <div class="cc-tip">{{ t('columnConfig.tip') }}</div>
      <el-checkbox-group v-model="checked" class="cc-group">
        <el-checkbox v-for="col in columns" :key="col.prop" :value="col.prop" :label="col.label" />
      </el-checkbox-group>
      <div class="cc-actions">
        <el-button size="small" plain @click="handleReset">{{ t('columnConfig.reset') }}</el-button>
        <el-button size="small" type="primary" @click="handleSave">{{ t('common.save') }}</el-button>
      </div>
    </div>
  </el-popover>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  /** 全部可配置列：[{ prop, label }] */
  columns: { type: Array, required: true },
  /** 当前显示的 prop 数组（v-model） */
  modelValue: { type: Array, required: true },
  /** 全量 prop 列表（用于重置） */
  defaultVisible: { type: Array, required: true },
})
const emit = defineEmits(['update:modelValue', 'save'])

const { t } = useI18n()

/** checkbox 组的临时选中态（点保存才提交，避免误触即改） */
const checked = ref([...props.modelValue])

watch(() => props.modelValue, v => { checked.value = [...v] })

const allProps = computed(() => props.columns.map(c => c.prop))

function handleSave() {
  // 至少保留一列
  const next = checked.value.filter(p => allProps.value.includes(p))
  emit('update:modelValue', next.length ? next : props.defaultVisible)
  emit('save', next.length ? next : props.defaultVisible)
}

function handleReset() {
  checked.value = [...props.defaultVisible]
  emit('update:modelValue', [...props.defaultVisible])
  emit('save', [...props.defaultVisible])
}
</script>

<style scoped>
.cc-tip {
  font-size: 11px;
  color: var(--ink-faint);
  margin-bottom: 8px;
}
.cc-group {
  display: flex;
  flex-direction: column;
  gap: 2px;
  max-height: 240px;
  overflow-y: auto;
}
.cc-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 10px;
}
</style>
