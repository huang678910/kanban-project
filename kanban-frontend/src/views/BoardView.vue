<template>
  <div class="board-page">
    <header class="app-header">
      <div class="header-left">
        <el-button text @click="$router.push('/')"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="board-name">{{ boardStore.currentBoard?.name }}</span>
        <el-select v-model="boardStatus" @change="updateBoardStatus" size="small" style="width:110px;margin-left:12px" :disabled="!canEditBoardStatus">
          <el-option label="📋 待办" value="TODO" />
          <el-option label="🔄 进行" value="IN_PROGRESS" />
          <el-option label="✅ 完成" value="DONE" />
          <el-option label="🔒 结束" value="CLOSED" />
        </el-select>
      </div>
      <div class="header-right">
        <el-button type="primary" plain size="small" @click="$router.push(`/b/${boardId}/members`)">
          <el-icon><User /></el-icon> 成员
        </el-button>
        <el-button type="primary" plain size="small" @click="$router.push(`/b/${boardId}/activities`)">
          <el-icon><Clock /></el-icon> 动态
        </el-button>
        <el-button type="primary" plain size="small" @click="$router.push(`/b/${boardId}/statistics`)">
          <el-icon><DataAnalysis /></el-icon> 统计
        </el-button>
        <el-divider direction="vertical" />
        <span style="font-size:13px">{{ authStore.user?.displayName }}</span>
      </div>
    </header>

    <div class="board-lists" ref="boardContainer" v-loading="boardLoading">
      <div v-for="list in boardStore.lists" :key="list.id" class="list-column">
        <div class="list-header">
          <div class="list-title-row">
            <span class="list-title" @dblclick="startEditList(list)">
              <template v-if="editingListId === list.id">
                <el-input v-model="editListName" size="small" @blur="finishEditList(list)"
                  @keyup.enter="finishEditList(list)" ref="editInput" />
              </template>
              <template v-else>{{ list.name }}</template>
            </span>
            <el-select v-if="isMember" :model-value="list.status || 'TODO'" @change="(v) => updateListStatus(list.id, v)" size="small" style="width:80px" :disabled="!canEditStatus">
              <el-option label="待办" value="TODO" />
              <el-option label="进行" value="IN_PROGRESS" />
              <el-option label="完成" value="DONE" />
              <el-option label="结束" value="CLOSED" />
            </el-select>
            <el-select v-if="isMember" :model-value="list.priority || 'MEDIUM'" @change="(v) => updateListPriority(list.id, v)" size="small" style="width:70px;margin-top:4px" :disabled="!canEditListPriority">
              <el-option label="🔴高" value="HIGH" />
              <el-option label="🟡中" value="MEDIUM" />
              <el-option label="🟢低" value="LOW" />
            </el-select>
          </div>
          <el-dropdown trigger="click" v-if="canManageLists">
            <el-button text size="small"><el-icon><MoreFilled /></el-icon></el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="startEditList(list)">重命名</el-dropdown-item>
                <el-dropdown-item @click="handleDeleteList(list)" style="color:#f56c6c">删除列表</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <div class="list-cards">
          <VueDraggable
            v-model="listCards[list.id]"
            group="cards"
            item-key="id"
            :animation="200"
            ghost-class="card-ghost"
            @change="(evt) => onDragChange(evt, list.id)"
          >
            <template #item="{ element }">
              <div
                :class="['card-item', `priority-${element.priority?.toLowerCase()}`]"
                @click="openCardDetail(element.id)"
              >
                <div class="card-labels" v-if="element.labels?.length">
                  <span v-for="l in element.labels" :key="l.id"
                    class="card-label-tag" :style="{ background: l.color }">{{ l.name }}</span>
                </div>
                <div class="card-title">{{ element.title }}</div>
                <div class="card-footer">
                  <span :class="'card-status-badge status-' + (element.status || 'TODO').toLowerCase()">
                    {{ element.status === 'CLOSED' ? '🔒 结束' : element.status === 'DONE' ? '✅ 完成' : element.status === 'IN_PROGRESS' ? '🔄 进行' : '📋 待办' }}
                  </span>
                  <span v-if="element.priority" class="card-priority-badge">
                    {{ element.priority === 'HIGH' ? '🔴' : element.priority === 'MEDIUM' ? '🟡' : '🟢' }}
                  </span>
                  <span v-if="element.dueDate" :class="getDueClass(element.dueDate)">
                    <el-icon><Clock /></el-icon> {{ formatDate(element.dueDate) }}
                  </span>
                </div>
              </div>
            </template>
          </VueDraggable>
        </div>

        <div v-if="canEdit" class="list-footer">
          <el-button v-if="!showCardForm[list.id]" text size="small"
            @click="showCardForm[list.id] = true" style="width:100%">
            <el-icon><Plus /></el-icon> 添加卡片
          </el-button>
          <div v-else class="card-create-form">
            <el-input v-model="newCardTitle[list.id]" placeholder="输入卡片标题" size="small"
              @keyup.enter="handleAddCard(list.id)"
              @keyup.escape="showCardForm[list.id] = false; newCardTitle[list.id] = ''" />
            <div class="form-actions">
              <el-button type="primary" size="small" @click="handleAddCard(list.id)">添加</el-button>
              <el-button size="small" @click="showCardForm[list.id] = false; newCardTitle[list.id] = ''">取消</el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="canManageLists" class="list-create">
        <el-button v-if="!showListForm" text @click="showListForm = true" style="width:260px;height:40px">
          <el-icon><Plus /></el-icon> 添加列表
        </el-button>
        <div v-else class="list-create-form" style="width:260px">
          <el-input v-model="newListName" placeholder="输入列表名称" size="small"
            @keyup.enter="handleAddList"
            @keyup.escape="showListForm = false; newListName = ''" />
          <div class="form-actions">
            <el-button type="primary" size="small" @click="handleAddList">添加</el-button>
            <el-button size="small" @click="showListForm = false; newListName = ''">取消</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Card Detail Dialog -->
    <CardDetailDialog :card-id="detailCardId || 0"
      :visible="!!detailCardId" @close="detailCardId = null" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import VueDraggable from 'vuedraggable'
