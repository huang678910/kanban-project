import request from './request'

export const attachmentApi = {
  getAttachments: (cardId) => request.get(`/api/cards/${cardId}/attachments`),
  upload: (cardId, file) => {
    const formData = new FormData()
    formData.append('file', file)
    return request.post(`/api/cards/${cardId}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  deleteAttachment: (attachmentId) => request.delete(`/api/attachments/${attachmentId}`)
}
