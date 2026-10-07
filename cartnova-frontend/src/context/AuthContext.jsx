import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import * as authService from '../services/authService';
import { TOKEN_KEY, USER_KEY } from '../services/api';

const AuthContext = createContext(null);

function readStoredUser() {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw && localStorage.getItem(TOKEN_KEY) ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(readStoredUser);
  const [sessionMessage, setSessionMessage] = useState('');

  const persist = (data) => {
    const { token, ...profile } = data;
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(profile));
    setUser(profile);
    setSessionMessage('');
    return profile;
  };

  const login = useCallback(async (credentials) => persist(await authService.login(credentials)), []);
  const register = useCallback(async (payload) => persist(await authService.register(payload)), []);

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }, []);

  // Fired by the Axios interceptor when the JWT has expired
  useEffect(() => {
    const onUnauthorized = () => {
      setUser(null);
      setSessionMessage('Your session expired. Please sign in again.');
    };
    window.addEventListener('cartnova:unauthorized', onUnauthorized);
    return () => window.removeEventListener('cartnova:unauthorized', onUnauthorized);
  }, []);

  const value = useMemo(
    () => ({
      user,
      isAuthenticated: !!user,
      isAdmin: user?.role === 'ADMIN',
      sessionMessage,
      clearSessionMessage: () => setSessionMessage(''),
      login,
      register,
      logout,
    }),
    [user, sessionMessage, login, register, logout]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
