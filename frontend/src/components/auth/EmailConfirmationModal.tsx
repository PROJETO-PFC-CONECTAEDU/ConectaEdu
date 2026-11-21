import { useState } from 'react';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { Mail, RefreshCw } from 'lucide-react';
import api from '../../lib/api';
import toast from 'react-hot-toast';

interface EmailConfirmationModalProps {
  isOpen: boolean;
  onClose: () => void;
  email: string;
  onSuccess: () => void;
}

export function EmailConfirmationModal({ isOpen, onClose, email, onSuccess }: EmailConfirmationModalProps) {
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);

  const handleConfirm = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/auth/confirm-email', {
        email,
        code
      });
      toast.success('E-mail confirmado com sucesso!');
      onSuccess();
      onClose();
    } catch (error) {
      // Erro tratado globalmente
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setResending(true);
    try {
      await api.post('/auth/resend-confirmation', { email });
      toast.success('Código de confirmação reenviado!');
    } catch (error) {
      // Erro tratado globalmente
    } finally {
      setResending(false);
    }
  };


  return (
    <Modal 
      isOpen={isOpen} 
      onClose={onClose} 
      title="Confirmação de E-mail"
    >
      <div className="space-y-6">
        <div className="flex flex-col items-center text-center space-y-4">
          <div className="w-16 h-16 bg-brand-primary/10 rounded-full flex items-center justify-center text-brand-primary">
            <Mail size={32} />
          </div>
          <div className="space-y-2 w-full">
            <p className="text-text-body">
              Enviamos um código de confirmação para:
            </p>
            <div className="flex items-center justify-center gap-2">
              <span className="font-semibold text-brand-primary">{email}</span>
            </div>
          </div>
        </div>

        <form onSubmit={handleConfirm} className="space-y-6">
          <Input
            label="Código de Confirmação"
            placeholder="000000"
            value={code}
            onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
            required
            className="text-center text-2xl tracking-[0.5em] font-mono"
          />

          <div className="flex flex-col gap-3">
            <div className="flex gap-3">
              <Button variant="outline" type="button" className="flex-1" onClick={onClose}>
                Sair
              </Button>
              <Button type="submit" className="flex-1" disabled={loading || code.length < 6}>
                {loading ? 'Confirmando...' : 'Confirmar'}
              </Button>
            </div>
            
            <button
              type="button"
              onClick={handleResend}
              disabled={resending}
              className="flex items-center justify-center gap-2 text-sm text-text-muted hover:text-brand-primary transition-colors disabled:opacity-50"
            >
              <RefreshCw size={14} className={resending ? 'animate-spin' : ''} />
              Reenviar código de confirmação
            </button>
          </div>
        </form>
      </div>
    </Modal>
  );
}
