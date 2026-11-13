import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../lib/api';
import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { RatingSlider } from '../components/evaluations/RatingSlider';
import { EvaluationCard } from '../components/evaluations/EvaluationCard';
import { Star, History, Send } from 'lucide-react';

interface Demand {
  id: string;
  title: string;
  schoolName: string;
  studentId: string;
  studentName: string;
  hasStudentFeedback: boolean;
  hasDirectorFeedback: boolean;
}

interface Evaluation {
  id: string;
  demandTitle: string;
  studentName?: string;
  evaluatedBy?: string;
  engagement?: number;
  progress?: number;
  infrastructure?: number;
  knowledge?: number;
  punctuality?: number;
  teaching?: number;
  commitment?: number;
  empathy?: number;
  comments: string;
  createdAt: string;
}

export function Evaluations() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'evaluate' | 'history'>('evaluate');
  const [demands, setDemands] = useState<Demand[]>([]);
  const [history, setHistory] = useState<Evaluation[]>([]);
  const [loading, setLoading] = useState(true);

  // Form state
  const [selectedDemandId, setSelectedDemandId] = useState('');
  
  // Student evaluation of class
  const [engagement, setEngagement] = useState(5);
  const [progress, setProgress] = useState(5);
  const [infrastructure, setInfrastructure] = useState(5);
  
  // Director evaluation of student
  const [knowledge, setKnowledge] = useState(5);
  const [punctuality, setPunctuality] = useState(5);
  const [teaching, setTeaching] = useState(5);
  const [commitment, setCommitment] = useState(5);
  const [empathy, setEmpathy] = useState(5);

  const [comments, setComments] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const isDirector = user?.role === 'SCHOOL_DIRECTOR';
  const isStudent = user?.role === 'STUDENT';

  const loadData = React.useCallback(async () => {
    if (!user) return;
    try {
      const demandsResponse = await api.get('/demands/completed');
      
      setDemands(demandsResponse.data.filter((d: Demand) => 
        isStudent ? !d.hasStudentFeedback : !d.hasDirectorFeedback
      ));
      
      if (isStudent) {
        const historyResponse = await api.get(`/evaluations/student/${user.id}`);
        setHistory(historyResponse.data);
      } else if (isDirector) {
        const historyResponse = await api.get(`/evaluations/director/${user.id}`);
        setHistory(historyResponse.data);
      }
    } catch (error) {
      console.error('Erro ao carregar dados de avaliações:', error);
    } finally {
      setLoading(false);
    }
  }, [user, isStudent]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!selectedDemandId) {
      alert('Selecione uma opção para avaliar');
      return;
    }

    const selectedDemand = demands.find(d => d.id === selectedDemandId);
    if (!selectedDemand) return;

    setSubmitting(true);
    try {
      if (isStudent) {
        await api.post('/evaluations/class', {
          demandId: selectedDemandId,
          engagement,
          progress,
          infrastructure,
          comments
        });
      } else if (isDirector) {
        await api.post('/evaluations/student', {
          demandId: selectedDemandId,
          studentId: selectedDemand.studentId,
          knowledge,
          punctuality,
          teaching,
          commitment,
          empathy,
          comments
        });
      }

      alert('Avaliação enviada com sucesso!');
      // Reset form
      setSelectedDemandId('');
      setEngagement(5);
      setProgress(5);
      setInfrastructure(5);
      setKnowledge(5);
      setPunctuality(5);
      setTeaching(5);
      setCommitment(5);
      setEmpathy(5);
      setComments('');
      
      // Refresh data
      loadData();
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
    <div className="flex-1">
      <Header 
        title="Avaliações" 
        subtitle="Envie feedback sobre suas experiências ou veja o que dizem sobre você."
      />

      <div className="p-8 max-w-4xl mx-auto space-y-8">

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
            {isStudent ? 'Avaliações Recebidas' : 'Feedback dos Estudantes'}
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
              <label className="text-sm font-medium text-text-heading">
                {isStudent ? 'Selecione a Vaga/Escola' : 'Selecione a Demanda/Estudante'}
              </label>
              <select
                value={selectedDemandId}
                onChange={(e) => setSelectedDemandId(e.target.value)}
                className="w-full p-3 rounded-lg border border-border-default bg-white focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary outline-none transition-all"
                required
              >
                <option value="">Selecione uma opção...</option>
                {demands.map((demand) => (
                  <option key={demand.id} value={demand.id}>
                    {demand.title} - {isStudent ? demand.schoolName : demand.studentName}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-5 md:grid-cols-1 gap-8">
              {isStudent ? (
                <>
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
                </>
              ) : (
                <>
                  <RatingSlider 
                    label="Domínio do Conteúdo (Conhecimento)" 
                    value={knowledge} 
                    onChange={setKnowledge} 
                  />
                  <RatingSlider 
                    label="Pontualidade e Frequência" 
                    value={punctuality} 
                    onChange={setPunctuality} 
                  />
                  <RatingSlider 
                    label="Didática e Metodologia" 
                    value={teaching} 
                    onChange={setTeaching} 
                  />
                  <RatingSlider 
                    label="Comprometimento e Proatividade" 
                    value={commitment} 
                    onChange={setCommitment} 
                  />
                  <RatingSlider 
                    label="Empatia e Relacionamento" 
                    value={empathy} 
                    onChange={setEmpathy} 
                  />
                </>
              )}
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
            history.map((item) => {
              const metrics = isStudent ? [
                { label: 'Conhecimento', value: item.knowledge ?? 0 },
                { label: 'Pontualidade', value: item.punctuality ?? 0 },
                { label: 'Didática', value: item.teaching ?? 0 },
                { label: 'Comprometimento', value: item.commitment ?? 0 },
                { label: 'Empatia', value: item.empathy ?? 0 },
              ] : [
                { label: 'Engajamento', value: item.engagement ?? 0 },
                { label: 'Progresso', value: item.progress ?? 0 },
                { label: 'Infraestrutura', value: item.infrastructure ?? 0 },
              ];

              return (
                <EvaluationCard
                  key={item.id}
                  evaluatorName={isStudent ? item.demandTitle : `${item.studentName} - ${item.demandTitle}`}
                  date={item.createdAt}
                  metrics={metrics}
                  comments={item.comments}
                />
              );
            })
          ) : (
            <div className="text-center py-12 text-text-muted bg-slate-50 rounded-xl border-2 border-dashed border-border-default">
              Nenhuma avaliação recebida ainda.
            </div>
          )}
        </div>
      )}
      </div>
    </div>
  );
}
