import axios from 'axios';
import { handleApiError } from '../utils/errors';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
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
