import { useState } from 'react';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { Shield } from 'lucide-react';
import api from '../../lib/api';
import toast from 'react-hot-toast';

interface TwoFactorVerifyModalProps {
  isOpen: boolean;
  onClose: () => void;
  email: string;
  onSuccess: (data: any) => void;
}

export function TwoFactorVerifyModal({ isOpen, onClose, email, onSuccess }: TwoFactorVerifyModalProps) {
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);

  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await api.post('/auth/2fa/verify', {
        email,
        code: parseInt(code)
      });
      onSuccess(response.data);
      toast.success('Autenticação realizada com sucesso!');
      onClose();
    } catch (error) {
      toast.error('Código inválido ou expirado.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Verificação de Duas Etapas">
      <form onSubmit={handleVerify} className="space-y-6">
        <div className="flex flex-col items-center text-center space-y-4">
          <div className="w-16 h-16 bg-brand-primary/10 rounded-full flex items-center justify-center text-brand-primary">
            <Shield size={32} />
          </div>
          <div>
            <p className="text-text-body">
              O 2FA está ativado para sua conta. Por favor, insira o código gerado pelo seu aplicativo autenticador.
            </p>
          </div>
        </div>

        <Input
          label="Código de Autenticação"
          placeholder="000000"
          value={code}
          onChange={(e) => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
          required
          className="text-center text-2xl tracking-[0.5em] font-mono"
        />

        <div className="flex gap-3">
          <Button variant="outline" type="button" className="flex-1" onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" className="flex-1" disabled={loading || code.length < 6}>
            {loading ? 'Verificando...' : 'Verificar'}
          </Button>
        </div>
      </form>
    </Modal>
  );
}
