import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import api from '../../lib/api';
import { Modal } from '../ui/Modal';
import { Button } from '../ui/Button';
import toast from 'react-hot-toast';

interface LegalDocument {
  id: string;
  title: string;
  content: string;
  version: string;
}

interface ConsentStatus {
  needsConsent: boolean;
  pendingDocuments: LegalDocument[];
}

export function ConsentGuard({ children }: { children: React.ReactNode }) {
  const { user, isAuthenticated } = useAuth();
  const [needsConsent, setNeedsConsent] = useState(false);
  const [pendingDocs, setPendingDocs] = useState<LegalDocument[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isAuthenticated && user) {
      checkConsent();
    }
  }, [isAuthenticated, user]);

  const checkConsent = async () => {
    setLoading(true);
    try {
      const response = await api.get<ConsentStatus>(`/consents/status/${user?.id}`);
      if (response.data.needsConsent) {
        setNeedsConsent(true);
        setPendingDocs(response.data.pendingDocuments);
      }
    } catch (error) {
      console.error('Erro ao verificar consentimento', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAccept = async () => {
    setLoading(true);
    try {
      await Promise.all(
        pendingDocs.map((doc) =>
          api.post('/consents', {
            userId: user?.id,
            legalDocumentId: doc.id,
          })
        )
      );
      setNeedsConsent(false);
      toast.success('Termos aceitos com sucesso!');
    } catch (error) {
      toast.error('Erro ao aceitar termos. Tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  if (!needsConsent) {
    return <>{children}</>;
  }

  return (
    <>
      {children}
      <Modal 
        isOpen={needsConsent} 
        onClose={() => {}} 
        title="Termos e Condições (LGPD)"
      >
        <div className="space-y-6 max-h-[60vh] overflow-y-auto pr-2">
          {loading ? (
            <p>Carregando termos...</p>
          ) : (
            pendingDocs.map((doc) => (
              <div key={doc.id} className="space-y-2 border-b pb-4 last:border-0">
                <h3 className="text-lg font-bold text-text-heading">{doc.title}</h3>
                <div className="text-sm text-text-body whitespace-pre-wrap">
                  {doc.content}
                </div>
                <p className="text-xs text-text-muted italic">Versão: {doc.version}</p>
              </div>
            ))
          )}
        </div>
        <div className="mt-6 flex justify-end">
          <Button onClick={handleAccept} disabled={loading}>
            Aceitar e Continuar
          </Button>
        </div>
      </Modal>
    </>
  );
}
