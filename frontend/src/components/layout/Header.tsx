import { Bell, Search } from 'lucide-react';

interface HeaderProps {
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}

export function Header({ title, subtitle, action }: HeaderProps) {
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
        </div>
      </div>
    </header>
  );
}
