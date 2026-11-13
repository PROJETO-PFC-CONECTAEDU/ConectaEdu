import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../lib/api';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { Modal } from '../components/ui/Modal';
import { Header } from '../components/layout/Header';
import toast from 'react-hot-toast';
import { User, Shield, CheckCircle, XCircle, Key, Download, Trash2 } from 'lucide-react';
import { formatRole, formatLegalDocumentType, formatDateTime } from '../utils/format';
import { Input } from '../components/ui/Input';

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
  
  // Perfil
  const [isEditingProfile, setIsEditingProfile] = useState(false);
  const [profileData, setProfileData] = useState({ name: user?.name || '' });
  
  // Senha
  const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
  const [passwordData, setPasswordData] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });

  // Exclusão
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  useEffect(() => {
    fetchConsents();
    if (user) {
      setProfileData({ name: user.name });
    }
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
      localStorage.removeItem('@ConectaEdu:token');
      localStorage.removeItem('@ConectaEdu:user');
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
        title: `${formatLegalDocumentType(consent.documentType)} v${consent.documentVersion}`,
        content: response.data.content
      });
    } catch (error) {
      toast.error('Erro ao buscar conteúdo do documento.');
    }
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.put('/profile', profileData);
      toast.success('Perfil atualizado com sucesso!');
      setIsEditingProfile(false);
    } catch (error) {
      toast.error('Erro ao atualizar perfil.');
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      toast.error('As senhas não coincidem!');
      return;
    }
    try {
      await api.patch('/profile/change-password', {
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword
      });
      toast.success('Senha alterada com sucesso!');
      setIsPasswordModalOpen(false);
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (error) {
      toast.error('Erro ao alterar senha. Verifique se a senha atual está correta.');
    }
  };

  const handleExportData = async () => {
    try {
      const response = await api.get('/profile/export');
      const blob = new Blob([JSON.stringify(response.data, null, 2)], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `dados-conectaedu-${user?.name}.json`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      toast.success('Dados exportados com sucesso!');
    } catch (error) {
      toast.error('Erro ao exportar dados.');
    }
  };

  const handleDeleteAccount = async () => {
    try {
      await api.delete('/profile');
      toast.success('Sua conta foi desativada com sucesso.');
      localStorage.removeItem('@ConectaEdu:token');
      localStorage.removeItem('@ConectaEdu:user');
      setTimeout(() => {
        window.location.reload();
      }, 1500);
    } catch (error) {
      toast.error('Erro ao excluir conta.');
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
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-2">
                <User className="text-brand-primary" size={24} />
                <h3 className="text-lg font-bold text-text-heading">Informações Pessoais</h3>
              </div>
              {!isEditingProfile && (
                <Button variant="outline" size="sm" onClick={() => setIsEditingProfile(true)}>
                  Editar Perfil
                </Button>
              )}
            </div>

            {isEditingProfile ? (
              <form onSubmit={handleUpdateProfile} className="space-y-4">
                <div className="grid grid-cols-1 gap-4">
                  <Input 
                    label="Nome Completo"
                    value={profileData.name}
                    onChange={(e) => setProfileData({ ...profileData, name: e.target.value })}
                    required
                  />
                </div>
                <div className="flex justify-end gap-2">
                  <Button variant="outline" type="button" onClick={() => setIsEditingProfile(false)}>
                    Cancelar
                  </Button>
                  <Button type="submit">
                    Salvar Alterações
                  </Button>
                </div>
              </form>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <p className="text-sm font-medium text-text-muted mb-1">Nome Completo</p>
                  <p className="text-text-heading font-medium">{user?.name}</p>
                </div>
                <div>
                  <p className="text-sm font-medium text-text-muted mb-1">E-mail Cadastrado</p>
                  <div className="flex items-center gap-2">
                    <p className="text-text-heading font-medium">{user?.email}</p>
                    {user?.emailConfirmed ? (
                      <span className="text-[10px] bg-green-100 text-green-700 px-2 py-0.5 rounded-full font-bold uppercase">Confirmado</span>
                    ) : (
                      <span className="text-[10px] bg-yellow-100 text-yellow-700 px-2 py-0.5 rounded-full font-bold uppercase">Pendente</span>
                    )}
                  </div>
                </div>
              </div>
            )}
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-2 mb-6">
              <Key className="text-brand-primary" size={24} />
              <h3 className="text-lg font-bold text-text-heading">Segurança</h3>
            </div>
            <div className="flex items-center justify-between">
              <div>
                <p className="text-text-heading font-medium">Senha</p>
                <p className="text-sm text-text-body">Altere sua senha de acesso periodicamente para sua segurança.</p>
              </div>
              <Button variant="outline" onClick={() => setIsPasswordModalOpen(true)}>
                Alterar Senha
              </Button>
            </div>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-2 mb-6">
              <Shield className="text-brand-primary" size={24} />
              <h3 className="text-lg font-bold text-text-heading">Privacidade e Dados</h3>
            </div>
            
            <div className="space-y-6">
              <div className="flex items-center justify-between p-4 border rounded-lg">
                <div className="flex gap-4">
                  <div className="p-2 bg-brand-primary/10 rounded-lg text-brand-primary h-fit">
                    <Download size={20} />
                  </div>
                  <div>
                    <p className="text-text-heading font-bold">Exportar meus dados</p>
                    <p className="text-sm text-text-body">Baixe uma cópia de todos os seus dados pessoais armazenados na plataforma.</p>
                  </div>
                </div>
                <Button variant="outline" onClick={handleExportData}>
                  Exportar JSON
                </Button>
              </div>

              <div className="flex items-center justify-between p-4 border border-red-100 rounded-lg bg-red-50/30">
                <div className="flex gap-4">
                  <div className="p-2 bg-red-100 rounded-lg text-red-600 h-fit">
                    <Trash2 size={20} />
                  </div>
                  <div>
                    <p className="text-red-600 font-bold">Excluir minha conta</p>
                    <p className="text-sm text-text-body">Ao excluir sua conta, todos os seus dados serão anonimizados ou removidos.</p>
                  </div>
                </div>
                <Button 
                  variant="outline" 
                  className="text-red-600 border-red-200 hover:bg-red-50"
                  onClick={() => setIsDeleteModalOpen(true)}
                >
                  Excluir Conta
                </Button>
              </div>
            </div>
          </Card>

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
                        {formatLegalDocumentType(consent.documentType)}
                        <span className="ml-2 text-sm font-normal text-text-muted">v{consent.documentVersion}</span>
                      </button>
                      <p className="text-xs text-text-body">
                        Aceito em: {formatDateTime(consent.acceptedAt)}
                      </p>
                      {consent.withdrawnAt && (
                        <p className="text-xs text-brand-accent font-medium">
                          Revogado em: {formatDateTime(consent.withdrawnAt)}
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

      <ConfirmModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={handleDeleteAccount}
        title="Excluir Minha Conta"
        description="Esta ação é irreversível. Seus dados pessoais serão removidos ou anonimizados e você perderá o acesso à plataforma. Deseja realmente excluir sua conta?"
        confirmText="Sim, excluir conta"
        cancelText="Cancelar"
      />

      <Modal
        isOpen={isPasswordModalOpen}
        onClose={() => setIsPasswordModalOpen(false)}
        title="Alterar Senha"
      >
        <form onSubmit={handleChangePassword} className="space-y-4">
          <Input 
            label="Senha Atual"
            type="password"
            value={passwordData.currentPassword}
            onChange={(e) => setPasswordData({ ...passwordData, currentPassword: e.target.value })}
            required
          />
          <Input 
            label="Nova Senha"
            type="password"
            value={passwordData.newPassword}
            onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
            required
          />
          <Input 
            label="Confirmar Nova Senha"
            type="password"
            value={passwordData.confirmPassword}
            onChange={(e) => setPasswordData({ ...passwordData, confirmPassword: e.target.value })}
            required
          />
          <div className="flex justify-end gap-2 pt-4">
            <Button variant="outline" type="button" onClick={() => setIsPasswordModalOpen(false)}>
              Cancelar
            </Button>
            <Button type="submit">
              Atualizar Senha
            </Button>
          </div>
        </form>
      </Modal>

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
