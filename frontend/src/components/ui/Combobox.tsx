import React, { useState, useRef, useEffect, useMemo } from 'react';
import { X, ChevronDown, Check } from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

interface ComboboxProps {
  options: string[];
  value: string | string[];
  onChange: (value: any) => void;
  multiple?: boolean;
  label?: string;
  error?: string;
  placeholder?: string;
  className?: string;
}

export function Combobox({
  options,
  value,
  onChange,
  multiple = false,
  label,
  error,
  placeholder = 'Selecione...',
  className
}: ComboboxProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const selectedValues = useMemo(() => {
    if (multiple) {
      return Array.isArray(value) ? value : (value ? (value as string).split(',').map(s => s.trim()).filter(Boolean) : []);
    }
    return value ? [value as string] : [];
  }, [value, multiple]);

  const filteredOptions = useMemo(() => {
    return options.filter(option => 
      option.toLowerCase().includes(searchTerm.toLowerCase()) &&
      (!multiple || !selectedValues.includes(option))
    );
  }, [options, searchTerm, selectedValues, multiple]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
        setSearchTerm('');
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSelect = (option: string) => {
    if (multiple) {
      const newValue = [...selectedValues, option];
      onChange(newValue);
      setSearchTerm('');
      inputRef.current?.focus();
    } else {
      onChange(option);
      setIsOpen(false);
      setSearchTerm('');
    }
  };

  const handleRemove = (option: string) => {
    if (multiple) {
      const newValue = selectedValues.filter(v => v !== option);
      onChange(newValue);
    } else {
      onChange('');
    }
  };

  const handleInputKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Backspace' && searchTerm === '' && selectedValues.length > 0 && multiple) {
      handleRemove(selectedValues[selectedValues.length - 1]);
    } else if (e.key === 'Enter' && isOpen && filteredOptions.length > 0) {
      e.preventDefault();
      handleSelect(filteredOptions[0]);
    } else if (e.key === 'Escape') {
      setIsOpen(false);
    }
  };

  return (
    <div className={cn("w-full space-y-1.5 relative", className)} ref={containerRef}>
      {label && (
        <label className="text-sm font-medium text-text-heading">
          {label}
        </label>
      )}
      
      <div 
        className={cn(
          "min-h-[44px] w-full rounded-lg border border-border-default bg-white px-3 py-1.5 text-sm flex flex-wrap gap-2 items-center cursor-text transition-all focus-within:ring-2 focus-within:ring-brand-primary/20",
          error && "border-red-500 focus-within:ring-red-500/20",
          isOpen && "border-brand-primary"
        )}
        onClick={() => {
          setIsOpen(true);
          inputRef.current?.focus();
        }}
      >
        {multiple && selectedValues.map(val => (
          <span key={val} className="bg-brand-primary/10 text-brand-primary px-2 py-1 rounded-md flex items-center gap-1 text-xs font-bold uppercase transition-colors hover:bg-brand-primary/20">
            {val}
            <button 
              type="button" 
              onClick={(e) => {
                e.stopPropagation();
                handleRemove(val);
              }}
              className="hover:text-brand-primary/80 transition-colors"
            >
              <X size={14} />
            </button>
          </span>
        ))}

        {!multiple && selectedValues.length > 0 && !isOpen && (
           <span className="text-text-heading truncate flex-1">{selectedValues[0]}</span>
        )}

        <input
          ref={inputRef}
          className="flex-1 min-w-[50px] bg-transparent outline-none placeholder:text-text-muted text-text-heading text-sm"
          placeholder={selectedValues.length === 0 ? placeholder : ''}
          value={searchTerm}
          onChange={(e) => {
            setSearchTerm(e.target.value);
            setIsOpen(true);
          }}
          onKeyDown={handleInputKeyDown}
          onFocus={() => setIsOpen(true)}
        />
        
        <div className="flex items-center gap-1 shrink-0">
          {((!multiple && selectedValues.length > 0) || (multiple && selectedValues.length > 0)) && (
            <button 
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                if (multiple) onChange([]);
                else onChange('');
                setSearchTerm('');
              }}
              className="text-text-muted hover:text-red-500 transition-colors p-0.5"
            >
              <X size={16} />
            </button>
          )}
          <div className="w-px h-4 bg-border-default mx-1" />
          <ChevronDown className={cn("text-text-muted transition-transform duration-200", isOpen && "rotate-180")} size={18} />
        </div>
      </div>

      {isOpen && (
        <div className="absolute z-[100] w-full mt-1 bg-white border border-border-default rounded-lg shadow-xl max-h-60 overflow-y-auto animate-in fade-in slide-in-from-top-1 duration-200">
          {filteredOptions.length > 0 ? (
            filteredOptions.map(option => (
              <div
                key={option}
                className={cn(
                  "px-4 py-2.5 hover:bg-brand-primary/5 cursor-pointer text-sm text-text-body flex items-center justify-between transition-colors",
                  !multiple && selectedValues.includes(option) && "bg-brand-primary/5 text-brand-primary font-medium"
                )}
                onClick={(e) => {
                  e.stopPropagation();
                  handleSelect(option);
                }}
              >
                <span>{option}</span>
                {!multiple && selectedValues.includes(option) && <Check size={16} className="text-brand-primary" />}
              </div>
            ))
          ) : (
            <div className="px-4 py-3 text-sm text-text-muted italic text-center">
              Nenhuma opção disponível
            </div>
          )}
        </div>
      )}

      {error && (
        <p className="text-xs text-red-500 animate-in fade-in slide-in-from-top-1">{error}</p>
      )}
    </div>
  );
}
