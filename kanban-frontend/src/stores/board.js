import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { boardApi } from '@/api/board'
import { listApi } from '@/api/list'
import { cardApi } from '@/api/card'

export const useBoardStore = defineStore('board', () => {
  const boards = ref([])
  const currentBoard = ref(null)
  const lists = ref([])
  const cards = ref([])  // All cards across all lists
  const members = ref([])

  // --- Board CRUD ---
  async function fetchBoards() {
    const res = await boardApi.getBoards()
    boards.value = res.data
  }

  async function fetchBoard(boardId) {
    const res = await boardApi.getBoard(boardId)
    currentBoard.value = res.data
    await fetchBoardData(boardId)
  }

  async function fetchBoardData(boardId) {
    const [listsRes, membersRes] = await Promise.all([
      listApi.getLists(boardId),
      boardApi.getMembers(boardId)
    ])
    lists.value = listsRes.data
    members.value = membersRes.data

    // Fetch cards for all lists
    const cardResults = await Promise.all(
      lists.value.map(l => cardApi.getCards(l.id))
    )
    cards.value = cardResults.flatMap(r => r.data)
  }

  function getCardsByListId(listId) {
    return cards.value
      .filter(c => c.listId === listId)
      .sort((a, b) => a.position - b.position)
  }

  // --- Real-time event handling ---
  function handleRealtimeEvent(event) {
    if (!currentBoard.value?.id) return
    switch (event.type) {
      case 'CARD_CREATED':
      case 'CARD_UPDATED':
      case 'CARD_MOVED':
      case 'CARD_DELETED':
        fetchBoardData(currentBoard.value.id)
        break
      default:
        break
    }
  }

  return {
    boards, currentBoard, lists, cards, members,
    fetchBoards, fetchBoard, fetchBoardData,
    getCardsByListId, handleRealtimeEvent
  }
})
