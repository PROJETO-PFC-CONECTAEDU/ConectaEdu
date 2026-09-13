import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Map as MapIcon, 
  Users,
  School
} from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

const menuItems = [
  { icon: LayoutDashboard, label: 'Dashboard', path: '/dashboard' },
  { icon: Users, label: 'Estudantes', path: '/estudantes' },
  { icon: School, label: 'Escolas', path: '/oportunidades' },
  { icon: MapIcon, label: 'Universidades', path: '/universidades' },
];

export function Sidebar() {
  return (
    <aside className="fixed left-0 top-0 h-screen w-64 bg-brand-dark flex flex-col text-white z-30">
      <div className="p-8">
        <h1 className="text-2xl font-display font-bold text-white tracking-tight">
          Conecta<span className="text-brand-accent">Edu</span>
        </h1>
      </div>

      <nav className="flex-1 px-4 space-y-2">
        {menuItems.map((item) => (
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

      <div className="p-4 border-t border-white/10">
        <div className="flex items-center gap-3 px-4 py-3">
          <div className="w-10 h-10 rounded-full bg-brand-primary flex items-center justify-center font-bold text-xs text-white">
            ADM
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold truncate">Administrador</p>
            <p className="text-xs text-text-muted truncate">Visão Geral</p>
          </div>
        </div>
      </div>
    </aside>
  );
}
