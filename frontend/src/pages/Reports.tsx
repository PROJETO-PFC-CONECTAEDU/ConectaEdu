import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { MetricCard } from '../components/ui/MetricCard';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import { Download, Filter, Calendar, Users, BookOpen, CheckCircle, XCircle } from 'lucide-react';
import { Button } from '../components/ui/Button';

export function Reports() {
  const barData = [
    { name: 'Ago', horas: 25 },
    { name: 'Set', horas: 32 },
    { name: 'Out', horas: 45 },
    { name: 'Nov', horas: 18 },
  ];

  const pieData = [
    { name: 'Matemática', value: 45 },
    { name: 'Português', value: 30 },
    { name: 'Artes', value: 15 },
    { name: 'Ciências', value: 10 },
  ];

  const COLORS = ['#2563eb', '#10b981', '#f59e0b', '#ef4444'];

  const reportTable = [
    { id: '10254', student: 'Maria Silva', school: 'E.M. Paulo Freire', rating: 4.8, hours: 45 },
    { id: '10255', student: 'Maria Silva', school: 'C.E. Dom Pedro II', rating: 5.0, hours: 32 },
    { id: '10256', student: 'Maria Silva', school: 'Creche M. Casulo', rating: 4.5, hours: 25 },
  ];

  return (
    <div className="flex-1">
      <Header 
        title="Relatórios" 
        subtitle="Analise seu desempenho e exporte relatórios consolidados." 
      />
      
      <div className="p-8 space-y-8">
        {/* Filtros */}
        <div className="flex flex-col md:flex-row gap-4 items-end justify-between bg-white p-6 rounded-2xl shadow-sm border border-border-default">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 flex-1 w-full">
            <div className="space-y-2">
              <label className="text-xs font-bold text-text-muted uppercase">Período</label>
              <select className="w-full h-10 px-3 rounded-lg border border-border-default bg-slate-50 text-sm">
                <option>Últimos 3 meses</option>
                <option>Últimos 6 meses</option>
                <option>Este ano</option>
              </select>
            </div>
            <div className="space-y-2">
              <label className="text-xs font-bold text-text-muted uppercase">Escola</label>
              <select className="w-full h-10 px-3 rounded-lg border border-border-default bg-slate-50 text-sm">
                <option>Todas as escolas</option>
              </select>
            </div>
            <div className="space-y-2">
              <label className="text-xs font-bold text-text-muted uppercase">Matéria</label>
              <select className="w-full h-10 px-3 rounded-lg border border-border-default bg-slate-50 text-sm">
                <option>Todas as matérias</option>
              </select>
            </div>
            <div className="flex items-end">
              <Button variant="outline" className="w-full flex items-center justify-center gap-2">
                <Filter size={18} /> Filtrar
              </Button>
            </div>
          </div>
          <Button className="flex items-center gap-2 px-6">
            <Download size={18} /> Exportar PDF
          </Button>
        </div>

        {/* Métricas */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6">
          <MetricCard 
            icon={<Calendar className="text-brand-primary" size={20} />}
            label="Total Horas"
            value="120h"
            note="+12h este mês"
          />
          <MetricCard 
            icon={<BookOpen className="text-brand-accent" size={20} />}
            label="Aulas Realizadas"
            value="45"
            note="Meta: 60"
          />
          <MetricCard 
            icon={<Users className="text-orange-500" size={20} />}
            label="Alunos Atendidos"
            value="12"
            note="Atendimento individual"
          />
          <MetricCard 
            icon={<CheckCircle className="text-green-600" size={20} />}
            label="Taxa de Presença"
            value="98%"
            note="Excelente"
          />
          <MetricCard 
            icon={<XCircle className="text-red-500" size={20} />}
            label="Cancelamentos"
            value="2"
            note="Justificados"
          />
        </div>

        {/* Gráficos */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <Card className="p-8">
            <h3 className="text-lg font-bold text-text-heading mb-8">Horas de Estágio Realizadas por Mês</h3>
            <div className="h-[300px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={barData}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e2e8f0" />
                  <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#64748b', fontSize: 12}} dy={10} />
                  <YAxis axisLine={false} tickLine={false} tick={{fill: '#64748b', fontSize: 12}} />
                  <Tooltip 
                    cursor={{fill: '#f8fafc'}}
                    contentStyle={{borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'}}
                  />
                  <Bar dataKey="horas" fill="#2563eb" radius={[4, 4, 0, 0]} barSize={40} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </Card>

          <Card className="p-8">
            <h3 className="text-lg font-bold text-text-heading mb-8">Distribuição por Matéria</h3>
            <div className="h-[300px] w-full">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {pieData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend verticalAlign="bottom" height={36}/>
                </PieChart>
              </ResponsiveContainer>
            </div>
          </Card>
        </div>

        {/* Tabela de Detalhamento */}
        <Card className="p-0 overflow-hidden">
          <div className="p-6 border-b border-slate-100">
            <h3 className="text-lg font-bold text-text-heading">Detalhamento de Atividades</h3>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-100">
                  <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Código</th>
                  <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Estudante</th>
                  <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider">Escola</th>
                  <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider text-center">Avaliação Média</th>
                  <th className="px-6 py-4 text-xs font-semibold text-text-muted uppercase tracking-wider text-center">Horas</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {reportTable.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-6 py-4 text-sm text-brand-primary font-bold">#{item.id}</td>
                    <td className="px-6 py-4 text-sm text-text-heading font-medium">{item.student}</td>
                    <td className="px-6 py-4 text-sm text-text-body">{item.school}</td>
                    <td className="px-6 py-4 text-center">
                      <span className="inline-flex items-center px-2 py-0.5 rounded bg-yellow-100 text-yellow-800 text-xs font-bold">
                        ★ {item.rating}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-text-heading font-bold text-center">{item.hours}h</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    </div>
  );
}
