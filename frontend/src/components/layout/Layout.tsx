import { Outlet, Navigate } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { BottomNav } from './BottomNav';
import { ConsentGuard } from './ConsentGuard';
import { useAuth } from '../../context/AuthContext';

export function Layout() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <ConsentGuard>
      <div className="min-h-screen bg-page-bg">
        <Sidebar />
        <main className="md:ml-[260px] pb-16 md:pb-0">
          <Outlet />
        </main>
        <BottomNav />
      </div>
    </ConsentGuard>
  );
}
