import { useState, useEffect } from 'react';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import { Shield, Smartphone, ArrowRight } from 'lucide-react';
import api from '../../lib/api';
import toast from 'react-hot-toast';

interface TwoFactorSetupModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export function TwoFactorSetupModal({ isOpen, onClose, onSuccess }: TwoFactorSetupModalProps) {
  const [step, setStep] = useState(1);
  const [qrCodeUrl, setQrCodeUrl] = useState('');
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && step === 1) {
      handleSetup();
    }
  }, [isOpen]);

  const handleSetup = async () => {
    try {
      const response = await api.post('/auth/2fa/setup');
      setQrCodeUrl(response.data.qrCodeUrl);
    } catch (error) {
      toast.error('Erro ao iniciar configuração do 2FA.');
      onClose();
    }
  };

  const handleConfirm = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post('/auth/2fa/confirm', { code: parseInt(code) });
      toast.success('2FA ativado com sucesso!');
      onSuccess();
      onClose();
    } catch (error) {
      toast.error('Código inválido. Tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Configurar Autenticação de Duas Etapas">
      {step === 1 ? (
        <div className="space-y-6">
          <div className="flex flex-col items-center text-center space-y-4">
            <div className="w-16 h-16 bg-brand-primary/10 rounded-full flex items-center justify-center text-brand-primary">
              <Smartphone size={32} />
            </div>
            <div className="space-y-2">
              <h4 className="font-bold text-text-heading text-lg">Escaneie o QR Code</h4>
              <p className="text-text-body text-sm">
                Abra seu aplicativo autenticador (Google Authenticator, Authy, etc.) e escaneie o código abaixo.
              </p>
            </div>
          </div>

          <div className="flex justify-center p-4 bg-white border rounded-xl">
            {qrCodeUrl ? (
              <img 
                src={`https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(qrCodeUrl)}`} 
                alt="QR Code 2FA"
                className="w-[200px] h-[200px]"
              />
            ) : (
              <div className="w-[200px] h-[200px] bg-slate-100 animate-pulse rounded-lg" />
            )}
          </div>

          <div className="bg-blue-50 p-4 rounded-lg flex gap-3">
            <Shield className="text-blue-600 shrink-0" size={20} />
            <p className="text-xs text-blue-800 leading-relaxed">
              O 2FA adiciona uma camada extra de segurança. Você precisará do código do app sempre que fizer login.
            </p>
          </div>

          <Button className="w-full gap-2" onClick={() => setStep(2)}>
            Próximo Passo <ArrowRight size={18} />
          </Button>
        </div>
      ) : (
        <form onSubmit={handleConfirm} className="space-y-6">
          <div className="flex flex-col items-center text-center space-y-4">
            <div className="w-16 h-16 bg-brand-primary/10 rounded-full flex items-center justify-center text-brand-primary">
              <Shield size={32} />
            </div>
            <div className="space-y-2">
              <h4 className="font-bold text-text-heading text-lg">Confirme o Código</h4>
              <p className="text-text-body text-sm">
                Digite o código de 6 dígitos que aparece no seu aplicativo para confirmar a ativação.
              </p>
            </div>
          </div>

          <Input
            label="Código de Confirmação"
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
            <Button type="submit" className="flex-1" disabled={loading || code.length < 6}>
              {loading ? 'Confirmando...' : 'Ativar 2FA'}
            </Button>
          </div>
        </form>
      )}
    </Modal>
  );
}
