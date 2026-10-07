import api from './api';

export const getCart = () => api.get('/cart').then((r) => r.data);
export const addToCart = (productId, quantity) =>
  api.post('/cart/items', { productId, quantity }).then((r) => r.data);
export const updateCartItem = (cartItemId, quantity) =>
  api.put(`/cart/items/${cartItemId}`, { quantity }).then((r) => r.data);
export const removeCartItem = (cartItemId) => api.delete(`/cart/items/${cartItemId}`).then((r) => r.data);
export const clearCart = () => api.delete('/cart').then((r) => r.data);
