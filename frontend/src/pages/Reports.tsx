import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { MetricCard } from '../components/ui/MetricCard';
import { Filter, Calendar, BookOpen, CheckCircle, XCircle, Star } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { useAuth } from '../context/AuthContext';
import { useQuery } from '@tanstack/react-query';
import api from '../lib/api';
import { useState, useMemo } from 'react';
import { subMonths, startOfYear, format } from 'date-fns';
import { useParams } from 'react-router-dom';

interface StudentEvaluation {
  id: string;
  demandId: string;
  demandTitle: string;
  schoolId: string;
  studentId: string;
  studentName: string;
  evaluatedBy: string;
  knowledge: number;
  punctuality: number;
  teaching: number;
  commitment: number;
  empathy: number;
  average: number;
  comments: string;
  createdAt: string;
  finishedAt: string;
}

interface StudentReport {
  studentId: string;
  studentName: string;
  from: string | null;
  to: string | null;
  schoolId: string | null;
  totalHours: number;
  requiredHours: number;
  remainingHours: number;
  requirementMetAt: string | null;
  performedLessons: number;
  validatedHoursInPeriod: number;
  pendingHours: number;
  canceledLessons: number;
  notCompletedLessons: number;
  averageReceived: number | null;
  evaluations: StudentEvaluation[];
}

interface School {
  id: string;
  name: string;
  cie: string;
}

export function Reports() {
  const { user } = useAuth();
  const { studentId: urlStudentId } = useParams();
  const studentId = urlStudentId || user?.id;
  
  const [selectedPeriod, setSelectedPeriod] = useState('3m');
  const [selectedSchoolId, setSelectedSchoolId] = useState('');

  const dateRange = useMemo(() => {
    const to = new Date();
    let from: Date;

    switch (selectedPeriod) {
      case '6m':
        from = subMonths(to, 6);
        break;
      case 'year':
        from = startOfYear(to);
        break;
      case '3m':
      default:
        from = subMonths(to, 3);
        break;
    }

    return {
      from: format(from, 'yyyy-MM-dd'),
      to: format(to, 'yyyy-MM-dd'),
    };
  }, [selectedPeriod]);

  const { data: schools } = useQuery<School[]>({
    queryKey: ['schools', 'active'],
    queryFn: async () => {
      const response = await api.get('/schools/active');
      return response.data;
    },
    retry: (failureCount, error: any) => {
      if (error.response?.status === 403 || error.response?.status === 401) return false;
      return failureCount < 3;
    }
  });

  const { data: report, isLoading } = useQuery<StudentReport>({
    queryKey: ['report', studentId, dateRange, selectedSchoolId],
    queryFn: async () => {
      const params: any = {
        from: dateRange.from,
        to: dateRange.to,
      };
      if (selectedSchoolId) {
        params.schoolId = selectedSchoolId;
      }
      const response = await api.get(`/reports/students/${studentId}`, { params });
      return response.data;
    },
    enabled: !!studentId,
    retry: (failureCount, error: any) => {
      if (error.response?.status === 403 || error.response?.status === 401) return false;
      return failureCount < 3;
    }
  });

  if (isLoading) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-brand-primary"></div>
      </div>
    );
  }

  return (
    <div className="flex-1">
      <Header 
        title={urlStudentId ? `Relatório de ${report?.studentName || 'Estudante'}` : "Meus Relatórios"} 
        subtitle={urlStudentId ? "Acompanhe o desempenho e progresso deste estudante." : "Analise seu desempenho e acompanhe seu progresso no estágio."} 
      />
      
      <div className="p-8 space-y-8">
        {/* Filtros */}
        <div className="flex flex-col md:flex-row gap-4 items-end justify-between bg-white p-6 rounded-2xl shadow-sm border border-border-default">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 flex-1 w-full">
            <div className="space-y-2">
              <label className="text-xs font-bold text-text-muted uppercase">Período</label>
              <select 
                value={selectedPeriod}
                onChange={(e) => setSelectedPeriod(e.target.value)}
                className="w-full h-10 px-3 rounded-lg border border-border-default bg-slate-50 text-sm"
              >
                <option value="3m">Últimos 3 meses</option>
                <option value="6m">Últimos 6 meses</option>
                <option value="year">Este ano</option>
              </select>
            </div>
            <div className="space-y-2">
              <label className="text-xs font-bold text-text-muted uppercase">Escola</label>
              <select 
                value={selectedSchoolId}
                onChange={(e) => setSelectedSchoolId(e.target.value)}
                className="w-full h-10 px-3 rounded-lg border border-border-default bg-slate-50 text-sm"
              >
                <option value="">Todas as escolas</option>
                {schools?.map(school => (
                  <option key={school.id} value={school.id}>{school.name}</option>
                ))}
              </select>
            </div>
            <div className="flex items-end">
              <Button variant="outline" className="w-full flex items-center justify-center gap-2">
                <Filter size={18} /> Filtrar
              </Button>
            </div>
          </div>
        </div>

        {/* Métricas */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
          <MetricCard 
            icon={<Calendar className="text-brand-primary" size={20} />}
            label="Total Horas"
            value={`${report?.totalHours || 0}h`}
            note={`Meta: ${report?.requiredHours || 0}h`}
          />
          <MetricCard 
            icon={<BookOpen className="text-brand-accent" size={20} />}
            label="Aulas Realizadas"
            value={report?.performedLessons?.toString() || '0'}
            note={`${report?.validatedHoursInPeriod || 0}h validadas`}
          />
          <MetricCard 
            icon={<Star className="text-orange-500" size={20} />}
            label="Média Recebida"
            value={report?.averageReceived?.toFixed(1) || 'N/A'}
            note={`${report?.evaluations?.length || 0} avaliações`}
          />
          <MetricCard 
            icon={<CheckCircle className="text-green-600" size={20} />}
            label="Horas Pendentes"
            value={`${report?.pendingHours || 0}h`}
            note="Aguardando validação"
          />
          <MetricCard 
            icon={<XCircle className="text-red-500" size={20} />}
            label="Cancelamentos"
            value={report?.canceledLessons?.toString() || '0'}
            note="No período selecionado"
          />
        </div>

        {/* Tabela de Detalhamento */}
        <Card className="p-0 overflow-hidden">
          <div className="p-6 border-b border-slate-100">
            <h3 className="text-lg font-bold text-text-heading">Avaliações Recebidas</h3>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-100">
                  <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Demanda</th>
                  <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Data</th>
                  <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider text-center">Média</th>
                  <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Comentários</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {report?.evaluations && report.evaluations.length > 0 ? (
                  report.evaluations.map((evaluation) => (
                    <tr key={evaluation.id} className="hover:bg-slate-50 transition-colors">
                      <td className="px-6 py-4 text-sm text-text-heading font-medium">{evaluation.demandTitle}</td>
                      <td className="px-6 py-4 text-sm text-text-body">
                        {format(new Date(evaluation.finishedAt), 'dd/MM/yyyy')}
                      </td>
                      <td className="px-6 py-4 text-center">
                        <span className="inline-flex items-center px-2 py-0.5 rounded bg-yellow-100 text-yellow-800 text-xs font-bold">
                          ★ {evaluation.average.toFixed(1)}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-sm text-text-body italic truncate max-w-xs">
                        "{evaluation.comments}"
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={4} className="px-6 py-8 text-center text-text-muted text-sm">
                      Nenhuma avaliação encontrada no período.
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
