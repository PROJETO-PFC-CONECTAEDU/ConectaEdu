import { format, isToday, isTomorrow } from 'date-fns';
import { ptBR } from 'date-fns/locale';

interface TimeBadgeProps {
  date: Date;
  startTime: string;
  endTime: string;
  mobile?: boolean;
}

export function TimeBadge({ date, startTime, endTime, mobile }: TimeBadgeProps) {
  if (mobile) {
    return (
      <div className="bg-page-bg rounded-badge p-[6px_8px] flex flex-col items-center justify-center min-w-[60px]">
        <span className="text-text-heading font-sans font-bold text-[12px] leading-tight">{startTime}</span>
        <span className="text-text-body font-sans text-[10px] leading-tight">{endTime}</span>
      </div>
    );
  }

  const getRelativeDate = (date: Date) => {
    if (isToday(date)) return 'Hoje';
    if (isTomorrow(date)) return 'Amanhã';
    return format(date, 'eee', { locale: ptBR }).replace(/^\w/, (c) => c.toUpperCase()).replace('.', '');
  };

  const dayStr = getRelativeDate(date);
  const dateStr = format(date, 'dd de MMM', { locale: ptBR });

  return (
    <div className="bg-time-badge-bg rounded-badge p-4 flex flex-col justify-center min-w-[110px] h-[72px]">
      <span className="text-time-badge-text font-sans font-bold text-[12px] whitespace-nowrap">
        {dayStr}, {dateStr}
      </span>
      <span className="text-text-body font-sans text-[11px]">
        {startTime} - {endTime}
      </span>
    </div>
  );
}
