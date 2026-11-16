import axios from 'axios';
import { handleApiError } from '../utils/errors';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || (import.meta.env.DEV ? 'http://localhost:8080/api/v1' : '/api/v1'),
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('@ConectaEdu:token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !window.location.pathname.includes('/login')) {
      localStorage.removeItem('@ConectaEdu:token');
      localStorage.removeItem('@ConectaEdu:user');
      window.location.href = '/login';
    }
    
    handleApiError(error);
    return Promise.reject(error);
  }
);

export default api;
