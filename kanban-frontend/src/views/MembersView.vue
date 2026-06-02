<template>
  <div class="page">
    <header class="app-header">
      <div class="header-left">
        <el-button text @click="$router.push(`/b/${boardId}`)"><el-icon><ArrowLeft /></el-icon></el-button>
        <span class="page-title">成员管理</span>
      </div>
    </header>
    <div class="page-content">
      <el-card>
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>看板成员</span>
            <el-button v-if="canManageMembers" type="primary" size="small" @click="showAdd = true">
              <el-icon><Plus /></el-icon> 添加成员
            </el-button>
          </div>
        </template>
        <el-table :data="boardStore.members" style="width:100%">
          <el-table-column label="用户名" prop="username" />
          <el-table-column label="显示名称" prop="displayName" />
          <el-table-column label="角色" prop="role">
            <template #default="{ row }">
              <el-tag v-if="row.role === 'ADMIN'" type="danger">Admin</el-tag>
              <el-tag v-else-if="row.role === 'MEMBER'" type="success">Member</el-tag>
              <el-tag v-else type="info">Viewer</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="加入时间" prop="joinedAt" />
          <el-table-column label="操作" v-if="canManageMembers">
            <template #default="{ row }">
              <template v-if="row.role !== 'ADMIN'">
                <el-button size="small" @click="changeRole(row, 'MEMBER')" v-if="row.role !== 'MEMBER'">设为Member</el-button>
                <el-button size="small" @click="changeRole(row, 'VIEWER')" v-if="row.role !== 'VIEWER'">设为Viewer</el-button>
                <el-button size="small" type="danger" @click="removeMember(row)">移除</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-dialog v-model="showAdd" title="添加成员" width="400px">
        <el-form :model="addForm">
          <el-form-item label="用户邮箱">
            <el-input v-model="addForm.email" placeholder="输入已注册用户的邮箱" />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="addForm.role">
              <el-option label="Member（可管理卡片）" value="MEMBER" />
              <el-option label="Viewer（只读）" value="VIEWER" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showAdd = false">取消</el-button>
          <el-button type="primary" @click="handleAddMember">添加</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBoardStore } from '@/stores/board'
import { usePermission } from '@/composables/usePermission'
import { boardApi } from '@/api/board'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const boardStore = useBoardStore()
const { canManageMembers } = usePermission()

const boardId = computed(() => route.params.boardId)
const showAdd = ref(false)
const addForm = reactive({ email: '', role: 'MEMBER' })

onMounted(() => boardStore.fetchBoard(boardId.value))

async function handleAddMember() {
  if (!addForm.email.trim()) { ElMessage.warning('请输入用户邮箱'); return }
  await boardApi.addMember(boardId.value, addForm)
  ElMessage.success('成员已添加')
  showAdd.value = false
  addForm.email = ''
  boardStore.fetchBoard(boardId.value)
}

async function changeRole(member, role) {
  await boardApi.changeRole(member.id, role)
  ElMessage.success('角色已更新')
  boardStore.fetchBoard(boardId.value)
}

async function removeMember(member) {
  await boardApi.removeMember(member.id)
  ElMessage.success('成员已移除')
  boardStore.fetchBoard(boardId.value)
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f0f2f5; }
.page-content { max-width: 800px; margin: 24px auto; padding: 0 16px; }
.page-title { font-weight: 600; margin-left: 8px; }
.header-left { display: flex; align-items: center; }
</style>
