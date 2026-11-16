import React, { createContext, useContext, useState } from 'react';

import api from '../lib/api';

interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  universityId?: string;
  schoolId?: string;
  emailConfirmed: boolean;
  twoFactorEnabled: boolean;
  universityCNPJ?: string;
  schoolCIE?: string;
}

interface AuthContextData {
  user: User | null;
  token: string | null;
  signIn: (data: { token: string; user: User }) => void;
  updateUser: (user: User) => void;
  signOut: () => Promise<void>;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextData>({} as AuthContextData);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const storagedUser = localStorage.getItem('@ConectaEdu:user');
    return storagedUser ? JSON.parse(storagedUser) : null;
  });
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('@ConectaEdu:token');
  });

  const signIn = ({ token, user }: { token: string; user: User }) => {
    localStorage.setItem('@ConectaEdu:token', token);
    localStorage.setItem('@ConectaEdu:user', JSON.stringify(user));

    setToken(token);
    setUser(user);
  };

  const updateUser = (newUser: User) => {
    localStorage.setItem('@ConectaEdu:user', JSON.stringify(newUser));
    setUser(newUser);
  };

  const signOut = async () => {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      console.error('Erro ao fazer logout no servidor:', error);
    } finally {
      localStorage.removeItem('@ConectaEdu:token');
      localStorage.removeItem('@ConectaEdu:user');
      setToken(null);
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, token, signIn, updateUser, signOut, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
