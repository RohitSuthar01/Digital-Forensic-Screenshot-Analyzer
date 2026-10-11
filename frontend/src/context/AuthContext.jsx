import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

axios.defaults.xsrfCookieName = 'XSRF-TOKEN';
axios.defaults.xsrfHeaderName = 'X-XSRF-TOKEN';

const fetchCsrfToken = async () => {
  await axios.get('/api/auth/csrf', { withCredentials: true });
  const cookie = document.cookie.match(/(?:^|;\s*)XSRF-TOKEN=([^;]+)/);
  if (!cookie) throw new Error('Unable to initialize request security token. Refresh and try again.');
  return decodeURIComponent(cookie[1]);
};

const AuthContext = createContext();

export const useAuth = () => {
  return useContext(AuthContext);
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadUser = async () => {
      try {
        const response = await axios.get('/api/auth/me', { withCredentials: true });
        setUser(response.data);
      } catch (error) {
        // User not authenticated
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    loadUser();
  }, []);

  const login = async (credentials) => {
    try {
      const csrfToken = await fetchCsrfToken();
      const params = new URLSearchParams();
      params.append('username', credentials.username);
      params.append('password', credentials.password);
      const response = await axios.post('/api/auth/login', params.toString(), {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'X-XSRF-TOKEN': csrfToken,
        },
        withCredentials: true,
      });
      setUser(response.data);
      return response.data;
    } catch (error) {
      throw error;
    }
  };

  const logout = async () => {
    try {
      const csrfToken = await fetchCsrfToken();
      await axios.post('/api/auth/logout', {}, { withCredentials: true, headers: { 'X-XSRF-TOKEN': csrfToken } });
      setUser(null);
    } catch (error) {
      console.error('Logout error', error);
    }
  };

  const register = async (userData) => {
    try {
      const csrfToken = await fetchCsrfToken();
      const response = await axios.post('/api/auth/register', userData, {
        withCredentials: true,
        headers: { 'X-XSRF-TOKEN': csrfToken },
      });
      return response.data;
    } catch (error) {
      throw error;
    }
  };

  const value = {
    user,
    loading,
    login,
    logout,
    register
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
