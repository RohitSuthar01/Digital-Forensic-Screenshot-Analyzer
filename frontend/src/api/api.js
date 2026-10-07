import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  withCredentials: true, // Important for sending cookies (JSESSIONID) and handling CSRF
});

// Add a request interceptors to include the CSRF token if needed
// We'll handle CSRF by sending the X-XSRF-TOKEN cookie as a header (Spring Boot's default)
api.interceptors.request.use(function (config) {
  const token = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
  if (token) {
    config.headers['X-XSRF-TOKEN'] = token[1];
  }
  return config;
});

export default api;