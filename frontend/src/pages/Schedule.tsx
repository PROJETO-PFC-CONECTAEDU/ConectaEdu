import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState, useMemo } from 'react';
import axios from 'axios';
import api from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { toast } from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Input } from '../components/ui/Input';
import { MonthCalendar } from '../components/calendar/MonthCalendar';
import { SessionRow } from '../components/schedule/SessionRow';
import { parseISO, isSameDay, format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import { clsx } from 'clsx';
import { formatLessonStatus } from '../utils/format';

export interface Lesson {
  id: string;
  demandId: string;
  schoolName: string;
  subject: string;
  scheduledStart: string;
  scheduledEnd: string;
  status: 'SCHEDULED' | 'CONFIRMED' | 'CANCELED' | 'COMPLETED' | 'REGISTERED' | 'SIGNED' | 'VALIDATED';
  enrolledCount: number;
  presentCount?: number;
  confirmedAt?: string;
  registeredAt?: string;
  signedAt?: string;
  contentTaught?: string;
  nineBoxPerformance?: number;
  nineBoxPotential?: number;
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
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [currentDate, setCurrentDate] = useState(new Date(2026, 9, 22));
  const [selectedDay, setSelectedDay] = useState<Date | null>(null);

  const [registeringLesson, setRegisteringLesson] = useState<Lesson | null>(null);
  const [viewingLesson, setViewingLesson] = useState<Lesson | null>(null);
  const [cancellingLesson, setCancellingLesson] = useState<Lesson | null>(null);
  const [confirmingId, setConfirmingId] = useState<string | null>(null);

  const isStudent = user?.role === 'STUDENT';

  const { data: lessons, isLoading } = useQuery<Lesson[]>({
    queryKey: ['lessons', user?.id, user?.role],
    queryFn: async () => {
      const endpoint = user?.role === 'SCHOOL_DIRECTOR' 
        ? `/lessons/school/${user?.schoolId}` 
        : `/lessons/student/${user?.id}`;
      const response = await api.get(endpoint);
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

  const eventDates = useMemo(() => {
    if (!lessons) return [];
    return lessons.map(l => parseISO(l.scheduledStart));
  }, [lessons]);

  const filteredLessons = useMemo(() => {
    if (!lessons) return [];
    return lessons.filter(lesson => {
      const lessonDate = parseISO(lesson.scheduledStart);
      const matchesStatus = filterStatus === 'ALL' || lesson.status === filterStatus;
      const matchesDate = !selectedDay || isSameDay(lessonDate, selectedDay);
      return matchesStatus && matchesDate;
    }).sort((a, b) => new Date(a.scheduledStart).getTime() - new Date(b.scheduledStart).getTime());
  }, [lessons, filterStatus, selectedDay]);

  const tabs = [
    { id: 'ALL', label: 'Todas' },
    { id: 'CONFIRMED', label: 'Confirmadas' },
    { id: 'SCHEDULED', label: 'Pendentes' },
    { id: 'CANCELED', label: 'Canceladas' },
  ];

  return (
    <div className="flex-1">
      <Header 
        title="Minha Agenda" 
        subtitle="Organize e visualize seus atendimentos e compromissos pedagógicos."
      />

      <div className="p-4 md:p-10">
        <div className="flex flex-col md:flex-row gap-8 md:gap-[32px] items-start">
          {/* Calendar Card */}
          <Card className="w-full md:w-[415px] p-4 md:p-6 rounded-xl border-border-default shadow-none bg-surface">
            <MonthCalendar 
              currentDate={currentDate} 
              onDateChange={setCurrentDate}
              eventDates={eventDates}
              selectedDate={selectedDay}
              onSelectDate={setSelectedDay}
            />
          </Card>

          {/* Schedule Card */}
          <div className="flex-1 w-full">
            <Card className="w-full min-h-[428px] rounded-xl border-border-default shadow-none flex flex-col bg-surface">
              {/* Tabs - Desktop Only */}
              <div className="hidden md:flex px-6 pt-6 border-b border-border-default gap-2">
                {tabs.map(tab => (
                  <button
                    key={tab.id}
                    onClick={() => setFilterStatus(tab.id)}
                    className={clsx(
                      "px-4 py-2 text-[13px] font-sans font-semibold transition-colors mb-[-1px]",
                      filterStatus === tab.id 
                        ? "bg-brand-primary text-white rounded-pill mb-2" 
                        : "text-text-body hover:text-text-heading"
                    )}
                  >
                    {tab.label}
                  </button>
                ))}
              </div>

              <div className="p-4 md:p-6">
                <h3 className="md:hidden text-text-heading font-display font-bold text-[16px] mb-4">
                  {selectedDay 
                    ? `Aulas de ${format(selectedDay, "dd 'de' MMM", { locale: ptBR })}`
                    : `Aulas do Dia - ${format(currentDate, "dd 'Out'", { locale: ptBR })}`
                  }
                </h3>

                <div className="space-y-3">
                  {isLoading ? (
                    <p className="text-center py-10 text-text-body">Carregando...</p>
                  ) : filteredLessons.length > 0 ? (
                    filteredLessons.map(lesson => (
                      <div key={lesson.id} className="relative group">
                        <SessionRow 
                          session={lesson as any} 
                          onClick={() => {
                            if (isStudent) {
                                if (lesson.status === 'SCHEDULED') {
                                    setConfirmingId(lesson.id);
                                } else if (lesson.status === 'CONFIRMED') {
                                    setRegisteringLesson(lesson);
                                } else if (['COMPLETED', 'SIGNED', 'VALIDATED'].includes(lesson.status)) {
                                    setViewingLesson(lesson);
                                }
                            } else if (user?.role === 'SCHOOL_DIRECTOR') {
                                if (['COMPLETED', 'SIGNED', 'VALIDATED'].includes(lesson.status)) {
                                    setViewingLesson(lesson);
                                }
                            }
                          }}
                        />
                        {/* Ações rápidas no hover */}
                        {['SCHEDULED', 'CONFIRMED'].includes(lesson.status) && (
                            <button 
                                onClick={(e) => {
                                    e.stopPropagation();
                                    setCancellingLesson(lesson);
                                }}
                                className="hidden group-hover:flex absolute top-1 right-2 bg-white shadow-sm border border-red-100 text-red-500 p-1 rounded-md text-[10px] hover:bg-red-50"
                            >
                                Cancelar
                            </button>
                        )}
                      </div>
                    ))
                  ) : (
                    <div className="flex flex-col items-center justify-center py-20 text-text-body">
                      <p>Nenhum compromisso encontrado</p>
                    </div>
                  )}
                </div>
              </div>
            </Card>
          </div>
        </div>
      </div>

      {/* Modais Originais */}
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

          <div className="space-y-2 pt-2 border-t border-border-default">
            <p className="text-xs font-semibold text-text-muted uppercase tracking-wider">Avaliação da Turma (Impacto Nine Box)</p>
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

      <Modal
        isOpen={!!viewingLesson}
        onClose={() => setViewingLesson(null)}
        title="Detalhes da Aula"
      >
        {viewingLesson && (
          <div className="space-y-6">
            <div className="grid grid-cols-2 gap-4">
              <div className="bg-slate-50 p-4 rounded-lg border border-border-default">
                <p className="text-xs text-text-muted mb-1 font-medium uppercase tracking-wider">Status</p>
                <p className="text-sm font-bold text-text-heading">{formatLessonStatus(viewingLesson.status)}</p>
              </div>
              <div className="bg-slate-50 p-4 rounded-lg border border-border-default">
                <p className="text-xs text-text-muted mb-1 font-medium uppercase tracking-wider">Presença</p>
                <p className="text-sm font-bold text-text-heading">{viewingLesson.presentCount} de {viewingLesson.enrolledCount} alunos</p>
              </div>
            </div>

            <div className="bg-slate-50 p-4 rounded-lg border border-border-default">
              <p className="text-xs text-text-muted mb-2 font-medium uppercase tracking-wider">Conteúdo Lecionado</p>
              <p className="text-sm text-text-body leading-relaxed whitespace-pre-wrap italic">
                "{viewingLesson.contentTaught || 'Não informado'}"
              </p>
            </div>

            <div className="space-y-3">
              <p className="text-xs text-text-muted font-medium uppercase tracking-wider px-1">Avaliação da Turma (Nine Box)</p>
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-brand-primary/5 p-4 rounded-lg border border-brand-primary/10">
                  <p className="text-[10px] text-brand-primary font-bold uppercase mb-1">Desempenho</p>
                  <p className="text-sm font-semibold text-text-heading">
                    {viewingLesson.nineBoxPerformance === 1 ? 'Abaixo do esperado' : 
                     viewingLesson.nineBoxPerformance === 2 ? 'Atende ao esperado' : 
                     viewingLesson.nineBoxPerformance === 3 ? 'Acima do esperado' : 'Não informado'}
                  </p>
                </div>
                <div className="bg-brand-primary/5 p-4 rounded-lg border border-brand-primary/10">
                  <p className="text-[10px] text-brand-primary font-bold uppercase mb-1">Potencial</p>
                  <p className="text-sm font-semibold text-text-heading">
                    {viewingLesson.nineBoxPotential === 1 ? 'Baixo' : 
                     viewingLesson.nineBoxPotential === 2 ? 'Médio' : 
                     viewingLesson.nineBoxPotential === 3 ? 'Alto' : 'Não informado'}
                  </p>
                </div>
              </div>
            </div>

            <div className="flex justify-end pt-2">
              <Button onClick={() => setViewingLesson(null)}>Fechar</Button>
            </div>
          </div>
        )}
      </Modal>

      <ConfirmModal
        isOpen={!!confirmingId}
        onClose={() => setConfirmingId(null)}
        onConfirm={() => confirmMutation.mutate(confirmingId!)}
        title="Confirmar Aula"
        description="Você confirma que poderá realizar esta aula no horário agendado?"
        confirmText="Sim, confirmar"
        isLoading={confirmMutation.isPending}
      />
    </div>
  );
}
