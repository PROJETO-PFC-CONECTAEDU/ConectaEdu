import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  Search, 
  Map as MapIcon, 
  Calendar, 
  Clock, 
  Menu 
} from 'lucide-react';
import { clsx } from 'clsx';

export function BottomNav() {
  const tabs = [
    { icon: LayoutDashboard, label: 'Início', path: '/dashboard' },
    { icon: Search, label: 'Vagas', path: '/demandas' },
    { icon: MapIcon, label: 'Mapa', path: '/mapa' },
    { icon: Calendar, label: 'Agenda', path: '/agenda' },
    { icon: Clock, label: 'Horas', path: '/horas' },
    { icon: Menu, label: 'Mais', path: '/perfil' },
  ];

  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-surface border-t border-border-default h-[calc(64px+env(safe-area-inset-bottom))] px-2 pb-[env(safe-area-inset-bottom)] flex items-center justify-around z-50">
      {tabs.map((tab) => (
        <NavLink
          key={tab.path}
          to={tab.path}
          className={({ isActive }) => clsx(
            "flex flex-col items-center gap-1 flex-1 py-2 transition-colors",
            isActive ? "text-brand-primary" : "text-text-body"
          )}
        >
          {({ isActive }) => (
            <>
              <tab.icon size={20} />
              <span className={clsx(
                "text-[10px] font-sans",
                isActive ? "font-semibold" : "font-medium"
              )}>
                {tab.label}
              </span>
            </>
          )}
        </NavLink>
      ))}
    </nav>
  );
}
