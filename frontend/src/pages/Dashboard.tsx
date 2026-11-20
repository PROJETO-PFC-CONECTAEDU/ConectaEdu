import { useState, useEffect, useMemo } from 'react';
import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { useQuery } from '@tanstack/react-query';
import api from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { PlatformAdminDashboard } from '../components/dashboard/PlatformAdminDashboard';
import { UniversityAdminDashboard } from '../components/dashboard/UniversityAdminDashboard';
import { SchoolDirectorDashboard } from '../components/dashboard/SchoolDirectorDashboard';
import { StudentDashboard } from '../components/dashboard/StudentDashboard';
import { Filter } from 'lucide-react';

export function Dashboard() {
  const { user } = useAuth();
  const [schoolId, setSchoolId] = useState<string>('');
  const [demandId, setDemandId] = useState<string>('');
  const [studentId, setStudentId] = useState<string>('');
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [schools, setSchools] = useState<any[]>([]);
  const [demands, setDemands] = useState<any[]>([]);
  const [students, setStudents] = useState<any[]>([]);

  useEffect(() => {
    if (user?.role === 'PLATFORM_ADMIN' || user?.role === 'UNIVERSITY_ADMIN') {
      api.get('/schools/active')
        .then(res => setSchools(res.data))
        .catch(() => setSchools([]));
    }
    
    if (user?.role === 'PLATFORM_ADMIN' || user?.role === 'SCHOOL_DIRECTOR') {
      api.get('/demands')
        .then(res => setDemands(res.data))
        .catch(() => setDemands([]));
    }

    if (user?.role === 'PLATFORM_ADMIN' || user?.role === 'UNIVERSITY_ADMIN') {
      api.get('/students')
        .then(res => setStudents(res.data))
        .catch(() => setStudents([]));
    }
  }, [user]);

  const filteredDemands = useMemo(() => {
    if (!schoolId) return demands;
    return demands.filter(d => d.schoolId === schoolId || d.school?.id === schoolId);
  }, [demands, schoolId]);

  // Limpar demanda selecionada se ela não estiver na lista filtrada
  useEffect(() => {
    if (demandId && !filteredDemands.find(d => d.id === demandId)) {
      setDemandId('');
    }
  }, [filteredDemands, demandId]);

  const { data: stats, isLoading } = useQuery({
    queryKey: ['dashboard-stats', schoolId, demandId, studentId, startDate, endDate],
    queryFn: async () => {
      const params: any = {};
      if (schoolId) params.schoolId = schoolId;
      if (demandId) params.demandId = demandId;
      if (studentId) params.studentId = studentId;
      if (startDate) params.startDate = startDate;
      if (endDate) params.endDate = endDate;
      const response = await api.get('/dashboard/stats', { params });
      return response.data.statistics;
    },
    retry: (failureCount, error: any) => {
      if (error.response?.status === 403 || error.response?.status === 401) return false;
      return failureCount < 3;
    }
  });

  const renderDashboard = () => {
    if (isLoading) {
      return <div className="p-8 text-text-body">Carregando estatísticas...</div>;
    }

    if (!stats) {
      return <div className="p-8 text-text-body">Não foi possível carregar as estatísticas.</div>;
    }

    switch (user?.role) {
      case 'PLATFORM_ADMIN':
        return <PlatformAdminDashboard stats={stats} />;
      case 'UNIVERSITY_ADMIN':
        return <UniversityAdminDashboard stats={stats} />;
      case 'SCHOOL_DIRECTOR':
        return <SchoolDirectorDashboard stats={stats} />;
      case 'STUDENT':
        return <StudentDashboard stats={stats} />;
      default:
        return (
          <Card className="p-8">
            <h3 className="text-xl font-bold text-text-heading mb-4">Bem-vindo ao ConectaEdu</h3>
            <p className="text-text-body">
              Você está logado no sistema, mas seu perfil não possui uma visão de dashboard específica configurada.
            </p>
          </Card>
        );
    }
  };

  const showSchoolFilter = user?.role === 'PLATFORM_ADMIN' || user?.role === 'UNIVERSITY_ADMIN';
  const showDemandFilter = user?.role === 'PLATFORM_ADMIN' || user?.role === 'UNIVERSITY_ADMIN' || user?.role === 'SCHOOL_DIRECTOR';
  const showStudentFilter = user?.role === 'PLATFORM_ADMIN' || user?.role === 'UNIVERSITY_ADMIN';
  const showPeriodFilter = true;

  return (
    <div className="flex-1">
      <Header 
        title="Dashboard" 
        subtitle={`Bem-vindo, ${user?.name}. Veja o resumo das suas atividades.`} 
      />
      
      <div className="p-8 space-y-8">
        {(showSchoolFilter || showDemandFilter) && (
          <Card className="p-4 bg-gray-50 border-none shadow-sm flex flex-wrap items-center gap-4">
            <div className="flex items-center gap-2 text-brand-primary font-semibold mr-2">
              <Filter size={20} />
              <span>Filtros:</span>
            </div>

            {showSchoolFilter && (
              <div className="flex flex-col min-w-[200px]">
                <label className="text-xs text-text-body mb-1 font-medium">Escola</label>
                <select 
                  value={schoolId} 
                  onChange={(e) => setSchoolId(e.target.value)}
                  className="px-3 py-2 border border-border-light rounded-lg bg-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary"
                >
                  <option value="">Todas as Escolas</option>
                  {schools.map(school => (
                    <option key={school.id} value={school.id}>{school.name}</option>
                  ))}
                </select>
              </div>
            )}

            {showDemandFilter && (
              <div className="flex flex-col min-w-[200px]">
                <label className="text-xs text-text-body mb-1 font-medium">
                  {user?.role === 'SCHOOL_DIRECTOR' ? 'Aula de Reforço / Projeto' : 'Demanda / Projeto'}
                </label>
                <select 
                  value={demandId} 
                  onChange={(e) => setDemandId(e.target.value)}
                  className="px-3 py-2 border border-border-light rounded-lg bg-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary"
                >
                  <option value="">Todas as Demandas</option>
                  {filteredDemands.map(demand => (
                    <option key={demand.id} value={demand.id}>{demand.title}</option>
                  ))}
                </select>
              </div>
            )}

            {showStudentFilter && (
              <div className="flex flex-col min-w-[200px]">
                <label className="text-xs text-text-body mb-1 font-medium">Estudante</label>
                <select 
                  value={studentId} 
                  onChange={(e) => setStudentId(e.target.value)}
                  className="px-3 py-2 border border-border-light rounded-lg bg-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary"
                >
                  <option value="">Todos os Estudantes</option>
                  {students.map(student => (
                    <option key={student.id} value={student.id}>{student.name}</option>
                  ))}
                </select>
              </div>
            )}

            {showPeriodFilter && (
              <>
                <div className="flex flex-col">
                  <label className="text-xs text-text-body mb-1 font-medium">Data Inicial</label>
                  <input 
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    className="px-3 py-2 border border-border-light rounded-lg bg-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary"
                  />
                </div>
                <div className="flex flex-col">
                  <label className="text-xs text-text-body mb-1 font-medium">Data Final</label>
                  <input 
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    className="px-3 py-2 border border-border-light rounded-lg bg-white text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary"
                  />
                </div>
              </>
            )}

            {(schoolId || demandId || studentId || startDate || endDate) && (
              <button 
                onClick={() => { 
                  setSchoolId(''); 
                  setDemandId(''); 
                  setStudentId('');
                  setStartDate('');
                  setEndDate('');
                }}
                className="mt-5 text-xs text-brand-primary hover:underline font-medium"
              >
                Limpar filtros
              </button>
            )}
          </Card>
        )}

        {renderDashboard()}
      </div>
    </div>
  );
}
