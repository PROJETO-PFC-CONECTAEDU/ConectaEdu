import { useEffect } from 'react';
import { createPortal } from 'react-dom';
import { X } from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  className?: string;
  closeButtonLabel?: string;
  showCloseButton?: boolean;
}

export function Modal({ 
  isOpen, 
  onClose, 
  title, 
  children, 
  className,
  closeButtonLabel,
  showCloseButton = true
}: ModalProps) {
  useEffect(() => {
    const handleEsc = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && showCloseButton) onClose();
    };
    if (isOpen) {
      document.body.style.overflow = 'hidden';
      window.addEventListener('keydown', handleEsc);
    }
    return () => {
      document.body.style.overflow = 'unset';
      window.removeEventListener('keydown', handleEsc);
    };
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return createPortal(
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
      <div 
        className={cn(
          "bg-white rounded-2xl shadow-xl w-full max-w-md max-h-full flex flex-col overflow-hidden animate-in zoom-in-95 duration-200",
          className
        )}
      >
        <div className="flex-shrink-0 flex items-center justify-between p-6 border-b border-border-default">
          <h3 className="text-xl font-bold text-text-heading">{title}</h3>
          {showCloseButton && (
            <button 
              onClick={onClose}
              className={cn(
                "p-2 text-text-muted hover:text-text-heading hover:bg-slate-100 rounded-lg transition-colors",
                closeButtonLabel && "text-sm font-medium px-3 text-text-body"
              )}
            >
              {closeButtonLabel ? closeButtonLabel : <X size={20} />}
            </button>
          )}
        </div>
        <div className="p-6 overflow-y-auto flex-1">
          {children}
        </div>
      </div>
    </div>,
    document.body
  );
}
