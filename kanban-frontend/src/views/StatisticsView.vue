<template>
  <div class="page">
    <header class="app-header">
      <div class="header-left">
        <el-button text @click="$router.push(`/b/${boardId}`)"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="page-title">看板统计</span>
      </div>
    </header>
    <div class="page-content" v-loading="loading">
      <!-- Summary cards -->
      <el-row :gutter="16" style="margin-bottom:24px">
        <el-col :span="6" v-for="s in summaryCards" :key="s.label">
          <el-card shadow="hover">
            <div style="text-align:center">
              <div style="font-size:28px;font-weight:700;color:#409eff">{{ s.value }}</div>
              <div style="font-size:13px;color:#909399;margin-top:4px">{{ s.label }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- Per-list breakdown -->
      <el-card header="列表统计">
        <div v-for="stat in stats?.listStats || []" :key="stat.listId" style="margin-bottom:12px">
          <div style="display:flex;justify-content:space-between;margin-bottom:4px">
            <span>{{ stat.listName }}</span>
            <span>{{ stat.cardCount }} 张卡片</span>
          </div>
          <el-progress :percentage="getListPercentage(stat.cardCount)"
            :color="getProgressColor(stat.listId)" />
        </div>
        <el-empty v-if="!stats?.listStats?.length" description="暂无列表" />
      </el-card>
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
const stats = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await boardApi.getStatistics(boardId.value)
    stats.value = res.data
  } catch {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
})

function getListPercentage(cardCount) {
  const total = stats.value?.totalCards || 1
  return Math.round(cardCount / total * 100)
}

function getProgressColor(listId) {
  const colors = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399']
  const listStats = stats.value?.listStats || []
  const idx = listStats.findIndex(l => l.listId === listId)
  return colors[idx >= 0 ? idx % colors.length : 0]
}

const summaryCards = computed(() => {
  const s = stats.value
  if (!s) return []
  return [
    { label: '总卡片数', value: s.totalCards || 0 },
    { label: '高优先级', value: s.highPriority || 0 },
    { label: '已完成', value: s.done || 0 },
    { label: '已逾期', value: s.overdue || 0 }
  ]
})
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f2f5; }
.page-content { max-width: 900px; margin: 24px auto; padding: 0 16px; }
.page-title { font-weight: 600; margin-left: 8px; }
.header-left { display: flex; align-items: center; }
</style>
