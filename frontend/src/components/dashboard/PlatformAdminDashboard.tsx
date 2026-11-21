import { School as SchoolIcon, Building2, Users, CheckCircle, Clock, Star, TrendingUp, BarChart3 } from 'lucide-react';
import { MetricCard } from '../ui/MetricCard';
import { Card } from '../ui/Card';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer
} from 'recharts';

interface Props {
  stats: any;
}

export function PlatformAdminDashboard({ stats }: Props) {
  const chartData = stats.lessonsByPeriod?.map((item: any) => ({
    period: item.PERIOD || item.period,
    count: item.COUNT || item.count
  })) || [];

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

      <Card className="p-6">
        <div className="flex items-center gap-2 mb-6">
          <BarChart3 className="text-brand-primary" size={20} />
          <h3 className="text-lg font-bold text-text-heading">Atividades Realizadas no Sistema (por Período)</h3>
        </div>
        
        <div className="h-[300px] w-full">
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="period" />
                <YAxis />
                <Tooltip 
                  contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                />
                <Bar dataKey="count" fill="#f97316" radius={[4, 4, 0, 0]} name="Aulas" barSize={32} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-full flex items-center justify-center text-text-muted italic text-sm">
              Dados de atividades aparecerão conforme aulas forem registradas no sistema.
            </div>
          )}
        </div>
      </Card>
    </div>
  );
}
