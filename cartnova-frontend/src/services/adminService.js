import api from './api';

export const getDashboard = () => api.get('/admin/dashboard').then((r) => r.data);

export const createProduct = (payload) => api.post('/admin/products', payload).then((r) => r.data);
export const updateProduct = (id, payload) => api.put(`/admin/products/${id}`, payload).then((r) => r.data);
export const deleteProduct = (id) => api.delete(`/admin/products/${id}`).then((r) => r.data);

export const getUsers = () => api.get('/admin/users').then((r) => r.data);
export const getUser = (id) => api.get(`/admin/users/${id}`).then((r) => r.data);

export const getAllOrders = () => api.get('/admin/orders').then((r) => r.data);
export const getAdminOrder = (id) => api.get(`/admin/orders/${id}`).then((r) => r.data);
export const updateOrderStatus = (id, status) =>
  api.put(`/admin/orders/${id}/status`, { status }).then((r) => r.data);
