import request from './request'

export const listApi = {
  getLists: (boardId) => request.get(`/api/boards/${boardId}/lists`),
  createList: (boardId, data) => request.post(`/api/boards/${boardId}/lists`, data),
  updateList: (listId, data) => request.put(`/api/lists/${listId}`, data),
  deleteList: (listId) => request.delete(`/api/lists/${listId}`)
}
