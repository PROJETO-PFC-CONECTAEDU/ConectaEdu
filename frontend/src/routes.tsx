import { createBrowserRouter, Navigate } from 'react-router-dom';
import { Dashboard } from './pages/Dashboard';
import { Schools } from './pages/Schools';
import { Universities } from './pages/Universities';
import { Students } from './pages/Students';
import { Login } from './pages/Login';
import { Layout } from './components/layout/Layout';

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
      }
    ],
  },
]);
