<template>
  <div class="page">
    <header class="app-header">
      <div class="header-left">
        <el-button text @click="$router.push('/')"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="page-title">搜索</span>
      </div>
      <el-input v-model="query" placeholder="搜索卡片..." style="width:300px"
        @keyup.enter="doSearch" clearable />
    </header>
    <div class="page-content">
      <el-empty v-if="!query" description="输入关键词开始搜索" />
      <el-empty v-else-if="results.length === 0" description="未找到匹配的卡片" />
      <div v-else>
        <h3 style="margin-bottom:16px">找到 {{ results.length }} 个结果</h3>
        <el-card v-for="card in results" :key="card.id" style="margin-bottom:8px;cursor:pointer"
          :class="{ 'disabled-card': !card.boardId }"
          @click="card.boardId && $router.push(`/b/${card.boardId}`)">
          <h4>{{ card.title }}</h4>
          <div style="display:flex;gap:8px;margin-bottom:4px">
            <el-tag v-if="card.listName" size="small" type="info">{{ card.listName }}</el-tag>
            <el-tag v-if="!card.boardId" size="small" type="warning">无权访问</el-tag>
          </div>
          <p style="color:#909399;font-size:13px">{{ card.descriptionMd?.substring(0, 100) }}</p>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/api/request'
import { ElMessage } from 'element-plus'

const query = ref('')
const results = ref([])

async function doSearch() {
  if (!query.value.trim()) return
  try {
    const res = await request.get('/api/search', { params: { q: query.value } })
    results.value = res.data || []
  } catch (e) {
    ElMessage.error('搜索失败，请重试')
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f2f5; }
.page-content { max-width: 700px; margin: 24px auto; padding: 0 16px; }
.page-title { font-weight: 600; margin-left: 8px; }
.header-left { display: flex; align-items: center; }
</style>
