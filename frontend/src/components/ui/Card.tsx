import type { ReactNode } from 'react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

interface CardProps {
  children: ReactNode;
  className?: string;
}

export function Card({ children, className }: CardProps) {
  return (
    <div className={cn(
      'rounded-xl border border-border-default bg-white p-6 shadow-[0px_2px_4px_rgba(0,0,0,0.02),0px_8px_12px_rgba(0,0,0,0.04)]',
      className
    )}>
      {children}
    </div>
  );
}
