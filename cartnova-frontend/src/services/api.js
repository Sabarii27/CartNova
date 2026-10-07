import axios from 'axios';

export const TOKEN_KEY = 'cartnova_token';
export const USER_KEY = 'cartnova_user';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 15000,
});

// Attach the JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// If a request made WITH a token comes back 401, the token is expired/invalid
api.interceptors.response.use(
  (res) => res,
  (error) => {
    if (error.response?.status === 401 && localStorage.getItem(TOKEN_KEY)) {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
      window.dispatchEvent(new Event('cartnova:unauthorized'));
    }
    return Promise.reject(error);
  }
);

// Turns any Axios error into a friendly string (never shows raw Java errors)
export function getErrorMessage(error) {
  if (!error.response) {
    return 'Cannot reach the server. Check that the backend is running on port 8080.';
  }
  const data = error.response.data;
  if (data?.errors && typeof data.errors === 'object') {
    return Object.values(data.errors).join('. ');
  }
  if (data?.message) return data.message;
  if (error.response.status === 403) return 'You do not have permission to do that.';
  if (error.response.status === 404) return 'We could not find what you asked for.';
  return 'Something went wrong. Please try again.';
}

export default api;
