<template>
  <div class="not-found">
    <div class="code">404</div>
    <div class="text">页面不存在</div>
    <el-button type="primary" @click="$router.push('/workbench')">回到工作台</el-button>
  </div>
</template>

<style scoped>
.not-found {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
}
.code {
  font-size: 64px;
  font-weight: 700;
  color: #909399;
}
.text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}
</style>
