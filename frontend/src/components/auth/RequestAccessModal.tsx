import { Building2, School } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Modal } from '../ui/Modal';

interface RequestAccessModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function RequestAccessModal({ isOpen, onClose }: RequestAccessModalProps) {
  const navigate = useNavigate();

  const roles = [
    {
      id: 'UNIVERSITY_ADMIN',
      title: 'Administrador da Universidade',
      description: 'Gere os convênios, valide as horas de estágio dos alunos e acompanhe o desempenho pedagógico de toda a instituição.',
      icon: Building2,
      color: 'bg-blue-50 text-blue-600',
    },
    {
      id: 'SCHOOL_DIRECTOR',
      title: 'Diretor da Escola',
      description: 'Publique demandas de estágio, selecione os melhores candidatos para sua escola e acompanhe a evolução das atividades práticas.',
      icon: School,
      color: 'bg-emerald-50 text-emerald-600',
    },
  ];

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Como você deseja acessar?"
      className="max-w-lg"
    >
      <div className="space-y-4">
        <p className="text-text-body text-sm">
          Escolha o perfil que melhor descreve sua atuação profissional para que possamos direcionar sua solicitação corretamente.
        </p>
        
        <div className="grid gap-4">
          {roles.map((role) => {
            const Icon = role.icon;
            return (
              <button
                key={role.id}
                onClick={() => {
                  navigate(`/register?role=${role.id}`);
                  onClose();
                }}
                className="flex items-start gap-4 p-4 rounded-xl border border-border-default hover:border-brand-primary hover:bg-slate-50 transition-all text-left group"
              >
                <div className={`p-3 rounded-lg ${role.color} shrink-0`}>
                  <Icon size={24} />
                </div>
                <div className="space-y-1">
                  <h4 className="font-bold text-text-heading group-hover:text-brand-primary transition-colors">
                    {role.title}
                  </h4>
                  <p className="text-sm text-text-muted leading-relaxed">
                    {role.description}
                  </p>
                </div>
              </button>
            );
          })}
        </div>

        <div className="pt-2">
          <p className="text-xs text-text-muted text-center italic">
            * Sua solicitação passará por um processo de validação antes da liberação do acesso.
          </p>
        </div>
      </div>
    </Modal>
  );
}
