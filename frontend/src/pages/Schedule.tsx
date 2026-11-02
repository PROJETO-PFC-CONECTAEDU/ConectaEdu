import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import axios from 'axios';
import api from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { Calendar, Clock, BookOpen, Building2 } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Input } from '../components/ui/Input';
import { formatLessonStatus } from '../utils/format';

export interface Lesson {
  id: string;
  demandId: string;
  schoolName: string;
  subject: string;
  scheduledStart: string;
  scheduledEnd: string;
  status: 'SCHEDULED' | 'CONFIRMED' | 'CANCELED' | 'REGISTERED' | 'SIGNED' | 'VALIDATED';
  enrolledCount: number;
  presentCount?: number;
  confirmedAt?: string;
  registeredAt?: string;
  signedAt?: string;
}

const registrationSchema = z.object({
  presentCount: z.number().min(0, 'Quantidade de alunos deve ser 0 ou mais'),
  contentTaught: z.string().min(10, 'Conteúdo lecionado deve ter pelo menos 10 caracteres').max(2000),
  nineBoxPerformance: z.number().min(1).max(3),
  nineBoxPotential: z.number().min(1).max(3),
});

type RegistrationFormData = z.infer<typeof registrationSchema>;

const cancellationSchema = z.object({
  reason: z.string().min(5, 'Motivo deve ter pelo menos 5 caracteres'),
});

type CancellationFormData = z.infer<typeof cancellationSchema>;

