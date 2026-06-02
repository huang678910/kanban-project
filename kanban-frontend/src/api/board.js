import request from './request'

export const boardApi = {
  getBoards: () => request.get('/api/boards'),
  getBoard: (id) => request.get(`/api/boards/${id}`),
  createBoard: (data) => request.post('/api/boards', data),
  updateBoard: (id, data) => request.put(`/api/boards/${id}`, data),
  deleteBoard: (id) => request.delete(`/api/boards/${id}`),
  getMembers: (boardId) => request.get(`/api/boards/${boardId}/members`),
  addMember: (boardId, data) => request.post(`/api/boards/${boardId}/members`, data),
  removeMember: (memberId) => request.delete(`/api/board-members/${memberId}`),
  changeRole: (memberId, role) => request.put(`/api/board-members/${memberId}/role`, { role }),
  getActivities: (boardId) => request.get(`/api/boards/${boardId}/activities`),
  getStatistics: (boardId) => request.get(`/api/boards/${boardId}/statistics`)
}
