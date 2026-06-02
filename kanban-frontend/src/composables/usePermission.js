import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useBoardStore } from '@/stores/board'

export function usePermission() {
  const authStore = useAuthStore()
  const boardStore = useBoardStore()

  const currentMember = computed(() =>
    boardStore.members.find(m => m.userId === authStore.user?.id)
  )

  const isAdmin = computed(() => currentMember.value?.role === 'ADMIN')
  const isMember = computed(() => currentMember.value?.role === 'MEMBER' || isAdmin.value)
  const isViewer = computed(() => currentMember.value?.role === 'VIEWER')

  // Permissions
  const canEdit = computed(() => isMember.value)              // Admin + Member: create/edit cards
  const canManageMembers = computed(() => isAdmin.value)       // Admin only: manage members
  const canManageLists = computed(() => isAdmin.value)         // Admin only: create/delete lists
  const canEditBoardStatus = computed(() => isAdmin.value)     // Admin only: change board status
  const canEditListPriority = computed(() => isAdmin.value)    // Admin only: change list priority
  const canEditCardPriority = computed(() => isAdmin.value)    // Admin only: change card priority
  const canEditStatus = computed(() => isMember.value)         // Admin + Member: change list/card status

  return {
    currentMember, isAdmin, isMember, isViewer,
    canEdit, canManageMembers, canManageLists,
    canEditBoardStatus, canEditListPriority, canEditCardPriority, canEditStatus
  }
}
