import { Users, Activity, Clock, CheckCircle, BarChart3, Star, TrendingUp, ScatterChart as ScatterIcon } from 'lucide-react';
import { MetricCard } from '../ui/MetricCard';
import { Card } from '../ui/Card';
import { 
  BarChart, 
  Bar, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer,
  ScatterChart,
  Scatter,
  ZAxis,
  Cell
} from 'recharts';

interface Props {
  stats: any;
}

export function UniversityAdminDashboard({ stats }: Props) {
  const chartData = stats.lessonsByPeriod?.map((item: any) => ({
    period: item.PERIOD || item.period,
    count: item.COUNT || item.count
  })) || [];

  const nineBoxData = stats.nineBoxDistribution?.map((item: any) => ({
    performance: item.PERFORMANCE || item.performance,
    potential: item.POTENTIAL || item.potential,
    count: item.COUNT || item.count
  })) || [];

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <MetricCard 
          icon={<Users className="text-brand-primary" size={24} />}
          label="Meus Estudantes"
          value={stats.myStudentsCount || 0}
          note="Cadastrados na sua instituição"
        />
        <MetricCard 
          icon={<Activity className="text-brand-accent" size={24} />}
          label="Estudantes em Atividade"
          value={stats.activeStudentsCount || 0}
          note="Participando de projetos no momento"
        />
        <MetricCard 
          icon={<Clock className="text-orange-500" size={24} />}
          label="Horas Totais"
          value={stats.totalHours || 0}
          note="Horas de estágio validadas"
        />
        <MetricCard 
          icon={<CheckCircle className="text-green-500" size={24} />}
          label="Demandas Atendidas"
          value={stats.completedDemands || 0}
          note="Projetos concluídos pelos seus estudantes"
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <MetricCard 
          icon={<Star className="text-yellow-500" size={24} />}
          label="Média dos Estudantes"
          value={stats.studentsAverageScore?.toFixed(1) || 'N/A'}
          note="Avaliação dada pelos diretores"
        />
        <MetricCard 
          icon={<TrendingUp className="text-blue-500" size={24} />}
          label="Média de Satisfação Escolar"
          value={stats.classAverageScore?.toFixed(1) || 'N/A'}
          note="Avaliação das aulas pelos estudantes"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card className="p-6">
          <div className="flex items-center gap-2 mb-6">
            <BarChart3 className="text-brand-primary" size={20} />
            <h3 className="text-lg font-bold text-text-heading">Atividades Realizadas (por Período)</h3>
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
                  <Bar dataKey="count" fill="#0ea5e9" radius={[4, 4, 0, 0]} name="Aulas" barSize={32} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-text-muted italic text-sm">
                Dados de atividades aparecerão aqui conforme os alunos registrarem aulas.
              </div>
            )}
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center gap-2 mb-6">
            <ScatterIcon className="text-brand-accent" size={20} />
            <h3 className="text-lg font-bold text-text-heading">Impacto Pedagógico na Escola (Nine Box)</h3>
          </div>
          
          <div className="h-[300px] w-full">
            {nineBoxData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <ScatterChart margin={{ top: 20, right: 20, bottom: 20, left: 20 }}>
                  <CartesianGrid />
                  <XAxis type="number" dataKey="performance" name="Desempenho" domain={[0.5, 3.5]} ticks={[1, 2, 3]} />
                  <YAxis type="number" dataKey="potential" name="Potencial" domain={[0.5, 3.5]} ticks={[1, 2, 3]} />
                  <ZAxis type="number" dataKey="count" range={[100, 1000]} name="Qtd. Aulas" />
                  <Tooltip cursor={{ strokeDasharray: '3 3' }} />
                  <Scatter name="Aulas" data={nineBoxData} fill="#8884d8">
                    {nineBoxData.map((entry: any, index: number) => (
                      <Cell key={`cell-${index}`} fill={entry.performance + entry.potential >= 5 ? '#10b981' : entry.performance + entry.potential >= 3 ? '#f59e0b' : '#ef4444'} />
                    ))}
                  </Scatter>
                </ScatterChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-full flex items-center justify-center text-text-muted italic text-sm">
                A distribuição aparecerá conforme os estudantes preencherem o Nine Box nas aulas.
              </div>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
