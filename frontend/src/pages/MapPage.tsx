import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { MapComponent } from '../components/map/GoogleMap';

export function MapPage() {
  return (
    <div className="flex-1">
      <Header 
        title="Mapa da Rede" 
        subtitle="Visualize a localização das escolas e universidades parceiras." 
      />
      
      <div className="p-8">
        <Card className="p-4 bg-white shadow-sm border border-slate-200 overflow-hidden">
          <div className="mb-4 px-2">
            <h3 className="text-lg font-semibold text-text-heading">Localização Geográfica</h3>
            <p className="text-sm text-text-muted">Interaja com o mapa para explorar as instituições.</p>
          </div>
          <MapComponent />
        </Card>
      </div>
    </div>
  );
}