export function Schedule() {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [filterStatus, setFilterStatus] = useState<string>('');
  const [registeringLesson, setRegisteringLesson] = useState<Lesson | null>(null);
  const [cancellingLesson, setCancellingLesson] = useState<Lesson | null>(null);
  const [confirmingId, setConfirmingId] = useState<string | null>(null);

  const isStudent = user?.role === 'STUDENT';

  const { data: lessons, isLoading } = useQuery<Lesson[]>({
    queryKey: ['lessons', user?.id, filterStatus],
    queryFn: async () => {
      const response = await api.get(`/lessons/student/${user?.id}`, {
        params: filterStatus ? { status: filterStatus } : {}
      });
      return response.data;
    },
    enabled: !!user?.id,
  });

  const confirmMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.patch(`/lessons/${id}/confirm`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['lessons'] });
      toast.success('Aula confirmada com sucesso!');
      setConfirmingId(null);
    },
    onError: (error: unknown) => {
      if (axios.isAxiosError(error)) {
        toast.error(error.response?.data?.message || 'Erro ao confirmar aula');
      } else {
        toast.error('Erro ao confirmar aula');
      }
    }
  });

  const registerMutation = useMutation({
    mutationFn: async (data: { id: string; body: RegistrationFormData }) => {
      await api.patch(`/lessons/${data.id}/register`, data.body);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['lessons'] });
      toast.success('Aula registrada com sucesso!');
      setRegisteringLesson(null);
    },
    onError: (error: unknown) => {
      if (axios.isAxiosError(error)) {
        toast.error(error.response?.data?.message || 'Erro ao registrar aula');
      } else {
        toast.error('Erro ao registrar aula');
      }
    }
  });

  const cancelMutation = useMutation({
    mutationFn: async (data: { id: string; reason: string }) => {
      await api.patch(`/lessons/${data.id}/cancel`, { reason: data.reason });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['lessons'] });
      toast.success('Aula cancelada com sucesso!');
      setCancellingLesson(null);
    },
    onError: (error: unknown) => {
      if (axios.isAxiosError(error)) {
        toast.error(error.response?.data?.message || 'Erro ao cancelar aula');
      } else {
        toast.error('Erro ao cancelar aula');
      }
    }
  });

  const { register, handleSubmit: handleRegisterSubmit, formState: { errors: regErrors } } = useForm<RegistrationFormData>({
    resolver: zodResolver(registrationSchema)
  });

  const { register: registerCancel, handleSubmit: handleCancelSubmit, formState: { errors: cancelErrors } } = useForm<CancellationFormData>({
    resolver: zodResolver(cancellationSchema)
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'SCHEDULED': return 'bg-blue-100 text-blue-800';
      case 'CONFIRMED': return 'bg-green-100 text-green-800';
      case 'CANCELED': return 'bg-red-100 text-red-800';
      case 'REGISTERED': return 'bg-purple-100 text-purple-800';
      case 'SIGNED': return 'bg-emerald-100 text-emerald-800';
      case 'VALIDATED': return 'bg-emerald-100 text-emerald-700';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusLabel = (status: string) => {
    return formatLessonStatus(status);
  };

  return (
    <div className="flex-1">
      <Header 
        title="Agenda de Aulas" 
        subtitle="Acompanhe e gerencie suas aulas de reforço"
      />

      <div className="p-8 space-y-8">
        <div className="flex gap-2 overflow-x-auto pb-2">
        <Button 
          variant={filterStatus === '' ? 'primary' : 'outline'} 
          onClick={() => setFilterStatus('')}
          size="sm"
        >
          Todas
        </Button>
        <Button 
          variant={filterStatus === 'SCHEDULED' ? 'primary' : 'outline'} 
          onClick={() => setFilterStatus('SCHEDULED')}
          size="sm"
        >
          Agendadas
        </Button>
        <Button 
          variant={filterStatus === 'CONFIRMED' ? 'primary' : 'outline'} 
          onClick={() => setFilterStatus('CONFIRMED')}
          size="sm"
        >
          Confirmadas
        </Button>
        <Button 
          variant={filterStatus === 'REGISTERED' ? 'primary' : 'outline'} 
          onClick={() => setFilterStatus('REGISTERED')}
          size="sm"
        >
          Registradas
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {isLoading ? (
          <p>Carregando aulas...</p>
        ) : lessons?.length === 0 ? (
          <div className="col-span-full flex flex-col items-center justify-center py-12 text-text-muted bg-white rounded-xl border border-dashed border-gray-300">
            <Calendar size={48} className="mb-4 opacity-20" />
            <p>Nenhuma aula encontrada.</p>
          </div>
        ) : (
          lessons?.map((lesson) => (
            <Card key={lesson.id} className="flex flex-col h-full hover:shadow-md transition-shadow">
              <div className="flex justify-between items-start mb-4">
                <span className={`px-2 py-1 rounded-full text-xs font-semibold ${getStatusColor(lesson.status)}`}>
                  {getStatusLabel(lesson.status)}
                </span>
                <div className="flex items-center text-text-muted text-sm font-medium">
                  <Calendar size={14} className="mr-1" />
                  {new Date(lesson.scheduledStart).toLocaleDateString()}
                </div>
              </div>

              <h3 className="text-lg font-bold mb-2 text-text-heading">{lesson.subject}</h3>
              <p className="text-text-muted text-sm mb-4 flex items-center">
                <Building2 size={14} className="mr-1" />
                {lesson.schoolName}
              </p>

              <div className="space-y-3 mb-6 flex-1">
                <div className="flex items-center text-sm text-text-dark">
                  <Clock size={16} className="mr-3 text-brand-primary" />
                  <span className="font-medium">
                    {new Date(lesson.scheduledStart).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - 
                    {new Date(lesson.scheduledEnd).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>
                <div className="flex items-center text-sm text-text-dark">
                  <BookOpen size={16} className="mr-3 text-brand-primary" />
                  <span>{lesson.enrolledCount} alunos previstos</span>
                </div>
              </div>

              <div className="flex flex-col gap-2">
                {lesson.status === 'SCHEDULED' && isStudent && (
                  <Button 
                    className="w-full" 
                    size="sm"
                    onClick={() => setConfirmingId(lesson.id)}
                  >
                    Confirmar Aula
                  </Button>
                )}
                {lesson.status === 'CONFIRMED' && isStudent && (
                  <Button 
                    className="w-full" 
                    size="sm"
                    onClick={() => setRegisteringLesson(lesson)}
                  >
                    Registrar Realização
                  </Button>
                )}
                {['SCHEDULED', 'CONFIRMED'].includes(lesson.status) && (
                  <Button 
                    variant="outline" 
                    size="sm"
                    className="text-red-600 hover:bg-red-50 hover:border-red-200"
                    onClick={() => setCancellingLesson(lesson)}
                  >
                    Cancelar
                  </Button>
                )}
                {lesson.status === 'REGISTERED' && (
                   <p className="text-xs text-center text-text-muted italic">Aguardando assinatura da escola</p>
                )}
              </div>
            </Card>
          ))
        )}
      </div>
    </div>

    <Modal
        isOpen={!!registeringLesson}
        onClose={() => setRegisteringLesson(null)}
        title="Registrar Realização da Aula"
      >
        <form onSubmit={handleRegisterSubmit((data) => registerMutation.mutate({ id: registeringLesson!.id, body: data }))} className="space-y-4">
          <Input
            label="Alunos Presentes"
            type="number"
            {...register('presentCount', { valueAsNumber: true })}
            error={regErrors.presentCount?.message}
          />
          <div className="space-y-1">
            <label className="text-sm font-medium text-text-heading">Conteúdo Lecionado</label>
            <textarea
              className="w-full p-3 rounded-lg border border-border-default bg-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary/20 min-h-[100px]"
              {...register('contentTaught')}
              placeholder="Descreva o que foi abordado na aula..."
            />
            {regErrors.contentTaught && <p className="text-xs text-red-500">{regErrors.contentTaught.message}</p>}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-1">
              <label className="text-sm font-medium text-text-heading">Desempenho (1-3)</label>
              <select 
                className="w-full p-2.5 rounded-lg border border-border-default bg-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary/20" 
                {...register('nineBoxPerformance', { valueAsNumber: true })}
              >
                <option value={1}>Abaixo do esperado</option>
                <option value={2}>Atende ao esperado</option>
                <option value={3}>Acima do esperado</option>
              </select>
            </div>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text-heading">Potencial (1-3)</label>
              <select 
                className="w-full p-2.5 rounded-lg border border-border-default bg-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary/20" 
                {...register('nineBoxPotential', { valueAsNumber: true })}
              >
                <option value={1}>Baixo</option>
                <option value={2}>Médio</option>
                <option value={3}>Alto</option>
              </select>
            </div>
          </div>

          <div className="flex justify-end gap-3 mt-6">
            <Button variant="outline" onClick={() => setRegisteringLesson(null)}>Cancelar</Button>
            <Button type="submit" isLoading={registerMutation.isPending}>Salvar Registro</Button>
          </div>
        </form>
      </Modal>

      <Modal
        isOpen={!!cancellingLesson}
        onClose={() => setCancellingLesson(null)}
        title="Cancelar Aula"
      >
        <form onSubmit={handleCancelSubmit((data) => cancelMutation.mutate({ id: cancellingLesson!.id, reason: data.reason }))} className="space-y-4">
          <p className="text-sm text-text-muted">Tem certeza que deseja cancelar esta aula? Esta ação não pode ser desfeita.</p>
          <div className="space-y-1">
            <label className="text-sm font-medium text-text-heading">Motivo do Cancelamento</label>
            <textarea
              className="w-full p-3 rounded-lg border border-border-default bg-slate-50 text-sm focus:outline-none focus:ring-2 focus:ring-brand-primary/20 min-h-[80px]"
              {...registerCancel('reason')}
              placeholder="Explique o motivo do cancelamento..."
            />
            {cancelErrors.reason && <p className="text-xs text-red-500">{cancelErrors.reason.message}</p>}
          </div>
          <div className="flex justify-end gap-3 mt-6">
            <Button variant="outline" onClick={() => setCancellingLesson(null)}>Voltar</Button>
            <Button type="submit" variant="primary" className="bg-red-600 hover:bg-red-700" isLoading={cancelMutation.isPending}>Confirmar Cancelamento</Button>
          </div>
        </form>
      </Modal>

      <ConfirmModal
        isOpen={!!confirmingId}
        onClose={() => setConfirmingId(null)}
        onConfirm={() => confirmMutation.mutate(confirmingId!)}
        title="Confirmar Aula"
        message="Você confirma que poderá realizar esta aula no horário agendado?"
        confirmText="Sim, confirmar"
        isLoading={confirmMutation.isPending}
      />
    </div>
  );
}
