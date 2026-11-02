import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../lib/api';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { RatingSlider } from '../components/evaluations/RatingSlider';
import { EvaluationCard } from '../components/evaluations/EvaluationCard';
import { Star, History, Send } from 'lucide-react';

interface Demand {
  id: string;
  title: string;
  schoolName: string;
}

interface StudentEvaluation {
  id: string;
  demandTitle: string;
  evaluatedBy: string; // Isso deve ser o nome do diretor, mas o DTO manda UUID. O backend pode precisar de ajuste ou eu busco o nome.
  knowledge: number;
  punctuality: number;
  teaching: number;
  commitment: number;
  empathy: number;
  comments: string;
  createdAt: string;
}

export function Evaluations() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'evaluate' | 'history'>('evaluate');
  const [demands, setDemands] = useState<Demand[]>([]);
  const [history, setHistory] = useState<StudentEvaluation[]>([]);
  const [loading, setLoading] = useState(true);

  // Form state
  const [selectedDemandId, setSelectedDemandId] = useState('');
  const [engagement, setEngagement] = useState(5);
  const [progress, setProgress] = useState(5);
  const [infrastructure, setInfrastructure] = useState(5);
  const [comments, setComments] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    async function loadData() {
      if (!user) return;
      try {
        const [demandsResponse, historyResponse] = await Promise.all([
          api.get('/demands/ongoing'),
          api.get(`/evaluations/student/${user.id}`)
        ]);
        setDemands(demandsResponse.data);
        setHistory(historyResponse.data);
      } catch (error) {
        console.error('Erro ao carregar dados de avaliações:', error);
      } finally {
        setLoading(false);
      }
    }

    loadData();
  }, [user]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedDemandId) {
      alert('Selecione uma vaga para avaliar');
      return;
    }

    setSubmitting(true);
    try {
      await api.post('/evaluations/class', {
        demandId: selectedDemandId,
        engagement,
        progress,
        infrastructure,
        comments
      });
      alert('Avaliação enviada com sucesso!');
      // Reset form
      setSelectedDemandId('');
      setEngagement(5);
      setProgress(5);
      setInfrastructure(5);
      setComments('');
    } catch (error) {
      console.error('Erro ao enviar avaliação:', error);
      alert('Erro ao enviar avaliação. Tente novamente.');
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <div className="p-8 text-center">Carregando...</div>;
  }

  return (
    <div className="p-8 max-w-4xl mx-auto space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-text-heading font-display">Avaliações</h1>
        <p className="text-text-body">Envie feedback sobre suas experiências ou veja o que dizem sobre você.</p>
      </div>

      <div className="flex border-b border-border-default">
        <button
          onClick={() => setActiveTab('evaluate')}
          className={`px-6 py-3 text-sm font-medium transition-colors relative ${
            activeTab === 'evaluate' ? 'text-brand-primary' : 'text-text-muted hover:text-text-body'
          }`}
        >
          <div className="flex items-center gap-2">
            <Star size={18} />
            Avaliar
          </div>
          {activeTab === 'evaluate' && (
            <div className="absolute bottom-0 left-0 w-full h-0.5 bg-brand-primary" />
          )}
        </button>
        <button
          onClick={() => setActiveTab('history')}
          className={`px-6 py-3 text-sm font-medium transition-colors relative ${
            activeTab === 'history' ? 'text-brand-primary' : 'text-text-muted hover:text-text-body'
          }`}
        >
          <div className="flex items-center gap-2">
            <History size={18} />
            Histórico
          </div>
          {activeTab === 'history' && (
            <div className="absolute bottom-0 left-0 w-full h-0.5 bg-brand-primary" />
          )}
        </button>
      </div>

      {activeTab === 'evaluate' ? (
        <Card className="p-8">
          <form onSubmit={handleSubmit} className="space-y-8">
            <div className="space-y-2">
              <label className="text-sm font-medium text-text-heading">Selecione a Vaga/Escola</label>
              <select
                value={selectedDemandId}
                onChange={(e) => setSelectedDemandId(e.target.value)}
                className="w-full p-3 rounded-lg border border-border-default bg-white focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                required
              >
                <option value="">Selecione uma opção...</option>
                {demands.map((demand) => (
                  <option key={demand.id} value={demand.id}>
                    {demand.title} - {demand.schoolName}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-1 gap-8">
              <RatingSlider 
                label="Engajamento da Turma" 
                value={engagement} 
                onChange={setEngagement} 
              />
              <RatingSlider 
                label="Progresso do Conteúdo" 
                value={progress} 
                onChange={setProgress} 
              />
              <RatingSlider 
                label="Infraestrutura da Escola" 
                value={infrastructure} 
                onChange={setInfrastructure} 
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium text-text-heading">Comentários Adicionais</label>
              <textarea
                value={comments}
                onChange={(e) => setComments(e.target.value)}
                placeholder="Conte-nos mais sobre sua experiência..."
                className="w-full p-4 rounded-lg border border-border-default min-h-[120px] focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
              />
            </div>

            <div className="flex justify-end">
              <Button type="submit" disabled={submitting}>
                <Send size={18} className="mr-2" />
                {submitting ? 'Enviando...' : 'Enviar Avaliação'}
              </Button>
            </div>
          </form>
        </Card>
      ) : (
        <div className="space-y-4">
          {history.length > 0 ? (
            history.map((item) => (
              <EvaluationCard
                key={item.id}
                evaluatorName={item.demandTitle} // Usando título da demanda como identificador da fonte
                date={item.createdAt}
                engagement={item.commitment}
                autonomy={item.knowledge}
                punctuality={item.punctuality}
                comments={item.comments}
              />
            ))
          ) : (
            <div className="text-center py-12 text-text-muted bg-slate-50 rounded-xl border-2 border-dashed border-border-default">
              Nenhuma avaliação recebida ainda.
            </div>
          )}
        </div>
      )}
    </div>
  );
}