import { useAuthStore } from '@/stores/auth'
import { useBoardStore } from '@/stores/board'
import { usePermission } from '@/composables/usePermission'
import { useWebSocket } from '@/composables/useWebSocket'
import { cardApi } from '@/api/card'
import { listApi } from '@/api/list'
import request from '@/api/request'
import CardDetailDialog from '@/components/card/CardDetailDialog.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const boardStore = useBoardStore()
const { canEdit, canManageLists, isAdmin, isMember, canEditBoardStatus, canEditStatus, canEditListPriority } = usePermission()
const { connect, subscribeToBoard, unsubscribeFromBoard } = useWebSocket()

const boardId = computed(() => route.params.boardId)
const boardContainer = ref(null)
const detailCardId = ref(null)
const boardLoading = ref(false)
const boardStatus = ref('TODO')
const showListForm = ref(false)
const newListName = ref('')
const editingListId = ref(null)
const editListName = ref('')
const editInput = ref(null)

// Card creation state per list
const showCardForm = reactive({})
const newCardTitle = reactive({})

// Local cards per list for vuedraggable reactivity
const listCards = reactive({})

// Watch store changes and update local cards
watch(() => boardStore.cards, (newCards) => {
  boardStore.lists.forEach(list => {
    const listCardIds = newCards
      .filter(c => c.listId === list.id)
      .sort((a, b) => a.position - b.position)
    listCards[list.id] = listCardIds
  })
}, { deep: true })

// Drag state tracking
let isDraggingLocal = false

onMounted(async () => {
  boardLoading.value = true
  try {
    await boardStore.fetchBoard(boardId.value)
    boardStatus.value = boardStore.currentBoard?.status || 'ACTIVE'
  } catch {
    ElMessage.error('加载看板数据失败')
  } finally {
    boardLoading.value = false
  }
  connect()
  subscribeToBoard(boardId.value, (message) => {
    try {
      const event = JSON.parse(message.body)
      boardStore.handleRealtimeEvent(event)
    } catch (e) { /* ignore parse errors */ }
  })
})

onUnmounted(() => unsubscribeFromBoard(boardId.value))

function openCardDetail(cardId) { detailCardId.value = cardId }

function startEditList(list) {
  editingListId.value = list.id
  editListName.value = list.name
  nextTick(() => {
    const input = document.querySelector('.list-title .el-input__inner')
    if (input) input.focus()
  })
}

async function finishEditList(list) {
  if (editListName.value.trim() && editListName.value !== list.name) {
    await listApi.updateList(list.id, { name: editListName.value })
    boardStore.fetchBoardData(boardId.value)
  }
  editingListId.value = null
}

async function handleDeleteList(list) {
  try {
    await ElMessageBox.confirm(`确定删除列表 "${list.name}" 及其所有卡片？`, '确认删除', {
      type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消'
    })
    await listApi.deleteList(list.id)
    ElMessage.success('列表已删除')
    boardStore.fetchBoardData(boardId.value)
  } catch { /* cancelled */ }
}

