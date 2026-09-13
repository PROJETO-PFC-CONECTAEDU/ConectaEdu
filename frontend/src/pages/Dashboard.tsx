import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { useQuery } from '@tanstack/react-query';
import api from '../lib/api';
import { 
  Building2,
  School as SchoolIcon,
  Users
} from 'lucide-react';

export function Dashboard() {
  const { data: schools } = useQuery({
    queryKey: ['schools'],
    queryFn: async () => {
      const response = await api.get('/schools');
      return response.data;
    },
  });

  const { data: universities } = useQuery({
    queryKey: ['universities'],
    queryFn: async () => {
      const response = await api.get('/universities');
      return response.data;
    },
  });

  const { data: students } = useQuery({
    queryKey: ['students'],
    queryFn: async () => {
      const response = await api.get('/students');
      return response.data;
    },
  });

  return (
    <div className="flex-1">
      <Header 
        title="Dashboard" 
        subtitle="Bem-vindo ao ConectaEdu. Veja o resumo da plataforma." 
      />
      
      <div className="p-8 space-y-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <MetricCard 
            icon={<SchoolIcon className="text-brand-primary" size={24} />}
            label="Escolas Parceiras"
            value={schools?.length?.toString() || '0'}
            note="Instituições cadastradas"
          />
          <MetricCard 
            icon={<Building2 className="text-brand-accent" size={24} />}
            label="Universidades"
            value={universities?.length?.toString() || '0'}
            note="Instituições de ensino superior"
          />
          <MetricCard 
            icon={<Users className="text-orange-500" size={24} />}
            label="Estudantes"
            value={students?.length?.toString() || '0'}
            note="Estudantes ativos na plataforma"
          />
        </div>

        <Card className="p-8">
          <h3 className="text-xl font-bold text-text-heading mb-4">Plataforma ConectaEdu</h3>
          <p className="text-text-body leading-relaxed">
            Esta é a versão MVP da plataforma ConectaEdu conectada ao backend real.
          </p>
          <p className="text-text-body mt-4 leading-relaxed">
            As seções de agenda, vagas e avaliações foram removidas por não possuírem implementação no backend no momento, conforme solicitado na refatoração.
          </p>
        </Card>
      </div>
    </div>
  );
}

function MetricCard({ icon, label, value, note }: { icon: React.ReactNode, label: string, value: string, note: string }) {
  return (
    <Card className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div className="p-2 rounded-lg bg-slate-50">
          {icon}
        </div>
      </div>
      <div>
        <p className="text-sm text-text-body font-medium">{label}</p>
        <h4 className="text-2xl font-bold text-text-heading mt-1">{value}</h4>
        <p className="text-xs text-text-muted mt-1">{note}</p>
      </div>
    </Card>
  );
}
