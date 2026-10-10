import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../lib/api';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Card } from '../components/ui/Card';
import toast from 'react-hot-toast';
import { ShieldAlert, History, Calendar, CheckCircle, Archive } from 'lucide-react';
import { Header } from '../components/layout/Header';

interface LegalDocument {
  id: string;
  documentType: string;
  version: string;
  title: string;
  content: string;
  status: 'PUBLISHED' | 'ARCHIVED';
  changeSummary: string;
  publishedAt: string;
  archivedAt?: string;
}

export function LegalDocuments() {
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [documents, setDocuments] = useState<LegalDocument[]>([]);
  const [formData, setFormData] = useState({
    documentType: 'TERMS_OF_USE',
    title: '',
    content: '',
    version: '',
    changeSummary: ''
  });

  const fetchDocuments = useCallback(async () => {
    try {
      const response = await api.get('/legal-documents');
      setDocuments(response.data);
    } catch (error) {
      console.error('Erro ao buscar histórico de documentos:', error);
    }
  }, []);

  useEffect(() => {
    fetchDocuments();
  }, [fetchDocuments]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await api.post('/legal-documents', {
        ...formData,
        publisherUserId: user?.id
      });
      toast.success('Documento publicado com sucesso!');
      setFormData({
        documentType: 'TERMS_OF_USE',
        title: '',
        content: '',
        version: '',
        changeSummary: ''
      });
      fetchDocuments();
    } catch (error: unknown) {
      const axiosError = error as any;
      const message = axiosError.response?.data?.message || 'Erro ao publicar documento.';
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusBadge = (status: string) => {
    if (status === 'PUBLISHED') {
      return (
        <span className="flex items-center gap-1 text-[10px] font-bold text-green-600 bg-green-50 px-2 py-0.5 rounded-full border border-green-200 uppercase">
          <CheckCircle size={10} />
          Em vigor
        </span>
      );
    }
    return (
      <span className="flex items-center gap-1 text-[10px] font-bold text-gray-500 bg-gray-50 px-2 py-0.5 rounded-full border border-gray-200 uppercase">
        <Archive size={10} />
        Arquivado
      </span>
    );
  };

  return (
    <div className="flex-1">
      <Header 
        title="Documentação" 
        subtitle="Gerencie os termos de uso e políticas de privacidade da plataforma." 
      />
      
      <div className="p-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-6">
          <Card className="p-6">
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-1">
                  <label className="text-sm font-medium text-text-heading">Tipo de Documento</label>
                  <select 
                    className="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary bg-white"
                    value={formData.documentType}
                    onChange={(e) => setFormData({ ...formData, documentType: e.target.value })}
                    required
                  >
                    <option value="TERMS_OF_USE">Termos de Uso</option>
                    <option value="PRIVACY_POLICY">Política de Privacidade</option>
                  </select>
                </div>
                <Input 
                  label="Versão (ex: 1.0)"
                  placeholder="1.0"
                  value={formData.version}
                  onChange={(e) => setFormData({ ...formData, version: e.target.value })}
                  required
                />
              </div>

              <Input 
                label="Título"
                placeholder="Ex: Termos e Condições de Uso - Setembro 2026"
                value={formData.title}
                onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                required
              />

              <div className="space-y-1">
                <label className="text-sm font-medium text-text-heading">Conteúdo</label>
                <textarea 
                  className="w-full px-4 py-2 rounded-lg border border-gray-200 focus:outline-none focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary min-h-[300px]"
                  value={formData.content}
                  onChange={(e) => setFormData({ ...formData, content: e.target.value })}
                  required
                />
              </div>

              <Input 
                label="Resumo das Alterações"
                placeholder="Descreva brevemente o que mudou nesta versão"
                value={formData.changeSummary}
                onChange={(e) => setFormData({ ...formData, changeSummary: e.target.value })}
              />

              <div className="flex justify-end pt-4">
                <Button type="submit" disabled={loading}>
                  {loading ? 'Publicando...' : 'Publicar Nova Versão'}
                </Button>
              </div>
            </form>
          </Card>

          <Card className="p-6">
            <div className="flex items-center gap-2 mb-6">
              <History className="text-brand-primary" size={24} />
              <h3 className="text-lg font-bold text-text-heading">Histórico de Documentos</h3>
            </div>

            <div className="space-y-4">
              {documents.length === 0 ? (
                <div className="text-center py-8">
                  <p className="text-sm text-text-body">Nenhum documento registrado no histórico.</p>
                </div>
              ) : (
                documents.map((doc) => (
                  <div key={doc.id} className="p-4 rounded-lg border border-gray-100 bg-gray-50/50 hover:bg-gray-50 transition-colors">
                    <div className="flex justify-between items-start mb-2">
                      <div className="space-y-1">
                        <div className="flex items-center gap-2 flex-wrap">
                          <h4 className="font-bold text-text-heading">{doc.title}</h4>
                          {getStatusBadge(doc.status)}
                        </div>
                        <p className="text-xs text-text-body">
                          {doc.documentType === 'TERMS_OF_USE' ? 'Termos de Uso' : 'Política de Privacidade'} • Versão {doc.version}
                        </p>
                      </div>
                      <div className="text-right shrink-0">
                        <div className="flex items-center justify-end gap-1 text-[10px] font-medium text-text-body">
                          <Calendar size={10} />
                          {formatDate(doc.publishedAt)}
                        </div>
                      </div>
                    </div>
                    {doc.changeSummary && (
                      <div className="mt-2 text-xs text-text-body bg-white/80 p-2 rounded border border-gray-100/50">
                        <span className="font-bold text-brand-primary mr-1">Resumo das alterações:</span>
                        {doc.changeSummary}
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          </Card>
        </div>

        <div className="space-y-6">
          <Card className="p-6 bg-brand-primary/5 border-brand-primary/20">
            <div className="flex gap-3">
              <ShieldAlert className="text-brand-primary shrink-0" size={24} />
              <div>
                <h3 className="font-bold text-text-heading">Atenção</h3>
                <p className="text-sm text-text-body mt-1">
                  Ao publicar uma nova versão, todos os usuários serão solicitados a aceitá-la no próximo acesso.
                </p>
              </div>
            </div>
          </Card>

          <Card className="p-6">
            <h3 className="font-bold text-text-heading mb-4">Dicas de Formatação</h3>
            <ul className="text-sm text-text-body space-y-2 list-disc pl-4">
              <li>Use parágrafos claros e objetivos.</li>
              <li>Destaque seções importantes em caixa alta ou com títulos.</li>
              <li>Certifique-se de que a versão segue o padrão X.Y.</li>
            </ul>
          </Card>
        </div>
      </div>
    </div>
  </div>
  );
}
