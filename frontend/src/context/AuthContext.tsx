import React, { createContext, useContext, useState, useEffect } from 'react';
import { User } from '../types';
import { api } from '../api/client';

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (login: string, pass: string) => Promise<void>;
  quickLogin: (persona: 'alice' | 'bob' | 'clara' | 'dan' | 'eve') => Promise<void>;
  logout: () => void;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(localStorage.getItem('auth_token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      api.auth.me()
        .then((u) => setUser(u))
        .catch(() => logout())
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, [token]);

  const login = async (loginStr: string, pass: string) => {
    const data = await api.auth.login(loginStr, pass);
    localStorage.setItem('auth_token', data.token);
    setToken(data.token);
    setUser({
      id: data.userId,
      login: data.login,
      displayName: data.displayName,
      role: data.role,
      walletAddress: data.walletAddress,
    });
  };

  const quickLogin = async (persona: 'alice' | 'bob' | 'clara' | 'dan' | 'eve') => {
    await login(persona, 'password123');
  };

  const logout = () => {
    localStorage.removeItem('auth_token');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, login, quickLogin, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
