import React from 'react';
import { 
  format, 
  startOfMonth, 
  endOfMonth, 
  startOfWeek, 
  endOfWeek, 
  eachDayOfInterval, 
  isSameMonth, 
  isSameDay, 
  addMonths, 
  subMonths,
  getISOWeek,
  isWeekend
} from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { clsx } from 'clsx';

interface MonthCalendarProps {
  currentDate: Date;
  onDateChange: (date: Date) => void;
  eventDates: Date[];
  selectedDate?: Date | null;
  onSelectDate?: (date: Date | null) => void;
}

export function MonthCalendar({ currentDate, onDateChange, eventDates, selectedDate, onSelectDate }: MonthCalendarProps) {
  const monthStart = startOfMonth(currentDate);
  const monthEnd = endOfMonth(monthStart);
  const startDate = startOfWeek(monthStart, { weekStartsOn: 1 });
  const endDate = endOfWeek(monthEnd, { weekStartsOn: 1 });

  const calendarDays = eachDayOfInterval({
    start: startDate,
    end: endDate,
  });

  const nextMonth = () => onDateChange(addMonths(currentDate, 1));
  const prevMonth = () => onDateChange(subMonths(currentDate, 1));

  // Agrupar dias por semana para facilitar a exibição do número da semana
  const weeks: Date[][] = [];
  let currentWeek: Date[] = [];
  calendarDays.forEach((day) => {
    currentWeek.push(day);
    if (currentWeek.length === 7) {
      weeks.push(currentWeek);
      currentWeek = [];
    }
  });

  const dayLabels = ['seg.', 'ter.', 'qua.', 'qui.', 'sex.', 'sáb.', 'dom.'];

  return (
    <div className="w-full">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-text-heading font-display font-bold text-base">
          {format(currentDate, 'MMMM yyyy', { locale: ptBR }).replace(/^\w/, (c) => c.toUpperCase())}
        </h2>
        <div className="flex gap-2">
          <button 
            onClick={prevMonth}
            className="p-1 hover:bg-gray-100 rounded-full transition-colors"
            aria-label="Mês anterior"
          >
            <ChevronLeft size={16} className="text-text-heading" />
          </button>
          <button 
            onClick={nextMonth}
            className="p-1 hover:bg-gray-100 rounded-full transition-colors"
            aria-label="Próximo mês"
          >
            <ChevronRight size={16} className="text-text-heading" />
          </button>
        </div>
      </div>

      <div className="grid grid-cols-8 gap-y-2 text-center">
        {/* Cabeçalho */}
        <div className="text-[11px] font-sans text-cal-muted flex items-center justify-center">Sem</div>
        {dayLabels.map((label) => (
          <div key={label} className="text-[11px] font-sans text-cal-muted flex items-center justify-center">
            {label}
          </div>
        ))}

        {/* Dias */}
        {weeks.map((week, i) => (
          <React.Fragment key={i}>
            <div className="flex items-center justify-center">
              <div className="w-6 h-6 flex items-center justify-center bg-cal-week-number-bg rounded-[4px] text-[11px] font-sans font-extralight text-cal-muted">
                {getISOWeek(week[0])}
              </div>
            </div>
            {week.map((day) => {
              const isEventDay = eventDates.some(eventDate => isSameDay(eventDate, day));
              const isCurrentMonth = isSameMonth(day, monthStart);
              const isDayWeekend = isWeekend(day);
              const isSelected = selectedDate && isSameDay(day, selectedDate);
              
              return (
                <div key={day.toString()} className="flex items-center justify-center">
                  <div
                    onClick={() => onSelectDate?.(isSelected ? null : day)}
                    className={clsx(
                      "w-6 h-6 flex items-center justify-center text-[11px] font-sans rounded-[4px] transition-colors cursor-pointer",
                      isSelected ? "ring-2 ring-brand-primary ring-offset-1" : "",
                      isEventDay ? "bg-brand-primary text-white" : 
                      !isCurrentMonth ? (isDayWeekend ? "text-[#1270b0]" : "text-cal-day") :
                      isDayWeekend ? "text-cal-weekend" : "text-cal-muted",
                      isCurrentMonth && isSameDay(day, new Date()) && !isEventDay ? "bg-brand-primary/10 text-brand-primary font-bold" : ""
                    )}
                  >
                    {format(day, 'd')}
                  </div>
                </div>
              );
            })}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
}
