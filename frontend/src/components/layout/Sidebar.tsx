import { useAuth } from '../../context/AuthContext';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Map as MapIcon, 
  Users,
  School,
  FileText
} from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

const menuItems = [
  { icon: LayoutDashboard, label: 'Dashboard', path: '/dashboard', roles: ['PLATFORM_ADMIN', 'UNIVERSITY_ADMIN', 'STUDENT', 'SCHOOL_DIRECTOR'] },
  { icon: Users, label: 'Estudantes', path: '/estudantes', roles: ['PLATFORM_ADMIN'] },
  { icon: School, label: 'Escolas', path: '/oportunidades', roles: ['PLATFORM_ADMIN'] },
  { icon: MapIcon, label: 'Universidades', path: '/universidades', roles: ['PLATFORM_ADMIN'] },
  { icon: FileText, label: 'Documentação', path: '/documentos-legais', roles: ['PLATFORM_ADMIN'] },
];

export function Sidebar() {
  const { user } = useAuth();

  const filteredItems = menuItems.filter(item => 
    !item.roles || (user && item.roles.includes(user.role))
  );

  return (
    <aside className="fixed left-0 top-0 h-screen w-64 bg-brand-dark flex flex-col text-white z-30">
      <div className="p-8">
        <h1 className="text-2xl font-display font-bold text-white tracking-tight">
          Conecta<span className="text-brand-accent">Edu</span>
        </h1>
      </div>

      <nav className="flex-1 px-4 space-y-2">
        {filteredItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => cn(
              'flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-colors',
              isActive 
                ? 'bg-brand-primary text-white' 
                : 'text-text-muted hover:bg-white/5 hover:text-white'
            )}
          >
            <item.icon size={20} />
            {item.label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
