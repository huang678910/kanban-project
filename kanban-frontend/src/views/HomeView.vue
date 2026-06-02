<template>
  <div class="home-page">
    <header class="app-header">
      <span class="logo">📋 协作任务板</span>
      <div class="header-right">
        <el-button @click="$router.push('/search')" text>
          <el-icon><Search /></el-icon> 搜索
        </el-button>
        <el-button @click="$router.push('/profile')" text>{{ authStore.user?.displayName }}</el-button>
        <el-button @click="handleLogout" text type="danger">退出</el-button>
      </div>
    </header>
    <div class="home-content">
      <div class="home-title">
        <h2>我的看板</h2>
        <el-button type="primary" @click="showCreate = true">
          <el-icon><Plus /></el-icon> 创建看板
        </el-button>
      </div>
      <div class="board-grid" v-if="boardStore.boards.length > 0">
        <div v-for="board in boardStore.boards" :key="board.id"
          class="board-card" @click="$router.push(`/b/${board.id}`)">
          <h3>{{ board.name }}</h3>
          <p>{{ board.description || '暂无描述' }}</p>
          <div class="board-status-row" @click.stop>
            <el-select :model-value="board.status || 'TODO'" @change="(v) => updateBoardStatus(board.id, v)" size="small" style="width:100%">
              <el-option label="📋 待办" value="TODO" />
              <el-option label="🔄 进行" value="IN_PROGRESS" />
              <el-option label="✅ 完成" value="DONE" />
              <el-option label="🔒 结束" value="CLOSED" />
            </el-select>
          </div>
          <div class="board-meta">
            <span>{{ board.createdAt?.substring(0, 10) }}</span>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, board)">
              <el-button text @click.stop><el-icon><MoreFilled /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="delete" divided><span style="color:#f56c6c">删除</span></el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
      <el-empty v-else description="还没有看板，点击上方按钮创建一个吧" />
    </div>

    <!-- Create/Edit Board Dialog -->
    <el-dialog v-model="showCreate" :title="editingBoard ? '编辑看板' : '创建看板'" width="450px">
      <el-form :model="boardForm" label-position="top">
        <el-form-item label="看板名称" required>
          <el-input v-model="boardForm.name" placeholder="输入看板名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="boardForm.description" type="textarea" :rows="3" placeholder="输入看板描述（可选）" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="handleSaveBoard">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useBoardStore } from '@/stores/board'
import { boardApi } from '@/api/board'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const boardStore = useBoardStore()

const showCreate = ref(false)
const editingBoard = ref(null)
const boardForm = reactive({ name: '', description: '' })

onMounted(() => boardStore.fetchBoards())

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

function handleCommand(cmd, board) {
  if (cmd === 'edit') {
    editingBoard.value = board
    boardForm.name = board.name
    boardForm.description = board.description || ''
    showCreate.value = true
  } else if (cmd === 'delete') {
    ElMessageBox.confirm('确定删除该看板？此操作不可恢复！', '警告', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    }).then(async () => {
      await boardApi.deleteBoard(board.id)
      ElMessage.success('已删除')
      boardStore.fetchBoards()
    }).catch(() => {})
  }
}

async function updateBoardStatus(boardId, status) {
  await request.put(`/api/boards/${boardId}/status`, { status })
  boardStore.fetchBoards()
}

async function handleSaveBoard() {
  if (!boardForm.name.trim()) {
    ElMessage.warning('请输入看板名称')
    return
  }
  if (editingBoard.value) {
    await boardApi.updateBoard(editingBoard.value.id, boardForm)
  } else {
    await boardApi.createBoard(boardForm)
  }
  ElMessage.success(editingBoard.value ? '已更新' : '创建成功')
  showCreate.value = false
  editingBoard.value = null
  boardForm.name = ''
  boardForm.description = ''
  boardStore.fetchBoards()
}
</script>

<style scoped>
.home-page { min-height: 100vh; background: #f0f2f5; }
.home-content { max-width: 1100px; margin: 0 auto; padding-top: 24px; }
.home-title { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding: 0 24px; }
.home-title h2 { font-size: 20px; }
.board-meta { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; font-size: 12px; color: #909399; }
.board-card h3 { font-size: 16px; margin-bottom: 4px; }
.board-card p { color: #909399; font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.board-status-row { margin: 8px 0; }
.header-right { display: flex; align-items: center; gap: 8px; }
</style>