async function handleAddList() {
  if (!newListName.value.trim()) return
  await listApi.createList(boardId.value, { name: newListName.value })
  ElMessage.success('列表已创建')
  newListName.value = ''
  showListForm.value = false
  boardStore.fetchBoardData(boardId.value)
}

async function handleAddCard(listId) {
  if (!newCardTitle[listId]?.trim()) return
  await cardApi.createCard(listId, { title: newCardTitle[listId] })
  ElMessage.success('卡片已创建')
  newCardTitle[listId] = ''
  showCardForm[listId] = false
  boardStore.fetchBoardData(boardId.value)
}

function getDueClass(dueDate) {
  const now = new Date()
  const due = new Date(dueDate)
  const hoursLeft = (due - now) / (1000 * 60 * 60)
  if (hoursLeft < 0) return 'card-due-overdue'
  if (hoursLeft < 24) return 'card-due-soon'
  return ''
}

async function onDragChange(evt, targetListId) {
  if (evt.added) {
    const { element, newIndex } = evt.added
    const cards = listCards[targetListId]
    const prevCard = cards[newIndex - 1] || null
    const nextCard = cards[newIndex + 1] || null

    let newPosition
    if (!prevCard && !nextCard) newPosition = 65536.0
    else if (!prevCard) newPosition = nextCard.position / 2.0
    else if (!nextCard) newPosition = prevCard.position + 65536.0
    else newPosition = (prevCard.position + nextCard.position) / 2.0

    try {
      await cardApi.moveCard(element.id, targetListId, newPosition)
    } catch { boardStore.fetchBoardData(boardId.value) }
  } else if (evt.moved) {
    const { element, newIndex } = evt.moved
    const cards = listCards[targetListId]
    const prevCard = cards[newIndex - 1] || null
    const nextCard = cards[newIndex + 1] || null

    let newPosition
    if (!prevCard && !nextCard) newPosition = 65536.0  // Only card in list
    else if (!prevCard) newPosition = nextCard.position / 2.0
    else if (!nextCard) newPosition = prevCard.position + 65536.0
    else newPosition = (prevCard.position + nextCard.position) / 2.0

    try {
      await cardApi.moveCard(element.id, targetListId, newPosition)
    } catch { boardStore.fetchBoardData(boardId.value) }
  }
}

async function updateBoardStatus(status) {
  await request.put(`/api/boards/${boardId.value}/status`, { status })
  boardStatus.value = status
}

async function updateListStatus(listId, status) {
  await request.put(`/api/lists/${listId}/status`, { status })
  boardStore.fetchBoardData(boardId.value)
}

async function updateListPriority(listId, priority) {
  await request.put(`/api/lists/${listId}/priority`, { priority })
  boardStore.fetchBoardData(boardId.value)
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>

<style scoped>
.board-page { height: 100vh; display: flex; flex-direction: column; background: #1a1a2e; }
.board-page .app-header { background: #16213e; border-color: #0f3460; }
.board-page .app-header .logo,
.board-page .app-header span { color: #e0e0e0; }
.board-page .app-header .el-button { color: #c0d0ff; }
.board-page .app-header .el-button:hover { color: #ffffff; background: rgba(255,255,255,0.1); }
.board-name { font-weight: 600; margin-left: 8px; }
.header-left { display: flex; align-items: center; }
.board-lists { background: #1a1a2e; }
.list-column { background: #16213e; }
.list-header { padding: 4px 8px 8px; }
.list-title-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.list-title { font-weight: 600; font-size: 14px; color: #e0e0e0; cursor: pointer; }
.list-cards { flex: 1; overflow-y: auto; min-height: 20px; padding: 4px 0; }
.card-item { background: #0f3460; color: #e0e0e0; }
.card-item:hover { background: #1a4a8a; }
.card-title { font-size: 14px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 6px; font-size: 12px; color: #a0a0a0; }
.card-labels { display: flex; gap: 4px; margin-bottom: 4px; flex-wrap: wrap; }
.card-label-tag { font-size: 10px; padding: 0 6px; border-radius: 2px; color: white; }
.list-footer { padding: 4px 0; }
.form-actions { display: flex; gap: 4px; margin-top: 6px; }
.card-create-form { padding: 4px 0; }
.list-create { flex-shrink: 0; }
.list-create-form { background: #16213e; padding: 8px; border-radius: 8px; }
.card-ghost { opacity: 0.4; background: #533483 !important; }
.list-column .el-button { color: #a0a0a0; }
</style>
