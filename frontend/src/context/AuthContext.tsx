import React, { createContext, useContext, useState } from 'react';

interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  universityId?: string;
  schoolId?: string;
  emailConfirmed: boolean;
  twoFactorEnabled: boolean;
}

interface AuthContextData {
  user: User | null;
  token: string | null;
  signIn: (data: { token: string; user: User }) => void;
  updateUser: (user: User) => void;
  signOut: () => void;
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

  const signOut = () => {
    localStorage.removeItem('@ConectaEdu:token');
    localStorage.removeItem('@ConectaEdu:user');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, signIn, updateUser, signOut, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
