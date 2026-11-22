import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { MetricCard } from '../components/ui/MetricCard';
import { Filter, Calendar, BookOpen, CheckCircle, XCircle, Star, Search } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useQuery } from '@tanstack/react-query';
import api from '../lib/api';
import { useState, useMemo, useEffect } from 'react';
import { format } from 'date-fns';
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
  canceledByStudent: number;
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
  
  const [tempSchoolId, setTempSchoolId] = useState<string>('');
  const [tempStartDate, setTempStartDate] = useState<string>('');
  const [tempEndDate, setTempEndDate] = useState<string>('');
  const [tempPeriod, setTempPeriod] = useState<string>('');

  const [appliedSchoolId, setAppliedSchoolId] = useState<string>('');
  const [appliedStartDate, setAppliedStartDate] = useState<string>('');
  const [appliedEndDate, setAppliedEndDate] = useState<string>('');

  const handlePeriodChange = (value: string) => {
    setTempPeriod(value);
    if (value === 'custom') return;
    if (value === '') {
      setTempStartDate('');
      setTempEndDate('');
      return;
    }

    const now = new Date();
    const end = now.toISOString().split('T')[0];
    const d = new Date();
    d.setDate(d.getDate() - parseInt(value));
    const start = d.toISOString().split('T')[0];

    setTempStartDate(start);
    setTempEndDate(end);
  };

  const handleApplyFilters = () => {
    setAppliedSchoolId(tempSchoolId);
    setAppliedStartDate(tempStartDate);
    setAppliedEndDate(tempEndDate);
  };

  const handleClearFilters = () => {
    setTempSchoolId('');
    setTempStartDate('');
    setTempEndDate('');
    setTempPeriod('');
    setAppliedSchoolId('');
    setAppliedStartDate('');
    setAppliedEndDate('');
  };

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
    queryKey: ['report', studentId, appliedSchoolId, appliedStartDate, appliedEndDate],
    queryFn: async () => {
      const params: any = {};
      if (appliedStartDate) params.from = appliedStartDate;
      if (appliedEndDate) params.to = appliedEndDate;
      if (appliedSchoolId) params.schoolId = appliedSchoolId;
      
      const response = await api.get(`/reports/students/${studentId}`, { params });
      return response.data;
    },
    enabled: !!studentId,
    retry: (failureCount, error: any) => {
      if (error.response?.status === 403 || error.response?.status === 401) return false;
      return failureCount < 3;
    }
  });

  const showSchoolFilter = user?.role === 'PLATFORM_ADMIN' || user?.role === 'UNIVERSITY_ADMIN';
  const showPeriodFilter = true;

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
        {(showSchoolFilter || showPeriodFilter) && (
          <Card className="p-4 bg-gray-50 border-none shadow-sm flex flex-wrap items-center gap-4">
            <div className="flex items-center gap-2 text-brand-primary font-semibold mr-2">
              <Filter size={20} />
              <span>Filtros:</span>
            </div>

            {showSchoolFilter && (
              <div className="flex flex-col min-w-[200px]">
                <label className="text-xs text-text-body mb-1 font-medium">Escola</label>
                <select 
                  value={tempSchoolId} 
                  onChange={(e) => setTempSchoolId(e.target.value)}
                  className="px-3 py-2 border border-border-light rounded-lg bg-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary"
                >
                  <option value="">Todas as Escolas</option>
                  {schools?.map(school => (
                    <option key={school.id} value={school.id}>{school.name}</option>
                  ))}
                </select>
              </div>
            )}

            {showPeriodFilter && (
              <>
                <div className="flex flex-col min-w-[150px]">
                  <label className="text-xs text-text-body mb-1 font-medium">Período</label>
                  <select 
                    value={tempPeriod} 
                    onChange={(e) => handlePeriodChange(e.target.value)}
                    className="px-3 py-2 border border-border-light rounded-lg bg-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary"
                  >
                    <option value="">Todo o período</option>
                    <option value="7">Últimos 7 dias</option>
                    <option value="30">Últimos 30 dias</option>
                    <option value="60">Últimos 60 dias</option>
                    <option value="90">Últimos 90 dias</option>
                    <option value="custom">Customizado</option>
                  </select>
                </div>

                {(tempPeriod === 'custom' || tempStartDate || tempEndDate) && (
                  <>
                    <div className="flex flex-col">
                      <label className="text-xs text-text-body mb-1 font-medium">Data Inicial</label>
                      <input 
                        type="date"
                        value={tempStartDate}
                        onChange={(e) => {
                          setTempStartDate(e.target.value);
                          setTempPeriod('custom');
                        }}
                        className="px-3 py-2 border border-border-light rounded-lg bg-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary"
                      />
                    </div>
                    <div className="flex flex-col">
                      <label className="text-xs text-text-body mb-1 font-medium">Data Final</label>
                      <input 
                        type="date"
                        value={tempEndDate}
                        onChange={(e) => {
                          setTempEndDate(e.target.value);
                          setTempPeriod('custom');
                        }}
                        className="px-3 py-2 border border-border-light rounded-lg bg-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary"
                      />
                    </div>
                  </>
                )}
              </>
            )}

            <div className="flex items-center gap-4 ml-auto">
              <Button 
                onClick={handleApplyFilters}
                className="flex items-center gap-2"
                size="sm"
              >
                <Search size={16} />
                Filtrar
              </Button>

              {(tempSchoolId || tempStartDate || tempEndDate || tempPeriod || appliedSchoolId || appliedStartDate || appliedEndDate) && (
                <button 
                  onClick={handleClearFilters}
                  className="text-xs text-brand-primary hover:underline font-medium"
                >
                  Limpar filtros
                </button>
              )}
            </div>
          </Card>
        )}

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
            note={report?.canceledByStudent ? `${report.canceledByStudent} pelo estudante` : "Total no período"}
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
