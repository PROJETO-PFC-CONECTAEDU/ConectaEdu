import React from 'react';
import { Card } from './Card';

interface MetricCardProps {
  icon: React.ReactNode;
  label: string;
  value: string | number;
  note: string;
}

export function MetricCard({ icon, label, value, note }: MetricCardProps) {
  return (
    <Card className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <div className="p-2 rounded-lg bg-slate-50">
          {icon}
        </div>
      </div>
      <div>
        <p className="text-sm text-text-body font-medium">{label}</p>
        <h4 className="text-2xl font-bold text-text-heading mt-1">{value}</h4>
        <p className="text-xs text-text-muted mt-1">{note}</p>
      </div>
    </Card>
  );
}
