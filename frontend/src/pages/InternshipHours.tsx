import { useEffect, useState } from 'react';
import { Clock, Calendar, CheckCircle, TrendingUp } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import api from '../lib/api';
import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { MetricCard } from '../components/ui/MetricCard';
import { ProgressRing } from '../components/ui/ProgressRing';

import { formatLessonStatus, formatDate } from '../utils/format';

interface InternshipHoursData {
  totalHours: number;
  validatedLessons: number;
  pendingLessons: number;
  canceledLessons: number;
  requiredHours: number;
  remainingHours: number;
}

interface Lesson {
  id: string;
  schoolName: string;
  subject: string;
  scheduledStart: string;
  scheduledEnd: string;
  status: string;
}

export function InternshipHours() {
  const { user } = useAuth();
  const [hoursData, setHoursData] = useState<InternshipHoursData | null>(null);
  const [lessons, setLessons] = useState<Lesson[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadData() {
      if (!user) return;
      try {
        const [hoursResponse, lessonsResponse] = await Promise.all([
          api.get(`/internship/students/${user.id}/hours`),
          api.get(`/lessons/student/${user.id}`)
        ]);
        setHoursData(hoursResponse.data);
        setLessons(lessonsResponse.data);
      } catch (error) {
        console.error('Erro ao carregar dados de estágio:', error);
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, [user]);

  if (loading || !hoursData) {
    return <div className="p-8 text-center">Carregando...</div>;
  }

  const progress = Math.min(100, (hoursData.totalHours / hoursData.requiredHours) * 100);

  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { className: string }> = {
      'SCHEDULED': { className: 'bg-blue-100 text-blue-700' },
      'CONFIRMED': { className: 'bg-green-100 text-green-700' },
      'COMPLETED': { className: 'bg-purple-100 text-purple-700' },
      'REGISTERED': { className: 'bg-purple-100 text-purple-700' },
      'SIGNED': { className: 'bg-emerald-100 text-emerald-800' },
      'VALIDATED': { className: 'bg-emerald-100 text-emerald-700' },
      'CANCELED': { className: 'bg-red-100 text-red-700' },
    };

    const config = statusMap[status] || { className: 'bg-slate-100 text-slate-700' };
    return (
      <span className={`px-2 py-1 rounded-full text-xs font-medium ${config.className}`}>
        {formatLessonStatus(status)}
      </span>
    );
  };

  const calculateHours = (start: string | number[], end: string | number[]) => {
    const parseDate = (d: string | number[]) => {
      if (Array.isArray(d)) {
        return new Date(d[0], d[1] - 1, d[2], d[3] || 0, d[4] || 0);
      }
      return new Date(d);
    };
    
    const startDate = parseDate(start);
    const endDate = parseDate(end);
    
    if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) return '0.0';
    
    const diff = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60);
    return diff.toFixed(1);
  };

  return (
    <div className="flex-1">
      <Header 
        title="Horas de Estágio" 
        subtitle="Acompanhe seu progresso e histórico de atividades."
      />

      <div className="p-8 max-w-7xl mx-auto space-y-8">

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <Card className="lg:col-span-1 flex flex-col items-center justify-center p-8 gap-4">
          <h3 className="text-lg font-semibold text-text-heading">Progresso Geral</h3>
          <ProgressRing 
            progress={progress} 
            label={`${Math.round(progress)}%`}
            sublabel={`${hoursData.totalHours}h / ${hoursData.requiredHours}h`}
            size={160}
            strokeWidth={12}
          />
          <div className="text-center mt-4">
            <p className="text-sm text-text-body">Faltam {hoursData.remainingHours} horas para completar seu estágio.</p>
          </div>
        </Card>

        <div className="lg:col-span-2 grid grid-cols-1 sm:grid-cols-2 gap-4">
          <MetricCard 
            icon={<Calendar className="text-brand-primary" size={20} />}
            label="Total de Aulas"
            value={hoursData.validatedLessons}
            note="Aulas validadas"
          />
          <MetricCard 
            icon={<Clock className="text-brand-accent" size={20} />}
            label="Média Semanal"
            value={`${(hoursData.totalHours / 4).toFixed(1)}h`} // Mock: assume 4 semanas
            note="Horas por semana"
          />
          <MetricCard 
            icon={<TrendingUp className="text-orange-500" size={20} />}
            label="Aulas Pendentes"
            value={hoursData.pendingLessons}
            note="Aguardando validação"
          />
          <MetricCard 
            icon={<CheckCircle className="text-brand-primary" size={20} />}
            label="Aulas Canceladas"
            value={hoursData.canceledLessons}
            note="Não contabilizadas"
          />
        </div>
      </div>

      <Card>
        <div className="p-6 border-b border-border-default flex justify-between items-center">
          <h3 className="text-lg font-semibold text-text-heading">Histórico de Aulas e Atividades</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-slate-50 border-b border-border-default">
                <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Data</th>
                <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Escola</th>
                <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Matéria</th>
                <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Horas</th>
                <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-default">
              {lessons.map((lesson) => (
                <tr key={lesson.id} className="hover:bg-slate-50 transition-colors">
                  <td className="px-6 py-4 text-sm text-text-heading font-medium">
                    {formatDate(lesson.scheduledStart)}
                  </td>
                  <td className="px-6 py-4 text-sm text-text-body">{lesson.schoolName}</td>
                  <td className="px-6 py-4 text-sm text-text-body">{lesson.subject}</td>
                  <td className="px-6 py-4 text-sm text-text-body">
                    {calculateHours(lesson.scheduledStart, lesson.scheduledEnd)}h
                  </td>
                  <td className="px-6 py-4 text-sm">
                    {getStatusBadge(lesson.status)}
                  </td>
                </tr>
              ))}
              {lessons.length === 0 && (
                <tr>
                  <td colSpan={5} className="px-6 py-8 text-center text-text-muted">
                    Nenhuma atividade registrada até o momento.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </Card>
      </div>
    </div>
  );
}
