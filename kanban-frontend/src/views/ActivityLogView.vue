<template>
  <div class="page">
    <header class="app-header">
      <div class="header-left">
        <el-button text @click="$router.push(`/b/${boardId}`)"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="page-title">活动日志</span>
      </div>
    </header>
    <div class="page-content">
      <el-timeline v-if="activities.length > 0">
        <el-timeline-item v-for="log in activities" :key="log.id"
          :timestamp="log.createdAt?.substring(0, 19)" placement="top">
          <el-card shadow="hover">
            <p>{{ log.detail }}</p>
            <span style="font-size:12px;color:#909399">用户 #{{ log.userId }} · {{ log.action }}</span>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无活动" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { boardApi } from '@/api/board'
import { ElMessage } from 'element-plus'

const route = useRoute()
const boardId = computed(() => route.params.boardId)
const activities = ref([])

onMounted(async () => {
  try { activities.value = (await boardApi.getActivities(boardId.value)).data } catch {
    ElMessage.error('加载活动日志失败')
  }
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f2f5; }
.page-content { max-width: 700px; margin: 24px auto; padding: 0 16px; }
.page-title { font-weight: 600; margin-left: 8px; }
.header-left { display: flex; align-items: center; }
</style>
