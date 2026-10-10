import { useRef, useState } from 'react';
import { Autocomplete } from '@react-google-maps/api';
import { Input } from '../ui/Input';

interface AddressAutocompleteProps {
  onAddressSelect: (address: string, lat?: number, lng?: number) => void;
  defaultValue?: string;
  error?: string;
  label?: string;
  placeholder?: string;
}

export function AddressAutocomplete({ 
  onAddressSelect, 
  defaultValue = '', 
  error, 
  label = 'Endereço',
  placeholder = 'Busque o endereço da escola...'
}: AddressAutocompleteProps) {
  const autocompleteRef = useRef<google.maps.places.Autocomplete | null>(null);
  const [inputValue, setInputValue] = useState(defaultValue);

  const onLoad = (autocomplete: google.maps.places.Autocomplete) => {
    autocompleteRef.current = autocomplete;
  };

  const onPlaceChanged = () => {
    if (autocompleteRef.current !== null) {
      const place = autocompleteRef.current.getPlace();
      const address = place.formatted_address || place.name || '';
      const lat = place.geometry?.location?.lat();
      const lng = place.geometry?.location?.lng();

      setInputValue(address);
      onAddressSelect(address, lat, lng);
    }
  };

  return (
    <Autocomplete
      onLoad={onLoad}
      onPlaceChanged={onPlaceChanged}
      fields={['formatted_address', 'geometry', 'name']}
    >
      <Input
        label={label}
        placeholder={placeholder}
        value={inputValue}
        onChange={(e) => {
          setInputValue(e.target.value);
          onAddressSelect(e.target.value);
        }}
        error={error}
      />
    </Autocomplete>
  );
}
