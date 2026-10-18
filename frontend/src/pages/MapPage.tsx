import { useQuery } from '@tanstack/react-query';
import api from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { MapComponent } from '../components/map/GoogleMap';

interface Demand {
  id: string;
  title: string;
  subject: string;
  status: string;
  schoolId: string;
}

interface School {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
  demands?: Demand[];
}

export function MapPage() {
  const { user } = useAuth();
  const { data: schools, isLoading: isLoadingSchools } = useQuery<School[]>({
    queryKey: ['schools', 'active'],
    queryFn: async () => {
      const response = await api.get('/schools/active');
      return response.data;
    },
  });

  const { data: demands, isLoading: isLoadingDemands } = useQuery<Demand[]>({
    queryKey: ['demands', 'available', user?.role],
    queryFn: async () => {
      const endpoint = user?.role === 'STUDENT' ? '/demands/available/student' : '/demands/available';
      const response = await api.get(endpoint);
      return response.data;
    },
  });

  const isLoading = isLoadingSchools || isLoadingDemands;

  const schoolsWithDemands = schools?.map(school => ({
    ...school,
    demands: demands?.filter(demand => demand.schoolId === school.id)
  }));

  return (
    <div className="flex-1">
      <Header 
        title="Mapa da Rede" 
        subtitle="Visualize a localização das escolas e as demandas disponíveis." 
      />
      
      <div className="p-8">
        <Card className="p-4 bg-white shadow-sm border border-slate-200 overflow-hidden">
          <div className="mb-4 px-2">
            <h3 className="text-lg font-semibold text-text-heading">Localização Geográfica</h3>
            <p className="text-sm text-text-muted">Interaja com o mapa para explorar as instituições e suas demandas.</p>
          </div>
          {isLoading ? (
            <div className="w-full h-[600px] flex items-center justify-center bg-slate-50 rounded-lg">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-brand-primary"></div>
            </div>
          ) : (
            <MapComponent schools={schoolsWithDemands} />
          )}
        </Card>
      </div>
    </div>
  );
}
