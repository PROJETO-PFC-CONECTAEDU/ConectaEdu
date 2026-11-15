import { StatusBadge, type SessionStatus } from './StatusBadge';
import { TimeBadge } from './TimeBadge';
import { parseISO } from 'date-fns';

interface SessionRowProps {
  session: {
    id: string;
    scheduledStart: string;
    scheduledEnd: string;
    schoolName: string;
    subject: string;
    status: SessionStatus;
  };
  onClick?: () => void;
}

export function SessionRow({ session, onClick }: SessionRowProps) {
  const startDate = parseISO(session.scheduledStart);
  const startTime = session.scheduledStart.split('T')[1].substring(0, 5);
  const endTime = session.scheduledEnd.split('T')[1].substring(0, 5);

  return (
    <div 
      onClick={onClick}
      className="group bg-row-bg rounded-lg md:rounded-row h-auto md:h-[72px] p-3 md:p-0 flex flex-col md:flex-row items-start md:items-center gap-3 md:gap-0 cursor-pointer hover:bg-slate-100 transition-colors border border-transparent hover:border-border-default"
    >
      {/* Desktop Time Badge */}
      <div className="hidden md:block ml-4">
        <TimeBadge date={startDate} startTime={startTime} endTime={endTime} />
      </div>

      {/* Mobile Layout */}
      <div className="md:hidden flex w-full gap-3 items-center">
        <TimeBadge date={startDate} startTime={startTime} endTime={endTime} mobile />
        <div className="flex-1 flex flex-col gap-0.5">
          <h4 className="text-text-heading font-display font-bold text-[14px] leading-tight">
            {session.schoolName}
          </h4>
          <p className="text-text-body font-sans text-[12px]">
            {session.subject}
          </p>
        </div>
        <StatusBadge status={session.status} variant="chip" />
      </div>

      {/* Desktop Details */}
      <div className="hidden md:flex flex-col ml-[32px] flex-1">
        <h4 className="text-text-heading font-sans font-bold text-[15px]">
          {session.schoolName}
        </h4>
        <p className="text-text-body font-sans text-[13px]">
          {session.subject}
        </p>
      </div>

      {/* Desktop Status */}
      <div className="hidden md:block mr-6">
        <StatusBadge status={session.status} />
      </div>
    </div>
  );
}
