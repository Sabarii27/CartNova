import api from './api';

export const placeOrder = (shippingAddress) =>
  api.post('/orders', { shippingAddress }).then((r) => r.data);
export const getMyOrders = () => api.get('/orders').then((r) => r.data);
export const getOrder = (id) => api.get(`/orders/${id}`).then((r) => r.data);
export const cancelOrder = (id) => api.put(`/orders/${id}/cancel`).then((r) => r.data);
