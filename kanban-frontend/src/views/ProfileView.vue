<template>
  <div class="page">
    <header class="app-header">
      <div class="header-left">
        <el-button text @click="$router.push('/')"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="page-title">个人设置</span>
      </div>
    </header>
    <div class="page-content">
      <el-card>
        <el-descriptions title="用户信息" :column="1" border>
          <el-descriptions-item label="用户名">{{ authStore.user?.username }}</el-descriptions-item>
          <el-descriptions-item label="显示名称">{{ authStore.user?.displayName }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ authStore.user?.email || '未设置' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f2f5; }
.page-content { max-width: 500px; margin: 24px auto; padding: 0 16px; }
.page-title { font-weight: 600; margin-left: 8px; }
.header-left { display: flex; align-items: center; }
</style>
