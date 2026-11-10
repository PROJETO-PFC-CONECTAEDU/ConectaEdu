import { clsx } from 'clsx';

export type SessionStatus = 'SCHEDULED' | 'CONFIRMED' | 'PENDING' | 'RESCHEDULED' | 'CANCELED' | 'COMPLETED' | 'REGISTERED' | 'SIGNED' | 'VALIDATED';

interface StatusBadgeProps {
  status: SessionStatus;
  variant?: 'pill' | 'chip';
}

export function StatusBadge({ status, variant = 'pill' }: StatusBadgeProps) {
  const config = {
    CONFIRMED: { label: 'Confirmada', bg: 'bg-status-confirmed-bg', fg: 'text-status-confirmed-fg' },
    SCHEDULED: { label: 'Agendada', bg: 'bg-status-rescheduled-bg', fg: 'text-status-rescheduled-fg' },
    PENDING: { label: 'Pendente', bg: 'bg-status-pending-bg', fg: 'text-status-pending-fg' },
    RESCHEDULED: { label: 'Reagendada', bg: 'bg-status-rescheduled-bg', fg: 'text-status-rescheduled-fg' },
    CANCELED: { label: 'Cancelada', bg: 'bg-status-cancelled-bg', fg: 'text-status-cancelled-fg' },
    COMPLETED: { label: 'Registrada', bg: 'bg-purple-100', fg: 'text-purple-800' },
    REGISTERED: { label: 'Registrada', bg: 'bg-purple-100', fg: 'text-purple-800' },
    SIGNED: { label: 'Assinada', bg: 'bg-emerald-100', fg: 'text-emerald-800' },
    VALIDATED: { label: 'Validada', bg: 'bg-emerald-100', fg: 'text-emerald-700' },
  };

  const { label, bg, fg } = config[status.toUpperCase() as keyof typeof config] || { label: status, bg: 'bg-gray-100', fg: 'text-gray-800' };

  return (
    <span className={clsx(
      "font-sans font-semibold text-[10px] md:text-[11px] whitespace-nowrap",
      variant === 'pill' ? "rounded-pill px-[10px] py-[4px]" : "rounded-[6px] px-[8px] py-[4px]",
      bg,
      fg
    )}>
      {label}
    </span>
  );
}
