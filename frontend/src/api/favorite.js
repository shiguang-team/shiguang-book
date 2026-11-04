import request from './index'

export const addFavorite = (bookId) => request.post(`/api/favorites/${bookId}`)
export const removeFavorite = (bookId) => request.delete(`/api/favorites/${bookId}`)
export const getMyFavorites = () => request.get('/api/favorites')
