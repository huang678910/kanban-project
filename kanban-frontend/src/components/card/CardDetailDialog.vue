<template>
  <el-dialog :model-value="show" @update:model-value="handleClose" width="650px"
    :title="card?.title || '卡片详情'" top="5vh" destroy-on-close>
    <template v-if="card">
      <div class="detail-body">
        <div class="detail-main">
          <h3>{{ card.title }}</h3>
          <div class="detail-section">
            <h4>描述</h4>
            <div v-if="!editingDesc" class="desc-content" @click="isAdmin && startEditDesc()">
              <div v-if="card.descriptionMd" class="md-preview" v-html="renderedDescription"></div>
              <div v-else class="desc-placeholder">{{ isAdmin ? '点击添加描述...' : '无描述' }}</div>
            </div>
            <div v-else>
              <el-input v-model="editDesc" type="textarea" :rows="4" placeholder="支持Markdown语法" />
              <div style="margin-top:8px;display:flex;gap:8px">
                <el-button type="primary" size="small" @click="saveDesc">保存</el-button>
                <el-button size="small" @click="editingDesc = false">取消</el-button>
              </div>
            </div>
          </div>
          <div class="detail-section">
            <h4>评论</h4>
            <div v-for="c in comments" :key="c.id" class="comment-item">
              <strong>{{ getUserDisplayName(c.userId) }}</strong>
              <span class="comment-time">{{ c.createdAt?.substring(0,16) }}</span>
              <p>{{ c.content }}</p>
            </div>
            <div class="comment-form" v-if="canEdit">
              <el-input v-model="newComment" placeholder="添加评论..." size="small" />
              <el-button size="small" style="margin-top:6px" @click="addComment">发表</el-button>
            </div>
          </div>
        </div>
        <div class="detail-sidebar">
          <div class="sidebar-section">
            <h4>状态</h4>
            <el-select v-model="editStatus" @change="saveStatus" size="small" style="width:100%" :disabled="!canEdit">
              <el-option label="待办" value="TODO" />
              <el-option label="进行" value="IN_PROGRESS" />
              <el-option label="完成" value="DONE" />
              <el-option label="结束" value="CLOSED" />
            </el-select>
          </div>
          <div class="sidebar-section">
            <h4>优先级</h4>
            <el-select v-model="editPriority" @change="savePriority" size="small" style="width:100%" :disabled="!isAdmin">
              <el-option label="高" value="HIGH" />
              <el-option label="中" value="MEDIUM" />
              <el-option label="低" value="LOW" />
            </el-select>
          </div>
          <div class="sidebar-section">
            <h4>截止日期</h4>
            <el-input v-model="editDueDateStr" type="datetime-local" size="small" style="width:100%" @change="saveDueDate" :disabled="!isAdmin" />
          </div>
          <div class="sidebar-section">
            <h4>负责人</h4>
            <el-select v-model="editAssignee" @change="saveAssignee" size="small" style="width:100%" clearable placeholder="选择" :disabled="!isAdmin">
              <el-option v-for="m in boardStore.members" :key="m.userId" :label="m.displayName || ('用户#'+m.userId)" :value="m.userId" />
            </el-select>
          </div>
          <div class="sidebar-section">
            <h4>标签</h4>
            <div class="label-list">
              <span v-for="l in cardLabels" :key="l.id" class="label-chip" :style="{background:l.color}">
                {{ l.name }} <el-icon v-if="canEdit" class="label-remove" @click="removeLabel(l.id)"><Close /></el-icon>
              </span>
            </div>
            <div v-if="canEdit" style="display:flex;gap:4px;align-items:center">
              <el-input v-model="newLabelName" placeholder="标签名" size="small" style="width:80px" />
              <el-color-picker v-model="newLabelColor" size="small" />
              <el-button size="small" @click="addLabel">+</el-button>
            </div>
          </div>
          <div class="sidebar-section">
            <h4>附件</h4>
            <div v-for="a in attachments" :key="a.id" class="attachment-item">
              <span class="attachment-link" @click="downloadAttachment(a)">{{ a.filename }}</span>
              <el-button v-if="canEdit" size="small" text type="danger" @click="deleteAttachment(a.id)">删除</el-button>
            </div>
            <el-upload v-if="canEdit" :http-request="customUpload" :show-file-list="false">
              <el-button size="small" text>上传附件</el-button>
            </el-upload>
          </div>
          <div class="sidebar-section">
            <el-button v-if="canEdit" type="danger" size="small" @click="deleteCard" style="width:100%">删除卡片</el-button>
          </div>
        </div>
      </div>
    </template>
    <el-empty v-else description="加载中..." />
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useBoardStore } from '@/stores/board'
import { usePermission } from '@/composables/usePermission'
import { cardApi } from '@/api/card'
import { commentApi } from '@/api/comment'
import { attachmentApi } from '@/api/attachment'
import request from '@/api/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { marked } from 'marked'

