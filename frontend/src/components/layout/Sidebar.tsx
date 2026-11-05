import { useAuth } from '../../context/AuthContext';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Map as MapIcon, 
  Users,
  School,
  FileText,
  Building2,
  Search,
  Calendar,
  Clock,
  Star,
  BarChart3
} from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

const menuItems = [
  { icon: LayoutDashboard, label: 'Dashboard', path: '/dashboard', roles: ['PLATFORM_ADMIN', 'UNIVERSITY_ADMIN', 'STUDENT', 'SCHOOL_DIRECTOR'] },
  { icon: Search, label: 'Oportunidades', path: '/demandas', roles: ['STUDENT'] },
  { icon: Search, label: 'Demandas', path: '/demandas', roles: ['SCHOOL_DIRECTOR'] },
  { icon: MapIcon, label: 'Mapa', path: '/mapa', roles: ['PLATFORM_ADMIN', 'STUDENT', 'SCHOOL_DIRECTOR'] },
  { icon: Calendar, label: 'Agenda', path: '/agenda', roles: ['STUDENT', 'SCHOOL_DIRECTOR'] },
  { icon: Clock, label: 'Horas', path: '/horas', roles: ['STUDENT'] },
  { icon: Star, label: 'Avaliações', path: '/avaliacoes', roles: ['STUDENT'] },
  { icon: BarChart3, label: 'Relatórios', path: '/relatorios', roles: ['STUDENT', 'PLATFORM_ADMIN'] },
  { icon: Users, label: 'Estudantes', path: '/estudantes', roles: ['PLATFORM_ADMIN', 'UNIVERSITY_ADMIN'] },
  { icon: School, label: 'Escolas', path: '/oportunidades', roles: ['PLATFORM_ADMIN'] },
  { icon: Building2, label: 'Universidades', path: '/universidades', roles: ['PLATFORM_ADMIN'] },
  { icon: FileText, label: 'Documentação', path: '/documentos-legais', roles: ['PLATFORM_ADMIN'] },
];

export function Sidebar() {
  const { user } = useAuth();

  const filteredItems = menuItems.filter(item => 
    !item.roles || (user && item.roles.includes(user.role))
  );

  return (
    <aside className="fixed left-0 top-0 h-screen w-[260px] bg-brand-dark flex flex-col text-white z-30 hidden md:flex">
      <div className="p-[32px_24px_40px_24px] flex items-center gap-2">
        <h1 className="text-[20px] text-white font-display font-bold tracking-tight">
          Conecta<span className="text-brand-accent">Edu</span>
        </h1>
      </div>

      <nav className="flex-1 px-4 space-y-[6px]">
        {filteredItems.map((item) => (
          <NavLink
            key={item.label + item.path}
            to={item.path}
            className={({ isActive }) => cn(
              'relative flex items-center h-[42px] rounded-lg transition-colors',
              isActive 
                ? 'bg-brand-primary text-white font-semibold' 
                : 'text-white/70 hover:bg-white/5 hover:text-white font-medium'
            )}
          >
            <item.icon size={20} className="absolute left-4" />
            <span className="absolute left-[46px] text-[14px] font-sans">{item.label}</span>
          </NavLink>
        ))}
      </nav>

      <div className="p-6 border-t border-white/10 mt-auto">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-brand-primary/20 flex items-center justify-center text-brand-primary font-bold">
            {user?.name?.charAt(0)}
          </div>
          <div className="flex flex-col overflow-hidden">
            <span className="text-[14px] font-sans font-semibold text-white truncate">
                {user?.name}
            </span>
            <span className="text-[11px] font-sans text-text-muted">
                {user?.role === 'STUDENT' ? 'Estudante de Pedagogia' : user?.role}
            </span>
          </div>
        </div>
      </div>
    </aside>
  );
}
