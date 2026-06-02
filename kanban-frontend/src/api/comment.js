import request from './request'

export const commentApi = {
  getComments: (cardId) => request.get(`/api/cards/${cardId}/comments`),
  addComment: (cardId, data) => request.post(`/api/cards/${cardId}/comments`, data),
  deleteComment: (commentId) => request.delete(`/api/comments/${commentId}`)
}
