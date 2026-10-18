import { useEffect, useMemo, useState } from 'react';
import { GoogleMap, Marker, useJsApiLoader } from '@react-google-maps/api';

interface SchoolMapProps {
  address: string;
  schoolName: string;
  latitude?: number;
  longitude?: number;
}

const containerStyle = {
  width: '100%',
  height: '450px',
  borderRadius: '12px'
};

export function SchoolMap({ address, schoolName, latitude, longitude }: SchoolMapProps) {
  const { isLoaded } = useJsApiLoader({
    id: 'google-map-script',
    googleMapsApiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || '',
    libraries: ['places']
  });

  const [geocodedCenter, setGeocodedCenter] = useState<google.maps.LatLngLiteral | null>(null);

  const hasCoords = latitude !== undefined && longitude !== undefined && (latitude !== 0 || longitude !== 0);

  useEffect(() => {
    if (isLoaded && !hasCoords && address) {
      const geocoder = new google.maps.Geocoder();
      geocoder.geocode({ address }, (results, status) => {
        if (status === 'OK' && results?.[0]?.geometry?.location) {
          setGeocodedCenter({
            lat: results[0].geometry.location.lat(),
            lng: results[0].geometry.location.lng()
          });
        }
      });
    }
  }, [isLoaded, hasCoords, address]);

  const center = useMemo(() => {
    if (hasCoords) {
      return { lat: latitude!, lng: longitude! };
    }
    if (geocodedCenter) {
      return geocodedCenter;
    }
    return { lat: -23.55052, lng: -46.633308 };
  }, [latitude, longitude, hasCoords, geocodedCenter]);

  if (!isLoaded) {
    return (
      <div className="w-full h-[450px] bg-slate-100 animate-pulse rounded-xl flex items-center justify-center border border-slate-200">
        <p className="text-slate-400 text-sm">Carregando mapa...</p>
      </div>
    );
  }

  if (!address && !hasCoords) {
    return (
      <div className="w-full h-[450px] bg-slate-100 rounded-xl flex items-center justify-center border border-dashed border-slate-300">
        <p className="text-slate-500 text-sm">Endereço não disponível para exibir o mapa.</p>
      </div>
    );
  }

  const finalMarkerPos = hasCoords ? { lat: latitude!, lng: longitude! } : geocodedCenter;

  return (
    <div className="w-full h-[450px] rounded-xl overflow-hidden border border-border-default shadow-sm relative">
      <GoogleMap
        mapContainerStyle={containerStyle}
        center={center}
        zoom={15}
        options={{
          mapTypeControl: false,
          streetViewControl: false,
          fullscreenControl: false,
          clickableIcons: false,
          styles: [
            {
              featureType: 'poi',
              stylers: [{ visibility: 'off' }],
            },
          ],
        }}
      >
        {finalMarkerPos && <Marker position={finalMarkerPos} title={schoolName} />}
      </GoogleMap>
      {!hasCoords && geocodedCenter && (
        <div className="absolute top-2 right-2 flex items-center justify-center pointer-events-none">
          <div className="bg-white/90 px-3 py-1.5 rounded-full shadow-sm border border-slate-200 text-[10px] text-slate-600 font-medium flex items-center gap-1.5">
            <span className="w-2 h-2 bg-amber-500 rounded-full animate-pulse" />
            Localização via endereço
          </div>
        </div>
      )}
    </div>
  );
}
