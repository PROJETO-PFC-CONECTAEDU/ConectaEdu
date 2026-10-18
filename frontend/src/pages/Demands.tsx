import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import api from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { ClipboardList, Trash2, Plus, Calendar, Users, Clock, Building2, User as UserIcon, Check } from 'lucide-react';
import { toast } from 'react-hot-toast';

interface Candidate {
  id: string;
  name: string;
}

interface Demand {
  id: string;
  title: string;
  description: string;
  subject: string;
  gradeLevel: string;
  pupilAmount: number;
  status: string;
  classDate: string;
  totalHours: string;
  room: string;
  difficultyLevel: string;
  schoolName: string;
  directorName: string;
  studentName?: string;
  candidates: Candidate[];
  isArchived: boolean;
}

const demandSchema = z.object({
  title: z.string().min(3, 'Título deve ter pelo menos 3 caracteres'),
  description: z.string().min(10, 'Descrição deve ter pelo menos 10 caracteres'),
  subject: z.string().min(2, 'Disciplina é obrigatória'),
  gradeLevel: z.string().min(1, 'Nível de escolaridade é obrigatório'),
  pupilAmount: z.number().min(3, 'Quantidade de alunos deve ser pelo menos 3'),
  classDate: z.string().min(1, 'Data da aula é obrigatória'),
  totalHours: z.string().min(1, 'Total de horas é obrigatório'),
  room: z.string().min(1, 'Sala é obrigatória'),
  difficultyLevel: z.string().min(1, 'Grau de dificuldade é obrigatório'),
});

type DemandFormData = z.infer<typeof demandSchema>;

