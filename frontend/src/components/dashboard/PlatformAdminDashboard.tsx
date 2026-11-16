import { School as SchoolIcon, Building2, Users, CheckCircle, Clock, Star, TrendingUp } from 'lucide-react';
import { MetricCard } from '../ui/MetricCard';

interface Props {
  stats: any;
}

export function PlatformAdminDashboard({ stats }: Props) {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <MetricCard 
          icon={<SchoolIcon className="text-brand-primary" size={24} />}
          label="Escolas Parceiras"
          value={stats.schoolsCount || 0}
          note="Instituições de ensino básico"
        />
        <MetricCard 
          icon={<Building2 className="text-brand-accent" size={24} />}
          label="Universidades"
          value={stats.universitiesCount || 0}
          note="Instituições de ensino superior"
        />
        <MetricCard 
          icon={<Users className="text-orange-500" size={24} />}
          label="Estudantes Ativos"
          value={stats.activeStudentsCount || 0}
          note="Estudantes validados"
        />
        <MetricCard 
          icon={<Clock className="text-yellow-500" size={24} />}
          label="Demandas em Aberto"
          value={stats.waitingDemands || 0}
          note="Aguardando universitários"
        />
        <MetricCard 
          icon={<CheckCircle className="text-green-500" size={24} />}
          label="Demandas Finalizadas"
          value={stats.finishedDemands || 0}
          note="Projetos concluídos com sucesso"
        />
      </div>

      {(stats.studentsAverageScore !== undefined || stats.classAverageScore !== undefined) && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {stats.studentsAverageScore !== undefined && (
            <MetricCard 
              icon={<Star className="text-yellow-500" size={24} />}
              label="Média de Desempenho dos Estudantes"
              value={stats.studentsAverageScore?.toFixed(1) || 'N/A'}
              note="Avaliação dada pelos diretores"
            />
          )}
          {stats.classAverageScore !== undefined && (
            <MetricCard 
              icon={<TrendingUp className="text-blue-500" size={24} />}
              label="Média de Satisfação com as Aulas"
              value={stats.classAverageScore?.toFixed(1) || 'N/A'}
              note="Avaliação dada pelos estudantes"
            />
          )}
        </div>
      )}
    </div>
  );
}
