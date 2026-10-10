import { createBrowserRouter, Navigate } from 'react-router-dom';
import { Dashboard } from './pages/Dashboard';
import { Schools } from './pages/Schools';
import { Universities } from './pages/Universities';
import { Students } from './pages/Students';
import { LegalDocuments } from './pages/LegalDocuments';
import { Profile } from './pages/Profile';
import { Login } from './pages/Login';
import { Layout } from './components/layout/Layout';
import { RoleProtectedRoute } from './components/auth/RoleProtectedRoute';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        path: '/',
        element: <Navigate to="/dashboard" replace />,
      },
      {
        path: '/dashboard',
        element: <Dashboard />,
      },
      {
        path: '/perfil',
        element: <Profile />,
      },
      {
        element: <RoleProtectedRoute allowedRoles={['PLATFORM_ADMIN']} />,
        children: [
          {
            path: '/estudantes',
            element: <Students />,
          },
          {
            path: '/oportunidades',
            element: <Schools />,
          },
          {
            path: '/universidades',
            element: <Universities />,
          },
          {
            path: '/documentos-legais',
            element: <LegalDocuments />,
          }
        ]
      }
    ],
  },
]);
