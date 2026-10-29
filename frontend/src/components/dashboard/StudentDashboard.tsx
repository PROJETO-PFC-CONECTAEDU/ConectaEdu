import React from 'react';
import { Clock, Send, Star, BookOpen, Activity } from 'lucide-react';
import { MetricCard } from '../ui/MetricCard';
import { Card } from '../ui/Card';

interface Props {
  stats: any;
}

export function StudentDashboard({ stats }: Props) {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <MetricCard 
          icon={<Clock className="text-brand-primary" size={24} />}
          label="Minhas Horas"
          value={stats.myHours || 0}
          note="Total acumulado em projetos"
        />
        <MetricCard 
          icon={<Send className="text-brand-accent" size={24} />}
          label="Candidaturas"
          value={stats.applicationsCount || 0}
          note="Vagas que você demonstrou interesse"
        />
        <MetricCard 
          icon={<Star className="text-yellow-500" size={24} />}
          label="Áreas de Interesse"
          value={stats.interestAreas?.length || 0}
          note="Competências no seu perfil"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card className="p-6">
          <h3 className="text-lg font-bold text-text-heading mb-4 flex items-center gap-2">
            <Activity size={20} className="text-brand-primary" />
            Projeto Atual
          </h3>
          {stats.currentProject ? (
            <div className="bg-slate-50 p-4 rounded-lg border border-slate-100">
              <h4 className="font-bold text-text-heading">{stats.currentProject.title}</h4>
              <p className="text-sm text-text-body mt-1">{stats.currentProject.schoolName}</p>
              <div className="mt-4 flex items-center justify-between">
                <span className="text-xs font-medium px-2.5 py-0.5 rounded bg-blue-100 text-blue-800 uppercase">
                  {stats.currentProject.status}
                </span>
                <a href={`/demands/${stats.currentProject.id}`} className="text-brand-primary text-sm font-bold hover:underline">
                  Ver detalhes
                </a>
              </div>
            </div>
          ) : (
            <p className="text-text-muted text-sm italic">Você não está vinculado a nenhum projeto no momento.</p>
          )}
        </Card>

        <Card className="p-6">
          <h3 className="text-lg font-bold text-text-heading mb-4 flex items-center gap-2">
            <BookOpen size={20} className="text-brand-accent" />
            Minhas Áreas de Interesse
          </h3>
          <div className="flex flex-wrap gap-2">
            {stats.interestAreas && stats.interestAreas.length > 0 ? (
              stats.interestAreas.map((area: string) => (
                <span key={area} className="px-3 py-1 bg-brand-light text-brand-primary rounded-full text-xs font-medium">
                  {area}
                </span>
              ))
            ) : (
              <p className="text-text-muted text-sm italic">Nenhuma área de interesse cadastrada.</p>
            )}
          </div>
        </Card>
      </div>
    </div>
  );
}
