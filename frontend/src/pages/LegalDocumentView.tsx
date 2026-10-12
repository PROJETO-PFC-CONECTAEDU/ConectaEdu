import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../lib/api';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { ArrowLeft } from 'lucide-react';

interface LegalDocument {
  title: string;
  content: string;
  version: string;
  publishedAt: string;
}

export function LegalDocumentView() {
  const { type } = useParams<{ type: string }>();
  const [document, setDocument] = useState<LegalDocument | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchDocument() {
      try {
        const response = await api.get(`/legal-documents/current/${type}`);
        setDocument(response.data);
      } catch (error) {
        console.error('Erro ao buscar documento legal:', error);
      } finally {
        setLoading(false);
      }
    }

    if (type) {
      fetchDocument();
    }
  }, [type]);

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <div className="text-brand-primary animate-pulse font-medium">Carregando...</div>
      </div>
    );
  }

  if (!document) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <Card className="p-8 text-center max-w-md">
          <h2 className="text-2xl font-bold text-text-heading mb-4">Documento não encontrado</h2>
          <p className="text-text-body mb-6">Não foi possível encontrar a versão atual deste documento.</p>
          <Link to="/login">
            <Button>Voltar para o Login</Button>
          </Link>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl mx-auto">
        <div className="mb-8">
          <Link to="/login" className="flex items-center gap-2 text-text-muted hover:text-brand-primary transition-colors mb-4 w-fit">
            <ArrowLeft size={16} />
            <span>Voltar para o Login</span>
          </Link>
          
          <div className="flex items-center gap-3">
            <div>
              <h1 className="text-3xl font-bold text-text-heading font-display">{document.title}</h1>
              <p className="text-text-muted">Versão {document.version} • Publicado em {new Date(document.publishedAt).toLocaleDateString('pt-BR')}</p>
            </div>
          </div>
        </div>

        <Card className="p-8 md:p-12 shadow-sm">
          <div className="prose prose-brand max-w-none">
            {document.content.split('\n').map((paragraph, index) => (
              paragraph.trim() ? (
                <p key={index} className="text-text-body leading-relaxed mb-4">
                  {paragraph}
                </p>
              ) : <br key={index} />
            ))}
          </div>
        </Card>

        <div className="mt-8 text-center">
          <div className="flex justify-center gap-4 mb-4">
            <Link
              to="/legal/TERMS_OF_USE"
              className={`text-sm transition-colors ${
                type === 'TERMS_OF_USE' 
                  ? 'text-brand-primary font-bold' 
                  : 'text-text-muted hover:text-brand-primary'
              }`}
            >
              Termos de Uso
            </Link>
            <span className="text-text-muted text-sm">•</span>
            <Link
              to="/legal/PRIVACY_POLICY"
              className={`text-sm transition-colors ${
                type === 'PRIVACY_POLICY' 
                  ? 'text-brand-primary font-bold' 
                  : 'text-text-muted hover:text-brand-primary'
              }`}
            >
              Política de Privacidade
            </Link>
          </div>
          <p className="text-sm text-text-muted">
            © {new Date().getFullYear()} ConectaEdu. Todos os direitos reservados.
          </p>
        </div>
      </div>
    </div>
  );
}
