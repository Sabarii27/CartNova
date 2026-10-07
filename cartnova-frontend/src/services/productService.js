import api from './api';

// params: { search, category, minPrice, maxPrice, available, page, size, sort }
export const getProducts = (params = {}) => {
  const clean = Object.fromEntries(
    Object.entries(params).filter(([, v]) => v !== '' && v !== null && v !== undefined)
  );
  return api.get('/products', { params: clean }).then((r) => r.data);
};
export const getProduct = (id) => api.get(`/products/${id}`).then((r) => r.data);
export const getCategories = () => api.get('/categories').then((r) => r.data);