export function Demands() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');

  const isDirector = user?.role === 'SCHOOL_DIRECTOR';
  const isStudent = user?.role === 'STUDENT';

  const { register, handleSubmit, reset, formState: { errors } } = useForm<DemandFormData>({
    resolver: zodResolver(demandSchema)
  });

  const { data: demands, isLoading, error } = useQuery<Demand[]>({
    queryKey: ['demands', user?.role],
    queryFn: async () => {
      if (user?.role === 'STUDENT') {
        const [availableRes, ongoingRes] = await Promise.all([
          api.get('/demands/available/student'),
          api.get('/demands/ongoing'),
        ]);

        const demandsMap = new Map();
        availableRes.data.forEach((d: any) => demandsMap.set(d.id, d));
        ongoingRes.data.forEach((d: any) => demandsMap.set(d.id, d));

        return Array.from(demandsMap.values()) as Demand[];
      }

      const response = await api.get('/demands');
      return response.data;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/demands/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['demands'] });
      toast.success('Demanda removida com sucesso!');
      setDeletingId(null);
    },
    onError: () => {
      toast.error('Erro ao remover demanda.');
    }
  });

  const createMutation = useMutation({
    mutationFn: async (data: DemandFormData) => {
      await api.post('/demands', data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['demands'] });
      toast.success('Demanda criada com sucesso!');
      setIsModalOpen(false);
      reset();
    },
    onError: (err: any) => {
      const message = err.response?.data?.message || 'Erro ao criar demanda.';
      toast.error(message);
    }
  });

  const applyMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.post(`/demands/${id}/apply`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['demands'] });
      toast.success('Candidatura realizada com sucesso!');
    },
    onError: (err: any) => {
      const message = err.response?.data?.message || 'Erro ao realizar candidatura.';
      toast.error(message);
    }
  });


  const onSubmit = (data: DemandFormData) => {
    createMutation.mutate(data);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'WAITING': return 'bg-blue-100 text-blue-700';
      case 'ONGOING': return 'bg-amber-100 text-amber-700';
      case 'FINISHED': return 'bg-emerald-100 text-emerald-700';
      case 'ARCHIVED': return 'bg-gray-100 text-gray-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  const translateStatus = (status: string) => {
    switch (status) {
      case 'WAITING': return 'Aguardando';
      case 'ONGOING': return 'Em Andamento';
      case 'FINISHED': return 'Concluída';
      case 'ARCHIVED': return 'Arquivada';
      default: return status;
    }
  };

  const formatDate = (dateString: string) => {
    try {
      return new Intl.DateTimeFormat('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }).format(new Date(dateString));
    } catch (e) {
      return dateString;
    }
  };

  const filteredDemands = demands?.filter(demand => 
    demand.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    demand.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
    demand.subject.toLowerCase().includes(searchTerm.toLowerCase()) ||
    demand.schoolName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="flex-1">
      <Header 
        title="Demandas de Estágio" 
        subtitle={isDirector ? "Gerencie as solicitações de estagiários para sua escola." : "Visualize e candidate-se às oportunidades de estágio disponíveis."}
        searchValue={searchTerm}
        onSearchChange={setSearchTerm}
        action={
          isDirector && (
            <button 
              onClick={() => setIsModalOpen(true)}
              className="bg-brand-primary text-white px-4 py-2 rounded-lg flex items-center gap-2 font-bold hover:bg-brand-primary/90 transition-colors"
            >
              <Plus size={20} /> Nova Demanda
            </button>
          )
        }
      />

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Nova Demanda"
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Título"
            placeholder="Ex: Reforço de Matemática - 6º Ano"
            error={errors.title?.message}
            {...register('title')}
          />
          <div className="space-y-1">
            <label className="text-sm font-medium text-text-body">Descrição</label>
            <textarea
              className="w-full px-4 py-2 rounded-lg border border-border-default focus:border-brand-primary focus:ring-1 focus:ring-brand-primary outline-none transition-all resize-none h-24 text-sm"
              placeholder="Descreva a demanda em detalhes..."
              {...register('description')}
            />
            {errors.description && <p className="text-xs text-red-500">{errors.description.message}</p>}
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Disciplina"
              placeholder="Ex: Matemática"
              error={errors.subject?.message}
              {...register('subject')}
            />
            <Input
              label="Nível de Escolaridade"
              placeholder="Ex: 6º Ano Fundamental"
              error={errors.gradeLevel?.message}
              {...register('gradeLevel')}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Qtd. Alunos"
              type="number"
              placeholder="Min 3"
              error={errors.pupilAmount?.message}
              {...register('pupilAmount', { valueAsNumber: true })}
            />
            <Input
              label="Data da Aula"
              type="datetime-local"
              error={errors.classDate?.message}
              {...register('classDate')}
            />
          </div>

          <Input
            label="Total de Horas"
            placeholder="Ex: 4h"
            error={errors.totalHours?.message}
            {...register('totalHours')}
          />
          
          <div className="grid grid-cols-2 gap-4">
            <Input
              label="Sala"
              placeholder="Ex: Sala 102"
              error={errors.room?.message}
              {...register('room')}
            />
            <Input
              label="Grau de Dificuldade"
              placeholder="Ex: Médio"
              error={errors.difficultyLevel?.message}
              {...register('difficultyLevel')}
            />
          </div>
          
          <div className="flex gap-3 pt-2">
            <Button 
              type="button" 
              variant="outline" 
              className="flex-1"
              onClick={() => setIsModalOpen(false)}
            >
              Cancelar
            </Button>
            <Button 
              type="submit" 
              className="flex-1"
              disabled={createMutation.isPending}
            >
              {createMutation.isPending ? 'Criando...' : 'Criar Demanda'}
            </Button>
          </div>
        </form>
      </Modal>

      <div className="p-8">
        {isLoading && (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-brand-primary"></div>
          </div>
        )}

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            Erro ao carregar demandas.
          </div>
        )}

        {!isLoading && !error && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredDemands?.map((demand) => (
              <Card key={demand.id} className="hover:border-brand-primary transition-all group">
                <div className="flex items-start justify-between mb-4">
                  <Link to={`/demandas/${demand.id}`} className="flex items-center gap-4 flex-1">
                    <div className="w-12 h-12 rounded-lg bg-brand-primary/10 flex items-center justify-center text-brand-primary">
                      <ClipboardList size={24} />
                    </div>
                    <div>
                      <h4 className="font-bold text-text-heading group-hover:text-brand-primary transition-colors">
                        {demand.title}
                      </h4>
                      <p className="text-xs text-text-muted">{demand.subject} - {demand.gradeLevel}</p>
                    </div>
                  </Link>
                  {isDirector && !demand.isArchived && (
                    <button 
                      onClick={() => setDeletingId(demand.id)}
                      className="text-text-muted hover:text-red-500 p-1"
                    >
                      <Trash2 size={18} />
                    </button>
                  )}
                </div>

                <Link to={`/demandas/${demand.id}`} className="block">
                  <p className="text-sm text-text-body mb-4 line-clamp-2">
                    {demand.description}
                  </p>
                </Link>

                <div className="space-y-2 pt-4 border-t border-border-default">
                  <div className="flex items-center gap-2 text-xs text-text-body">
                    <Building2 size={14} className="text-text-muted shrink-0" />
                    <span className="font-medium">{demand.schoolName}</span>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-text-body">
                    <Calendar size={14} className="text-text-muted shrink-0" />
                    <span>{formatDate(demand.classDate)}</span>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-text-body">
                    <Users size={14} className="text-text-muted shrink-0" />
                    <span>{demand.pupilAmount} alunos</span>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-text-body">
                    <Clock size={14} className="text-text-muted shrink-0" />
                    <span>{demand.totalHours} no total</span>
                  </div>
                  {demand.studentName && (
                    <div className="flex items-center gap-2 text-xs text-text-body">
                      <UserIcon size={14} className="text-brand-primary shrink-0" />
                      <span className="font-medium text-brand-primary">{demand.studentName}</span>
                    </div>
                  )}
                </div>

                <div className="mt-6 flex items-center justify-between">
                  <div className="flex gap-2">
                    <span className={`px-2 py-1 rounded-md text-[10px] font-bold uppercase ${getStatusColor(demand.status)}`}>
                      {translateStatus(demand.status)}
                    </span>
                  </div>
                  {isStudent && demand.status === 'WAITING' && !demand.isArchived && (
                    <button 
                      onClick={() => applyMutation.mutate(demand.id)}
                      disabled={applyMutation.isPending || demand.candidates.some(c => c.id === user?.id)}
                      className="text-xs bg-brand-primary text-white px-3 py-1.5 rounded-md font-bold hover:bg-brand-primary/90 transition-colors disabled:opacity-50 flex items-center gap-1"
                    >
                      {demand.candidates.some(c => c.id === user?.id) ? (
                        <>
                          <Check size={14} /> Candidatado
                        </>
                      ) : (
                        applyMutation.isPending ? 'Candidatando...' : 'Tenho interesse'
                      )}
                    </button>

                  )}
                </div>

                {isDirector && demand.status === 'WAITING' && !demand.isArchived && (
                  <div className="mt-6 pt-4 border-t border-border-default">
                    <p className="text-[10px] font-bold text-text-muted uppercase">
                      Candidaturas: {demand.candidates.length}
                    </p>
                  </div>
                )}
              </Card>
            ))}

            {filteredDemands?.length === 0 && (
              <div className="col-span-full py-12 text-center text-text-muted">
                Nenhuma demanda encontrada.
              </div>
            )}
          </div>
        )}

        <ConfirmModal
          isOpen={!!deletingId}
          onClose={() => setDeletingId(null)}
          onConfirm={() => deletingId && deleteMutation.mutate(deletingId)}
          title="Remover Demanda"
          description="Tem certeza que deseja remover esta demanda? Esta ação não pode ser desfeita."
          isLoading={deleteMutation.isPending}
        />
      </div>
    </div>
  );
}
