import React from 'react';
import { Users, Activity, Clock, CheckCircle } from 'lucide-react';
import { MetricCard } from '../ui/MetricCard';

interface Props {
  stats: any;
}

export function UniversityAdminDashboard({ stats }: Props) {
  return (
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
        note="Horas complementares validadas"
      />
      <MetricCard 
        icon={<CheckCircle className="text-green-500" size={24} />}
        label="Demandas Atendidas"
        value={stats.completedDemands || 0}
        note="Projetos concluídos pelos seus alunos"
      />
    </div>
  );
}
