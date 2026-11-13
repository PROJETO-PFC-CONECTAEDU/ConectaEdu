import { createBrowserRouter, Navigate } from 'react-router-dom';
import { Dashboard } from './pages/Dashboard';
import { Schools } from './pages/Schools';
import { Universities } from './pages/Universities';
import { Students } from './pages/Students';
import { LegalDocuments } from './pages/LegalDocuments';
import { Profile } from './pages/Profile';
import { MapPage } from './pages/MapPage';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { LegalDocumentView } from './pages/LegalDocumentView';
import { Demands } from './pages/Demands';
import { DemandDetail } from './pages/DemandDetail';
import { Schedule } from './pages/Schedule';
import { InternshipHours } from './pages/InternshipHours';
import { Evaluations } from './pages/Evaluations';
import { Reports } from './pages/Reports';
import { PlatformUsers } from './pages/PlatformUsers';
import { Layout } from './components/layout/Layout';
import { RoleProtectedRoute } from './components/auth/RoleProtectedRoute';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/register',
    element: <Register />,
  },
  {
    path: '/legal/:type',
    element: <LegalDocumentView />,
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
        path: '/mapa',
        element: <MapPage />,
      },
      {
        element: <RoleProtectedRoute allowedRoles={['PLATFORM_ADMIN', 'UNIVERSITY_ADMIN']} />,
        children: [
          {
            path: '/estudantes',
            element: <Students />,
          },
        ]
      },
      {
        element: <RoleProtectedRoute allowedRoles={['PLATFORM_ADMIN']} />,
        children: [
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
          },
          {
            path: '/usuarios',
            element: <PlatformUsers />,
          }
        ]
      },
      {
        element: <RoleProtectedRoute allowedRoles={['SCHOOL_DIRECTOR', 'STUDENT']} />,
        children: [
          {
            path: '/demandas',
            element: <Demands />,
          },
          {
            path: '/demandas/:id',
            element: <DemandDetail />,
          },
          {
            path: '/agenda',
            element: <Schedule />,
          },
          {
            path: '/horas',
            element: <InternshipHours />,
          },
          {
            path: '/avaliacoes',
            element: <Evaluations />,
          },
          {
            path: '/relatorios',
            element: <Reports />,
          }
        ]
      }
    ],
  },
]);
