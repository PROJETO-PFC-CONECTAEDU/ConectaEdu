import { Card } from '../ui/Card';
import { Star, Calendar, Quote } from 'lucide-react';
import { formatDate } from '../../utils/format';

interface EvaluationCardProps {
  evaluatorName: string;
  date: string;
  engagement: number;
  autonomy: number;
  punctuality: number;
  comments: string;
}

export function EvaluationCard({
  evaluatorName,
  date,
  engagement,
  autonomy,
  punctuality,
  comments,
}: EvaluationCardProps) {
  const average = ((engagement + autonomy + punctuality) / 3).toFixed(1);

  return (
    <Card className="p-6 space-y-4">
      <div className="flex justify-between items-start">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-brand-primary/10 rounded-full">
            <Star className="text-brand-primary" size={20} />
          </div>
          <div>
            <h4 className="text-sm font-semibold text-text-heading">{evaluatorName}</h4>
            <div className="flex items-center gap-1 text-xs text-text-muted">
              <Calendar size={12} />
              {formatDate(date)}
            </div>
          </div>
        </div>
        <div className="flex items-center gap-1 bg-brand-accent/10 px-2 py-1 rounded text-brand-accent font-bold text-sm">
          {average}
        </div>
      </div>

      <div className="grid grid-cols-3 gap-2 py-2 border-y border-slate-50">
        <div className="text-center">
          <p className="text-[10px] text-text-muted uppercase">Engajamento</p>
          <p className="text-sm font-bold text-text-heading">{engagement}</p>
        </div>
        <div className="text-center">
          <p className="text-[10px] text-text-muted uppercase">Autonomia</p>
          <p className="text-sm font-bold text-text-heading">{autonomy}</p>
        </div>
        <div className="text-center">
          <p className="text-[10px] text-text-muted uppercase">Pontualidade</p>
          <p className="text-sm font-bold text-text-heading">{punctuality}</p>
        </div>
      </div>

      <div className="relative">
        <Quote className="absolute -left-2 -top-2 text-slate-100" size={24} />
        <p className="text-sm text-text-body italic pl-4 relative z-10">
          {comments}
        </p>
      </div>
    </Card>
  );
}
