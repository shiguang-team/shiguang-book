import request from './index'

export const addFavorite = (bookId) => request.post(`/favorites/${bookId}`)
export const removeFavorite = (bookId) => request.delete(`/favorites/${bookId}`)
export const getMyFavorites = () => request.get('/favorites')
export const checkFavorite = (bookId) => request.get(`/favorites/check/${bookId}`)