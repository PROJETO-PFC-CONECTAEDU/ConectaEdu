import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { useQuery } from '@tanstack/react-query';
import api from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { PlatformAdminDashboard } from '../components/dashboard/PlatformAdminDashboard';
import { UniversityAdminDashboard } from '../components/dashboard/UniversityAdminDashboard';
import { SchoolDirectorDashboard } from '../components/dashboard/SchoolDirectorDashboard';
import { StudentDashboard } from '../components/dashboard/StudentDashboard';

export function Dashboard() {
  const { user } = useAuth();

  const { data: stats, isLoading } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: async () => {
      const response = await api.get('/dashboard/stats');
      return response.data.statistics;
    },
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

  return (
    <div className="flex-1">
      <Header 
        title="Dashboard" 
        subtitle={`Bem-vindo, ${user?.name}. Veja o resumo das suas atividades.`} 
      />
      
      <div className="p-8 space-y-8">
        {renderDashboard()}
        
        <Card className="p-8">
          <h3 className="text-xl font-bold text-text-heading mb-4">Plataforma ConectaEdu</h3>
          <p className="text-text-body leading-relaxed">
            Esta plataforma conecta estudantes universitários a escolas para apoio pedagógico, promovendo a formação acadêmica e o impacto social.
          </p>
        </Card>
      </div>
    </div>
  );
}
