import { useCallback } from 'react';
import { GoogleMap, useJsApiLoader, Marker } from '@react-google-maps/api';

const containerStyle = {
  width: '100%',
  height: '600px',
  borderRadius: '12px'
};

const center = {
  lat: -23.55052,
  lng: -46.633308
};

export function MapComponent() {
  const { isLoaded } = useJsApiLoader({
    id: 'google-map-script',
    googleMapsApiKey: "AIzaSyAE76iJXLxmRFfyamEfFUPksZfyOIQ_FpE"
  });

  const onLoad = useCallback(function callback(map: google.maps.Map) {
    const bounds = new window.google.maps.LatLngBounds(center);
    map.fitBounds(bounds);
  }, []);

  const onUnmount = useCallback(function callback() {
    // Limpeza se necessário
  }, []);

  return isLoaded ? (
    <div className="w-full h-full min-h-[600px]">
      <GoogleMap
        mapContainerStyle={containerStyle}
        center={center}
        zoom={12}
        onLoad={onLoad}
        onUnmount={onUnmount}
        options={{
          mapTypeControl: true,
          streetViewControl: true,
          fullscreenControl: true,
        }}
      >
        <Marker 
          position={center} 
          title="São Paulo"
        />
      </GoogleMap>
    </div>
  ) : (
    <div className="flex items-center justify-center h-[600px] bg-slate-100 rounded-xl border-2 border-dashed border-slate-300">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-brand-primary mx-auto mb-4"></div>
        <p className="text-slate-500 font-medium">Carregando mapa do Google...</p>
      </div>
    </div>
  );
}
