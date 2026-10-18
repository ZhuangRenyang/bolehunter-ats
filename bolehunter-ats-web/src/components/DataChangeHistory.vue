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
  <el-drawer :model-value="modelValue" :title="t('changeLog.title')" size="500px"
    @update:model-value="$emit('update:modelValue', $event)">
    <div v-loading="loading">
      <el-timeline v-if="groups.length" style="padding-left: 6px">
        <el-timeline-item v-for="g in groups" :key="g.time" :timestamp="g.time" placement="top" type="primary">
          <div v-for="c in g.items" :key="c.id" class="diff-row">
            <div class="diff-field">{{ fieldLabel(c.field) }}</div>
            <div class="diff-values">
              <span class="diff-old">{{ c.oldValue || t('changeLog.emptyValue') }}</span>
              <el-icon class="diff-arrow"><Right /></el-icon>
              <span class="diff-new">{{ c.newValue || t('changeLog.emptyValue') }}</span>
            </div>
            <div class="diff-operator">{{ t('changeLog.by') }} {{ c.operator || '—' }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else :description="t('changeLog.empty')" :image-size="60" />
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Right } from '@element-plus/icons-vue'
import { getChangeLog } from '@/api/audit'

const props = defineProps({
  /** 双向开关 */
  modelValue: { type: Boolean, default: false },
  /** 数据表名（company/candidate/position） */
  tableName: { type: String, required: true },
  /** 记录主键 */
  recordId: { type: [Number, String], required: true },
})
defineEmits(['update:modelValue'])

const { t } = useI18n()

const loading = ref(false)
const logs = ref([])

/** 字段名中文化（按表分别映射，未映射的字段显示原名） */
const FIELD_LABELS = {
  company: {
    name: '客户名称', foreignName: '外文名', city: '城市', industry: '所属行业',
    level: '客户等级', clientStatus: '客户状态', ownerId: '负责人', mission: '使命',
    description: '描述', logo: 'Logo', poster: '海报', remark: '备注',
  },
  candidate: {
    name: '姓名', phone: '电话', email: '邮箱', currentCompany: '当前公司',
    currentPosition: '当前职位', education: '学历', city: '城市',
    source: '来源', ownerId: '负责人', remark: '备注', expectPosition: '期望职位',
    expectSalary: '期望薪资', yearsOfExperience: '工作年限',
  },
  position: {
    title: '职位名称', positionName: '职位名称', requirement: '要求', quantity: '招聘人数',
    salaryUp: '薪资上限', salaryDown: '薪资下限', city: '城市', state: '状态',
  },
}
function fieldLabel(field) {
  return FIELD_LABELS[props.tableName]?.[field] || field
}

/** 按时间（同一秒视为一次保存）分组 */
const groups = computed(() => {
  const map = new Map()
  for (const c of logs.value) {
    const time = String(c.create_time || '').replace('T', ' ').slice(0, 19)
    if (!map.has(time)) map.set(time, { time, items: [] })
    map.get(time).items.push(c)
  }
  return [...map.values()]
})

watch(() => props.modelValue, open => {
  if (open && props.recordId) {
    loading.value = true
    getChangeLog(props.tableName, props.recordId)
      .then(res => { logs.value = res || [] })
      .catch(() => { logs.value = [] })
      .finally(() => { loading.value = false })
  }
})
</script>

<style scoped>
.diff-row {
  padding: 6px 0;
  border-bottom: 1px solid var(--glass-line);
  margin-bottom: 4px;
}
.diff-field {
  font-size: 12px;
  font-weight: 600;
  color: var(--ink);
  margin-bottom: 3px;
}
.diff-values {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  flex-wrap: wrap;
}
.diff-old {
  color: var(--red-text);
  background: var(--red-soft, rgba(224, 68, 59, 0.08));
  border-radius: var(--r-xs);
  padding: 1px 6px;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.diff-arrow {
  color: var(--ink-faint);
}
.diff-new {
  color: var(--leaf-text, #2e7d32);
  background: rgba(46, 125, 50, 0.08);
  border-radius: var(--r-xs);
  padding: 1px 6px;
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.diff-operator {
  font-size: 11px;
  color: var(--ink-faint);
  margin-top: 2px;
}
</style>
