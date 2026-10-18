import { useCallback, useState } from 'react';
import { GoogleMap, Marker, InfoWindow } from '@react-google-maps/api';
import { ClipboardList, ExternalLink, MapPin, List, X, ChevronRight } from 'lucide-react';
import { Link } from 'react-router-dom';

interface Demand {
  id: string;
  title: string;
  subject: string;
  status: string;
}

interface School {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
  demands?: Demand[];
}

interface MapProps {
  schools?: School[];
}

const containerStyle = {
  width: '100%',
  height: '600px',
  borderRadius: '12px'
};

const center = {
  lat: -23.55052,
  lng: -46.633308
};

export function MapComponent({ schools = [] }: MapProps) {
  const [selectedSchool, setSelectedSchool] = useState<School | null>(null);
  const [map, setMap] = useState<google.maps.Map | null>(null);
  const [isMenuOpen, setIsMenuOpen] = useState(true);

  const onLoad = useCallback(function callback(mapInstance: google.maps.Map) {
    setMap(mapInstance);
    if (schools.length > 0) {
      const bounds = new window.google.maps.LatLngBounds();
      schools.forEach(school => {
        if (school.latitude && school.longitude) {
          bounds.extend({ lat: school.latitude, lng: school.longitude });
        }
      });
      mapInstance.fitBounds(bounds);
    } else {
      const bounds = new window.google.maps.LatLngBounds(center);
      mapInstance.fitBounds(bounds);
    }
  }, [schools]);

  const onUnmount = useCallback(function callback() {
    setMap(null);
  }, []);

  const handleSelectSchool = (school: School) => {
    setSelectedSchool(school);
    if (map && school.latitude && school.longitude) {
      map.panTo({ lat: school.latitude, lng: school.longitude });
      map.setZoom(15);
    }
  };

  // Coleta todas as demandas para a lista lateral
  const allDemands = schools.flatMap(school => 
    (school.demands || []).map(demand => ({
      ...demand,
      schoolName: school.name,
      school: school
    }))
  );

  return (
    <div className="w-full h-full min-h-[600px] relative">
      {/* Botão para abrir menu se estiver fechado */}
      {!isMenuOpen && (
        <button 
          onClick={() => setIsMenuOpen(true)}
          className="absolute top-4 left-4 z-10 bg-white p-2 rounded-lg shadow-md border border-slate-200 flex items-center gap-2 hover:bg-slate-50 transition-colors"
        >
          <List size={20} className="text-brand-primary" />
          <span className="text-sm font-bold text-text-heading">Ver Demandas</span>
        </button>
      )}

      {/* Menu Flutuante */}
      {isMenuOpen && (
        <div className="absolute top-4 left-4 z-10 w-80 max-h-[calc(100%-2rem)] bg-white rounded-xl shadow-xl border border-slate-200 flex flex-col overflow-hidden">
          <div className="p-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
            <div className="flex items-center gap-2">
              <ClipboardList size={20} className="text-brand-primary" />
              <h3 className="font-bold text-text-heading">Demandas Ativas</h3>
            </div>
            <button 
              onClick={() => setIsMenuOpen(false)}
              className="text-text-muted hover:text-text-heading p-1 rounded-md hover:bg-slate-200 transition-colors"
            >
              <X size={18} />
            </button>
          </div>

          <div className="flex-1 overflow-y-auto p-2 space-y-2 max-h-[500px]">
            {allDemands.length > 0 ? (
              allDemands.map(demand => (
                <div 
                  key={demand.id}
                  onClick={() => handleSelectSchool(demand.school)}
                  className="p-3 rounded-lg border border-slate-100 hover:border-brand-primary/30 hover:bg-brand-primary/5 cursor-pointer transition-all group"
                >
                  <div className="flex justify-between items-start mb-1">
                    <h4 className="text-xs font-bold text-text-heading group-hover:text-brand-primary transition-colors line-clamp-2">
                      {demand.title}
                    </h4>
                    <Link 
                      to={`/demandas/${demand.id}`}
                      className="text-text-muted hover:text-brand-primary"
                      onClick={(e) => e.stopPropagation()}
                    >
                      <ExternalLink size={14} />
                    </Link>
                  </div>
                  <div className="flex items-center gap-1 text-[10px] text-text-muted">
                    <MapPin size={10} />
                    <span className="truncate">{demand.schoolName}</span>
                  </div>
                  <div className="mt-2 flex items-center justify-between">
                    <span className="text-[10px] px-1.5 py-0.5 bg-slate-100 text-slate-600 rounded">
                      {demand.subject}
                    </span>
                    <ChevronRight size={14} className="text-slate-300 group-hover:text-brand-primary transition-colors" />
                  </div>
                </div>
              ))
            ) : (
              <div className="p-8 text-center">
                <p className="text-sm text-text-muted italic">Nenhuma demanda encontrada.</p>
              </div>
            )}
          </div>
        </div>
      )}

      <GoogleMap
        mapContainerStyle={containerStyle}
        center={center}
        zoom={12}
        onLoad={onLoad}
        onUnmount={onUnmount}
        options={{
          mapTypeControl: false,
          streetViewControl: false,
          fullscreenControl: false,
          zoomControl: true,
          clickableIcons: false,
          styles: [
            {
              featureType: 'poi',
              stylers: [{ visibility: 'off' }],
            },
          ],
        }}
      >
        {schools.map(school => (
          <Marker 
            key={school.id}
            position={{ lat: school.latitude, lng: school.longitude }}
            title={school.name}
            onClick={() => handleSelectSchool(school)}
          />
        ))}

        {selectedSchool && (
          <InfoWindow
            position={{ lat: selectedSchool.latitude, lng: selectedSchool.longitude }}
            onCloseClick={() => setSelectedSchool(null)}
          >
            <div className="p-2 min-w-[250px] max-w-[300px]">
              <div className="flex items-center gap-2 mb-3 border-b pb-2">
                <MapPin size={18} className="text-brand-primary" />
                <h4 className="font-bold text-text-heading text-sm">{selectedSchool.name}</h4>
              </div>

              <div className="space-y-3">
                <h5 className="text-xs font-semibold text-text-muted uppercase tracking-wider">
                  Demandas Disponíveis ({selectedSchool.demands?.length || 0})
                </h5>
                
                {selectedSchool.demands && selectedSchool.demands.length > 0 ? (
                  <div className="space-y-2 max-h-[200px] overflow-y-auto pr-1">
                    {selectedSchool.demands.map(demand => (
                      <div key={demand.id} className="p-2 rounded-lg bg-slate-50 border border-slate-100">
                        <div className="flex items-start justify-between gap-2">
                          <div>
                            <p className="text-xs font-bold text-text-heading leading-tight mb-1">
                              {demand.title}
                            </p>
                            <p className="text-[10px] text-text-muted">
                              {demand.subject}
                            </p>
                          </div>
                          <Link 
                            to={`/demandas/${demand.id}`}
                            className="text-brand-primary hover:text-brand-primary/80"
                          >
                            <ExternalLink size={14} />
                          </Link>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-xs text-text-muted italic">Nenhuma demanda ativa no momento.</p>
                )}
              </div>
            </div>
          </InfoWindow>
        )}

        {schools.length === 0 && (
          <Marker 
            position={center} 
            title="São Paulo"
          />
        )}
      </GoogleMap>
    </div>
  );
}
