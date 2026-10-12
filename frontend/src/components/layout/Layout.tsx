import { Outlet, Navigate } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { ConsentGuard } from './ConsentGuard';
import { useAuth } from '../../context/AuthContext';

export function Layout() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <ConsentGuard>
      <div className="min-h-screen bg-slate-50">
        <Sidebar />
        <main className="ml-64">
          <Outlet />
        </main>
      </div>
    </ConsentGuard>
  );
}