const props = defineProps({ cardId: [Number, String], visible: Boolean })
const emit = defineEmits(['close'])

const { canEdit, isAdmin } = usePermission()
const boardStore = useBoardStore()

// Local dialog state
const show = ref(false)
watch(() => props.visible, (v) => { show.value = v })

function handleClose(val) {
  if (!val) {
    show.value = false
    emit('close')
  }
}

// Data
const card = ref(null)
const comments = ref([])
const attachments = ref([])
const cardLabels = ref([])

// Edit state
const editingDesc = ref(false)
const editDesc = ref('')
const editStatus = ref('TODO')
const editPriority = ref('MEDIUM')
const editDueDate = ref(null)
const editDueDateStr = ref('')
const editAssignee = ref(null)
const newComment = ref('')
const newLabelName = ref('')
const newLabelColor = ref('#409eff')

async function customUpload(options) {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await request.post(`/api/cards/${props.cardId}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    options.onSuccess(res)
    await onUploadSuccess()
  } catch (e) {
    options.onError(e)
    ElMessage.error('上传失败')
  }
}

const renderedDescription = computed(() => {
  if (!card.value?.descriptionMd) return ''
  return marked.parse(card.value.descriptionMd)
})

function getUserDisplayName(userId) {
  const member = boardStore.members?.find(m => m.userId === userId)
  if (member) return member.displayName || member.username || `用户#${userId}`
  return `用户#${userId}`
}

// Load card data
watch(() => props.cardId, async (id) => {
  if (!id || id === 0) return
  try {
    const [cardRes, commentsRes, attachRes, labelRes] = await Promise.all([
      cardApi.getCard(id),
      commentApi.getComments(id),
      attachmentApi.getAttachments(id),
      request.get(`/api/cards/${id}/labels`)
    ])
    card.value = cardRes.data
    comments.value = commentsRes.data
    attachments.value = attachRes.data
    cardLabels.value = labelRes.data || []
    editStatus.value = card.value.status || 'TODO'
    editPriority.value = card.value.priority || 'MEDIUM'
    editDueDate.value = card.value.dueDate ? new Date(card.value.dueDate) : null
    editDueDateStr.value = card.value.dueDate ? new Date(card.value.dueDate).toISOString().slice(0, 16) : ''
    editAssignee.value = card.value.assigneeId
  } catch { /* */ }
}, { immediate: true })

// Save functions
function startEditDesc() { editingDesc.value = true; editDesc.value = card.value.descriptionMd || '' }
async function saveDesc() {
  await cardApi.updateCard(props.cardId, { title: card.value.title, descriptionMd: editDesc.value, priority: editPriority.value, dueDate: editDueDate.value, assigneeId: editAssignee.value })
  card.value.descriptionMd = editDesc.value
  editingDesc.value = false
}
async function saveStatus() {
  await request.put(`/api/cards/${props.cardId}/status`, { status: editStatus.value })
  card.value.status = editStatus.value
  boardStore.fetchBoardData(boardStore.currentBoard?.id)
}
async function savePriority() {
  await cardApi.updateCard(props.cardId, { title: card.value.title, descriptionMd: card.value.descriptionMd, priority: editPriority.value, dueDate: editDueDate.value, assigneeId: editAssignee.value })
  card.value.priority = editPriority.value
  boardStore.fetchBoardData(boardStore.currentBoard?.id)
}
async function saveDueDate() {
  const dateVal = editDueDateStr.value ? new Date(editDueDateStr.value).toISOString() : null
  await cardApi.updateCard(props.cardId, { title: card.value.title, descriptionMd: card.value.descriptionMd, priority: editPriority.value, dueDate: dateVal, assigneeId: editAssignee.value })
  card.value.dueDate = dateVal
  boardStore.fetchBoardData(boardStore.currentBoard?.id)
}
async function saveAssignee() {
  await cardApi.updateCard(props.cardId, { title: card.value.title, descriptionMd: card.value.descriptionMd, priority: editPriority.value, dueDate: editDueDate.value, assigneeId: editAssignee.value })
  card.value.assigneeId = editAssignee.value
  boardStore.fetchBoardData(boardStore.currentBoard?.id)
}
async function addComment() {
  if (!newComment.value.trim()) return
  await commentApi.addComment(props.cardId, { content: newComment.value })
  ElMessage.success('已添加')
  newComment.value = ''
  comments.value = (await commentApi.getComments(props.cardId)).data
}
async function addLabel() {
  if (!newLabelName.value.trim()) return
  await request.post(`/api/cards/${props.cardId}/labels`, { name: newLabelName.value, color: newLabelColor.value })
  const res = await request.get(`/api/cards/${props.cardId}/labels`)
  cardLabels.value = res.data || []
  newLabelName.value = ''
}
async function removeLabel(labelId) {
  await request.delete(`/api/card-labels/${labelId}`)
  const res = await request.get(`/api/cards/${props.cardId}/labels`)
  cardLabels.value = res.data || []
}
async function onUploadSuccess() {
  attachments.value = (await attachmentApi.getAttachments(props.cardId)).data
}
function onUploadError() { ElMessage.error('上传失败') }
async function downloadAttachment(att) {
  const token = localStorage.getItem('kanban_token')
  const res = await fetch(`/api/attachments/${att.id}/download`, {
    headers: { Authorization: `Bearer ${token}` }
  })
  if (!res.ok) { ElMessage.error('下载失败'); return }
  const blob = await res.blob()
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = att.filename
  a.click()
  URL.revokeObjectURL(url)
}
async function deleteAttachment(id) {
  await attachmentApi.deleteAttachment(id)
  attachments.value = (await attachmentApi.getAttachments(props.cardId)).data
}
async function deleteCard() {
  try {
    await ElMessageBox.confirm('确定删除此卡片？', '确认', { type: 'warning' })
    await cardApi.deleteCard(props.cardId)
    ElMessage.success('已删除')
    emit('close')
    boardStore.fetchBoardData(boardStore.currentBoard?.id)
  } catch { /* */ }
}
</script>

<style scoped>
.detail-body { display: flex; gap: 24px; }
.detail-main { flex: 1; min-width: 0; }
.detail-sidebar { width: 200px; flex-shrink: 0; }
.detail-section { margin-bottom: 20px; }
.detail-section h4 { font-size: 12px; color: #909399; margin-bottom: 6px; text-transform: uppercase; }
.desc-placeholder { padding: 12px; background: #f5f7fa; border-radius: 4px; cursor: pointer; color: #909399; min-height: 40px; }
.md-preview { padding: 12px; background: #f5f7fa; border-radius: 4px; min-height: 30px; cursor: pointer; }
.comment-item { padding: 8px 0; border-bottom: 1px solid #f0f0f0; }
.comment-item p { margin: 4px 0 0; font-size: 14px; }
.comment-time { font-size: 12px; color: #909399; margin-left: 8px; }
.comment-form { margin-top: 8px; }
.sidebar-section { margin-bottom: 16px; }
.sidebar-section h4 { font-size: 12px; color: #909399; margin-bottom: 6px; }
.label-list { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 4px; }
.label-chip { display: inline-flex; align-items: center; gap: 2px; padding: 2px 8px; border-radius: 3px; font-size: 11px; color: white; }
.label-remove { cursor: pointer; font-size: 12px; }
.attachment-link { color: #409eff; cursor: pointer; text-decoration: underline; }
.attachment-link:hover { color: #337ecc; }
.attachment-item { display: flex; justify-content: space-between; align-items: center; font-size: 13px; padding: 2px 0; }
</style>
