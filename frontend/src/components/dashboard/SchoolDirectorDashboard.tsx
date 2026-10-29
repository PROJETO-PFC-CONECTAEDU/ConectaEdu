import React from 'react';
import { ClipboardList, DoorOpen, Activity, Trophy } from 'lucide-react';
import { MetricCard } from '../ui/MetricCard';

interface Props {
  stats: any;
}

export function SchoolDirectorDashboard({ stats }: Props) {
  return (
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
  );
}
