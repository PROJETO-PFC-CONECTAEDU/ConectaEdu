import React, { useState, useRef, useLayoutEffect } from 'react';
import { Info } from 'lucide-react';
import { twMerge } from 'tailwind-merge';

interface TooltipProps {
  content: string;
  children?: React.ReactNode;
  className?: string;
}

export function Tooltip({ content, children, className }: TooltipProps) {
  const [isVisible, setIsVisible] = useState(false);
  const tooltipRef = useRef<HTMLDivElement>(null);
  const [position, setPosition] = useState({ left: '50%', transform: 'translateX(-50%)' });
  const [arrowPosition, setArrowPosition] = useState({ left: '50%' });

  useLayoutEffect(() => {
    if (!isVisible) {
      setPosition({ left: '50%', transform: 'translateX(-50%)' });
      setArrowPosition({ left: '50%' });
      return;
    }

    if (tooltipRef.current) {
      // Forçamos o reset imediato do estilo para capturar o rect original sem ajustes anteriores
      tooltipRef.current.style.left = '50%';
      tooltipRef.current.style.transform = 'translateX(-50%)';

      const rect = tooltipRef.current.getBoundingClientRect();
      const padding = 10;
      let leftAdjustment = 0;

      if (rect.left < padding) {
        leftAdjustment = padding - rect.left;
      } else if (rect.right > window.innerWidth - padding) {
        leftAdjustment = window.innerWidth - padding - rect.right;
      }

      if (leftAdjustment !== 0) {
        setPosition({
          left: '50%',
          transform: `translateX(calc(-50% + ${leftAdjustment}px))`
        });
        setArrowPosition({
          left: `calc(50% - ${leftAdjustment}px)`
        });
      }
    }
  }, [isVisible]);

  return (
    <div 
      className="relative flex items-center inline-block"
      onMouseEnter={() => setIsVisible(true)}
      onMouseLeave={() => setIsVisible(false)}
    >
      {children || <Info size={14} className={twMerge("text-text-muted cursor-help ml-1", className)} />}
      
      {isVisible && (
        <div 
          ref={tooltipRef}
          style={position}
          className="absolute bottom-full mb-2 w-48 p-2 bg-slate-800 text-white text-xs rounded shadow-lg z-50 pointer-events-none"
        >
          {content}
          <div 
            style={arrowPosition}
            className="absolute top-full -translate-x-1/2 -mt-1 border-4 border-transparent border-t-slate-800" 
          />
        </div>
      )}
    </div>
  );
}
