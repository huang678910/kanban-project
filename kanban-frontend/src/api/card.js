import request from './request'

export const cardApi = {
  getCards: (listId) => request.get(`/api/lists/${listId}/cards`),
  getCard: (cardId) => request.get(`/api/cards/${cardId}`),
  createCard: (listId, data) => request.post(`/api/lists/${listId}/cards`, data),
  updateCard: (cardId, data) => request.put(`/api/cards/${cardId}`, data),
  deleteCard: (cardId) => request.delete(`/api/cards/${cardId}`),
  moveCard: (cardId, targetListId, position) =>
    request.put(`/api/cards/${cardId}/move`, { targetListId, position })
}
