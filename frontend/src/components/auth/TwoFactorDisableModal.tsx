import { useState } from 'react';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { Mail, ShieldX } from 'lucide-react';
import api from '../../lib/api';
import toast from 'react-hot-toast';

interface TwoFactorDisableModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export function TwoFactorDisableModal({ isOpen, onClose, onSuccess }: TwoFactorDisableModalProps) {
  const [step, setStep] = useState(1);
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);

  const handleRequestCode = async () => {
    setLoading(true);
    try {
      await api.post('/auth/2fa/disable/request');
      toast.success('Código de desativação enviado para seu e-mail.');
      setStep(2);
    } catch (error) {
      toast.error('Erro ao solicitar código de desativação.');
    } finally {
      setLoading(false);
    }
  };

  const handleConfirmDisable = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/auth/2fa/disable/confirm', { code });
      toast.success('2FA desativado com sucesso.');
      onSuccess();
      onClose();
    } catch (error) {
      toast.error('Código inválido ou expirado.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Desativar Autenticação de Duas Etapas">
      {step === 1 ? (
        <div className="space-y-6">
          <div className="flex flex-col items-center text-center space-y-4">
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center text-red-600">
              <ShieldX size={32} />
            </div>
            <div className="space-y-2">
              <h4 className="font-bold text-text-heading text-lg">Tem certeza?</h4>
              <p className="text-text-body text-sm">
                Desativar o 2FA torna sua conta menos segura. Para continuar, enviaremos um código de confirmação para o seu e-mail.
              </p>
            </div>
          </div>

          <div className="flex gap-3">
            <Button variant="outline" type="button" className="flex-1" onClick={onClose}>
              Cancelar
            </Button>
            <Button className="flex-1 bg-red-600 hover:bg-red-700" onClick={handleRequestCode} disabled={loading}>
              {loading ? 'Enviando...' : 'Enviar Código'}
            </Button>
          </div>
        </div>
      ) : (
        <form onSubmit={handleConfirmDisable} className="space-y-6">
          <div className="flex flex-col items-center text-center space-y-4">
            <div className="w-16 h-16 bg-brand-primary/10 rounded-full flex items-center justify-center text-brand-primary">
              <Mail size={32} />
            </div>
            <div className="space-y-2">
              <h4 className="font-bold text-text-heading text-lg">Confirme o Código</h4>
              <p className="text-text-body text-sm">
                Insira o código de 6 dígitos enviado para o seu e-mail.
              </p>
            </div>
          </div>

          <Input
            label="Código do E-mail"
            placeholder="000000"
            value={code}
            onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
            required
            className="text-center text-2xl tracking-[0.5em] font-mono"
            autoFocus
          />

          <div className="flex gap-3">
            <Button variant="outline" type="button" className="flex-1" onClick={() => setStep(1)}>
              Voltar
            </Button>
            <Button type="submit" className="flex-1 bg-red-600 hover:bg-red-700" disabled={loading || code.length < 6}>
              {loading ? 'Desativando...' : 'Confirmar Desativação'}
            </Button>
          </div>
        </form>
      )}
    </Modal>
  );
}
