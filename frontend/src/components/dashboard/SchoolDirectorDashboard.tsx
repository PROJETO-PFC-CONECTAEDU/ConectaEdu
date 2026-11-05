import { ClipboardList, DoorOpen, Activity, Trophy, BarChart3 } from 'lucide-react';
import { MetricCard } from '../ui/MetricCard';
import { Card } from '../ui/Card';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend, 
  ResponsiveContainer 
} from 'recharts';

interface Props {
  stats: any;
}

export function SchoolDirectorDashboard({ stats }: Props) {
  // Transformar dados para o gráfico se necessário
  const chartData = stats.lessonsByPeriodAndDemand?.reduce((acc: any[], item: any) => {
    const existing = acc.find(d => d.period === item.PERIOD);
    if (existing) {
      existing[item.DEMAND] = item.COUNT;
    } else {
      acc.push({ period: item.PERIOD, [item.DEMAND]: item.COUNT });
    }
    return acc;
  }, []) || [];

  const demands = Array.from(new Set(stats.lessonsByPeriodAndDemand?.map((item: any) => item.DEMAND))) as string[];
  const colors = ['#f97316', '#0ea5e9', '#10b981', '#f59e0b', '#6366f1'];

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <MetricCard 
          icon={<ClipboardList className="text-brand-primary" size={24} />}
          label="Minhas Demandas"
          value={stats.myDemandsCount || 0}
          note="Total de solicitações criadas"
        />
        <MetricCard 
          icon={<DoorOpen className="text-yellow-500" size={24} />}
          label="Vagas em Aberto"
          value={stats.openVacanciesCount || 0}
          note="Demandas aguardando universitário"
        />
        <MetricCard 
          icon={<Activity className="text-brand-accent" size={24} />}
          label="Aulas em Andamento"
          value={stats.ongoingClassesCount || 0}
          note="Projetos ocorrendo no momento"
        />
        <MetricCard 
          icon={<Trophy className="text-green-500" size={24} />}
          label="Histórico de Apoio"
          value={stats.successfulDemandsCount || 0}
          note="Demandas concluídas com sucesso"
        />
      </div>

      <Card className="p-6">
        <div className="flex items-center gap-2 mb-6">
          <BarChart3 className="text-brand-primary" size={20} />
          <h3 className="text-lg font-bold text-text-heading">Aulas Realizadas por Período e Demanda</h3>
        </div>
        
        <div className="h-[350px] w-full">
          {chartData.length > 0 ? (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="period" />
                <YAxis />
                <Tooltip 
                  contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                />
                <Legend />
                {demands.map((demand, index) => (
                  <Bar 
                    key={demand} 
                    dataKey={demand} 
                    fill={colors[index % colors.length]} 
                    radius={[4, 4, 0, 0]} 
                  />
                ))}
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-full flex items-center justify-center text-text-muted italic">
              Nenhum dado disponível para o gráfico.
            </div>
          )}
        </div>
      </Card>
    </div>
  );
}
