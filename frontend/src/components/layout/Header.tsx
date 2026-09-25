import { Bell, Search, User as UserIcon, LogOut, UserCircle } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { formatRole } from '../../utils/format';

interface HeaderProps {
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}

export function Header({ title, subtitle, action }: HeaderProps) {
  const { user, signOut } = useAuth();
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  return (
    <header className="h-20 bg-white border-b border-border-default px-8 flex items-center justify-between sticky top-0 z-20">
      <div>
        <h2 className="text-xl font-bold text-text-heading">{title}</h2>
        {subtitle && <p className="text-sm text-text-body">{subtitle}</p>}
      </div>

      <div className="flex items-center gap-6">
        <div className="relative hidden lg:block w-96">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" size={18} />
          <input
            type="text"
            placeholder="Pesquisar..."
            className="w-full h-10 pl-10 pr-4 rounded-lg border border-border-default bg-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary/20"
          />
        </div>

        <div className="flex items-center gap-4">
          {action}
          <button className="relative p-2 text-text-body hover:text-brand-primary transition-colors rounded-full hover:bg-slate-50">
            <Bell size={22} />
          </button>

          <div className="relative">
            <button 
              onClick={() => setIsProfileOpen(!isProfileOpen)}
              className="flex items-center gap-3 p-1 pl-3 rounded-full hover:bg-slate-50 transition-colors border border-transparent hover:border-border-default"
            >
              <div className="text-right hidden sm:block">
                <p className="text-sm font-semibold text-text-heading">{user?.name}</p>
              </div>
              <div className="h-10 w-10 rounded-full bg-brand-primary/10 flex items-center justify-center text-brand-primary">
                <UserIcon size={20} />
              </div>
            </button>

            {isProfileOpen && (
              <div className="absolute right-0 mt-2 w-56 bg-white rounded-xl shadow-lg border border-border-default py-2 z-50">
                <div className="px-4 py-2 border-b border-border-default mb-2">
                  <p className="text-sm font-semibold text-text-heading">{user?.name}</p>
                  <p className="text-xs text-text-muted truncate">{user?.email}</p>
                  <span className="mt-1 inline-block px-2 py-0.5 bg-brand-primary/10 text-brand-primary text-[10px] font-bold rounded-full uppercase">
                    {formatRole(user?.role)}
                  </span>
                </div>
                
                <Link 
                  to="/perfil"
                  onClick={() => setIsProfileOpen(false)}
                  className="w-full flex items-center gap-3 px-4 py-2 text-sm text-text-body hover:bg-slate-50 hover:text-brand-primary transition-colors"
                >
                  <UserCircle size={18} />
                  Meu Perfil
                </Link>

                <button 
                  onClick={() => {
                    signOut();
                    window.location.href = '/login';
                  }}
                  className="w-full flex items-center gap-3 px-4 py-2 text-sm text-red-600 hover:bg-red-50 transition-colors"
                >
                  <LogOut size={18} />
                  Sair da Conta
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  );
}
