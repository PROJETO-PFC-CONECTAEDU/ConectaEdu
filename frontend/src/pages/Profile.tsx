import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../lib/api';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { Modal } from '../components/ui/Modal';
import { Header } from '../components/layout/Header';
import toast from 'react-hot-toast';
import { User, Shield, CheckCircle, XCircle } from 'lucide-react';
import { formatRole } from '../utils/format';

interface ConsentRecord {
  id: string;
  legalDocumentId: string;
  documentType: string;
  documentVersion: string;
  action: string;
  acceptedAt: string;
  withdrawnAt: string | null;
}

export function Profile() {
  const { user } = useAuth();
  const [consents, setConsents] = useState<ConsentRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedConsent, setSelectedConsent] = useState<ConsentRecord | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [viewingDoc, setViewingDoc] = useState<{ title: string; content: string } | null>(null);

  useEffect(() => {
    fetchConsents();
  }, [user]);

  const fetchConsents = async () => {
    if (!user) return;
    try {
      const response = await api.get<ConsentRecord[]>(`/consents/user/${user.id}`);
      setConsents(response.data);
    } catch (error) {
      toast.error('Erro ao buscar histórico de consentimentos.');
    } finally {
      setLoading(false);
    }
  };

  const handleRevokeClick = (consent: ConsentRecord) => {
    setSelectedConsent(consent);
    setIsModalOpen(true);
  };

  const handleConfirmRevoke = async () => {
    if (!selectedConsent || !user) return;

    try {
      await api.patch('/consents/withdraw', {
        userId: user.id,
        legalDocumentId: selectedConsent.legalDocumentId
      });
      toast.success('Consentimento revogado com sucesso!');
      fetchConsents();
      setIsModalOpen(false);
      
      // Força a recarga para que o ConsentGuard verifique o novo status e bloqueie o acesso se necessário
      setTimeout(() => {
        window.location.reload();
      }, 1500);
    } catch (error) {
      toast.error('Erro ao revogar consentimento.');
    }
  };

  const handleViewDocument = async (consent: ConsentRecord) => {
    try {
      const response = await api.get(`/legal-documents/${consent.legalDocumentId}`);
      setViewingDoc({
        title: `${consent.documentType === 'TERMS_OF_USE' ? 'Termos de Uso' : 'Política de Privacidade'} v${consent.documentVersion}`,
        content: response.data.content
      });
    } catch (error) {
      toast.error('Erro ao buscar conteúdo do documento.');
    }
  };

  return (
    <div className="flex-1">
      <Header 
        title="Meu Perfil" 
        subtitle="Gerencie suas informações e preferências de privacidade." 
      />
      
      <div className="p-8 space-y-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card className="p-6">
          <div className="flex flex-col items-center text-center">
            <div className="w-24 h-24 bg-brand-primary/10 rounded-full flex items-center justify-center text-brand-primary mb-4">
              <User size={48} />
            </div>
            <h2 className="text-xl font-bold text-text-heading">{user?.name}</h2>
            <p className="text-text-body">{user?.email}</p>
            <span className="mt-2 px-3 py-1 bg-brand-primary/10 text-brand-primary text-xs font-bold rounded-full uppercase">
              {formatRole(user?.role)}
            </span>
          </div>
        </Card>

        <div className="md:col-span-2 space-y-6">
          <Card className="p-6">
            <div className="flex items-center gap-2 mb-6">
              <Shield className="text-brand-primary" size={24} />
              <h3 className="text-lg font-bold text-text-heading">Consentimentos e Privacidade</h3>
            </div>

            {loading ? (
              <p>Carregando histórico...</p>
            ) : consents.length === 0 ? (
              <p className="text-text-body italic">Nenhum registro de consentimento encontrado.</p>
            ) : (
              <div className="space-y-4">
                {consents.map((consent) => (
                  <div key={consent.id} className="flex items-center justify-between p-4 border rounded-lg">
                    <div className="space-y-1">
                      <button 
                        onClick={() => handleViewDocument(consent)}
                        className="font-bold text-text-heading hover:text-brand-primary transition-colors text-left"
                      >
                        {consent.documentType === 'TERMS_OF_USE' ? 'Termos de Uso' : 'Política de Privacidade'}
                        <span className="ml-2 text-sm font-normal text-text-muted">v{consent.documentVersion}</span>
                      </button>
                      <p className="text-xs text-text-body">
                        Aceito em: {new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(consent.acceptedAt))}
                      </p>
                      {consent.withdrawnAt && (
                        <p className="text-xs text-brand-accent font-medium">
                          Revogado em: {new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(consent.withdrawnAt))}
                        </p>
                      )}
                    </div>
                    
                    <div className="flex items-center gap-4">
                      {consent.withdrawnAt ? (
                        <div className="flex items-center gap-1 text-brand-accent">
                          <XCircle size={18} />
                          <span className="text-sm font-medium">Revogado</span>
                        </div>
                      ) : (
                        <>
                          <div className="flex items-center gap-1 text-green-600">
                            <CheckCircle size={18} />
                            <span className="text-sm font-medium">Ativo</span>
                          </div>
                          <Button 
                            variant="outline" 
                            className="text-brand-accent border-brand-accent hover:bg-brand-accent/5"
                            onClick={() => handleRevokeClick(consent)}
                          >
                            Revogar
                          </Button>
                        </>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </Card>
        </div>
      </div>

      <ConfirmModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onConfirm={handleConfirmRevoke}
        title="Revogar Consentimento"
        description="Ao revogar seu consentimento, você poderá perder o acesso a certas funcionalidades da plataforma até que aceite os termos novamente. Deseja continuar?"
        confirmText="Revogar"
        cancelText="Cancelar"
      />

      <Modal
        isOpen={!!viewingDoc}
        onClose={() => setViewingDoc(null)}
        title={viewingDoc?.title || ''}
        className="max-w-2xl"
      >
        <div className="max-h-[60vh] overflow-y-auto pr-2">
          <div className="text-sm text-text-body whitespace-pre-wrap leading-relaxed">
            {viewingDoc?.content}
          </div>
        </div>
        <div className="mt-6 flex justify-end">
          <Button onClick={() => setViewingDoc(null)}>
            Fechar
          </Button>
        </div>
      </Modal>
    </div>
  </div>
  );
}
