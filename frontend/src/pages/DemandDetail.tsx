import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import api from '../lib/api';
import { useAuth } from '../context/AuthContext';
import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { 
  ClipboardList, 
  Calendar, 
  Users, 
  Clock, 
  Building2, 
  User as UserIcon, 
  ArrowLeft,
  Check,
  CheckCircle2,
  AlertCircle,
  Trash2,
  Info,
  MapPin
} from 'lucide-react';
import { toast } from 'react-hot-toast';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { SchoolMap } from '../components/map/SchoolMap';
import { Modal } from '../components/ui/Modal';
import { Input } from '../components/ui/Input';
import { MonthCalendar } from '../components/calendar/MonthCalendar';
import { isSameDay, startOfDay, format } from 'date-fns';
import { formatDemandStatus, formatLessonStatus } from '../utils/format';
import type { Lesson } from './Schedule';

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
  isArchived: boolean;
  pendingClassDate?: string;
  pendingRoom?: string;
  schoolName: string;
  schoolAddress: string;
  schoolLatitude?: number;
  schoolLongitude?: number;
  directorName: string;
  studentId?: string;
  studentName?: string;
  candidates: Candidate[];
  createdAt: string;
  distance?: number;
  isRecommended?: boolean;
}

const schedulingSchema = z.object({
  scheduledStart: z.string().min(1, 'Início é obrigatório').refine((val) => {
    const minDate = new Date();
    minDate.setDate(minDate.getDate() + 2);
    return new Date(val) >= minDate;
  }, 'A aula deve ser agendada com no mínimo 2 dias de antecedência'),
  scheduledEnd: z.string().min(1, 'Término é obrigatório'),
  enrolledCount: z.number().min(3, 'Mínimo de 3 alunos').max(10, 'Máximo de 10 alunos'),
}).refine((data) => new Date(data.scheduledEnd) > new Date(data.scheduledStart), {
  message: "O término deve ser após o início",
  path: ["scheduledEnd"],
});

type SchedulingFormData = z.infer<typeof schedulingSchema>;

export function DemandDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const queryClient = useQueryClient();

  const isDirector = user?.role === 'SCHOOL_DIRECTOR';
  const isStudent = user?.role === 'STUDENT';
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isFinishModalOpen, setIsFinishModalOpen] = useState(false);
  const [isScheduleModalOpen, setIsScheduleModalOpen] = useState(false);
  const [selectedDates, setSelectedDates] = useState<Date[]>([]);
  const [currentCalendarDate, setCurrentCalendarDate] = useState(new Date());
  const [startTime, setStartTime] = useState('08:00');
  const [endTime, setEndTime] = useState('13:00');
  const [pupilAmount, setPupilAmount] = useState(3);
  const pupilAmountError = (pupilAmount < 3 || pupilAmount > 10) ? 'Mínimo 3 e máximo 10 alunos' : undefined;

  const { register, handleSubmit, reset, formState: { errors } } = useForm<SchedulingFormData>({
    resolver: zodResolver(schedulingSchema),
    defaultValues: {
      enrolledCount: 3
    }
  });

  const { data: demand, isLoading, error } = useQuery<Demand>({
    queryKey: ['demand', id],
    queryFn: async () => {
      const response = await api.get(`/demands/${id}`);
      return response.data;
    },
  });

  useEffect(() => {
    if (demand?.pupilAmount) {
      setPupilAmount(demand.pupilAmount);
    }
  }, [demand?.pupilAmount]);

  const { data: lessons } = useQuery<Lesson[]>({
    queryKey: ['demand-lessons', id],
    queryFn: async () => {
      const response = await api.get(`/lessons/demand/${id}`);
      return response.data;
    },
    enabled: !!id,
  });

  const scheduleMutation = useMutation({
    mutationFn: async (data: SchedulingFormData) => {
      await api.post('/lessons', {
        ...data,
        demandId: id,
        origin: 'NEW'
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['demand-lessons', id] });
      toast.success('Aula agendada com sucesso!');
      setIsScheduleModalOpen(false);
      reset();
    },
    onError: (err: unknown) => {
      const message = axios.isAxiosError(err) 
        ? (err.response?.data?.detail || err.response?.data?.message || 'Erro ao agendar aula.') 
        : 'Erro ao agendar aula.';
      toast.error(message);
    }
  });

  const scheduleMultipleMutation = useMutation({
    mutationFn: async () => {
      if (selectedDates.length === 0) throw new Error('Selecione ao menos um dia');
      if (pupilAmount < 3 || pupilAmount > 10) throw new Error('A quantidade de alunos deve estar entre 3 e 10');
      
      const promises = selectedDates.map(date => {
        const start = new Date(date);
        const [startH, startM] = startTime.split(':').map(Number);
        start.setHours(startH, startM, 0, 0);

        const end = new Date(date);
        const [endH, endM] = endTime.split(':').map(Number);
        end.setHours(endH, endM, 0, 0);

        return api.post('/lessons', {
          demandId: id,
          scheduledStart: format(start, "yyyy-MM-dd'T'HH:mm:ss"),
          scheduledEnd: format(end, "yyyy-MM-dd'T'HH:mm:ss"),
          enrolledCount: pupilAmount,
          origin: 'NEW'
        });
      });

      await Promise.all(promises);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['demand-lessons', id] });
      toast.success(`${selectedDates.length} aula(s) agendada(s) com sucesso!`);
      setSelectedDates([]);
    },
    onError: (err: any) => {
      const message = axios.isAxiosError(err)
        ? (err.response?.data?.detail || err.response?.data?.message || err.message || 'Erro ao agendar aulas.')
        : (err.message || 'Erro ao agendar aulas.');
      toast.error(message);
    }
  });

  const toggleDate = (date: Date | null) => {
    if (!date) return;
    const dateOnly = startOfDay(date);
    setSelectedDates(prev => {
      const exists = prev.find(d => isSameDay(d, dateOnly));
      if (exists) {
        return prev.filter(d => !isSameDay(d, dateOnly));
      }
      return [...prev, dateOnly];
    });
  };

  const signMutation = useMutation({
    mutationFn: async (lessonId: string) => {
      await api.patch(`/lessons/${lessonId}/sign`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['demand-lessons', id] });
      toast.success('Aula assinada com sucesso!');
    },
    onError: (err: unknown) => {
      const message = axios.isAxiosError(err) 
        ? (err.response?.data?.detail || err.response?.data?.message || 'Erro ao assinar aula.') 
        : 'Erro ao assinar aula.';
      toast.error(message);
    }
  });

  const applyMutation = useMutation({
    mutationFn: async () => {
      await api.post(`/demands/${id}/apply`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['demand', id] });
      toast.success('Candidatura realizada com sucesso!');
    },
    onError: (err: unknown) => {
      const message = axios.isAxiosError(err) ? (err.response?.data?.message || 'Erro ao realizar candidatura.') : 'Erro ao realizar candidatura.';
      toast.error(message);
    }
  });

  const publishMutation = useMutation({
    mutationFn: async () => {
      await api.post(`/demands/${id}/publish`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['demand', id] });
      toast.success('Demanda publicada com sucesso!');
    },
    onError: (err: unknown) => {
      const message = axios.isAxiosError(err) ? (err.response?.data?.message || 'Erro ao publicar demanda.') : 'Erro ao publicar demanda.';
      toast.error(message);
    }
  });

  const approveMutation = useMutation({
    mutationFn: async (studentId: string) => {
      await api.post(`/demands/${id}/approve/${studentId}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['demand', id] });
      toast.success('Candidato aprovado com sucesso!');
    },
    onError: (err: unknown) => {
      const message = axios.isAxiosError(err) ? (err.response?.data?.message || 'Erro ao aprovar candidato.') : 'Erro ao aprovar candidato.';
      toast.error(message);
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async () => {
      await api.delete(`/demands/${id}`);
    },
    onSuccess: () => {
      toast.success('Demanda removida com sucesso!');
      navigate('/demandas');
    },
    onError: () => {
      toast.error('Erro ao remover demanda.');
    }
  });

  const finishMutation = useMutation({
    mutationFn: async () => {
      await api.post(`/demands/${id}/finish`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['demand', id] });
      toast.success('Demanda encerrada com sucesso!');
      setIsFinishModalOpen(false);
    },
    onError: (err: unknown) => {
      const message = axios.isAxiosError(err) 
        ? (err.response?.data?.message || 'Erro ao encerrar demanda.') 
        : 'Erro ao encerrar demanda.';
      toast.error(message);
    }
  });
  
  const confirmChangeMutation = useMutation({
    mutationFn: async () => {
      await api.post(`/demands/${id}/confirm`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['demand', id] });
      toast.success('Alterações confirmadas com sucesso!');
    },
    onError: (err: unknown) => {
      const message = axios.isAxiosError(err) ? (err.response?.data?.message || 'Erro ao confirmar alterações.') : 'Erro ao confirmar alterações.';
      toast.error(message);
    }
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'WAITING': return 'bg-blue-100 text-blue-700 border-blue-200';
      case 'ONGOING': return 'bg-amber-100 text-amber-700 border-amber-200';
      case 'FINISHED': return 'bg-emerald-100 text-emerald-700 border-emerald-200';
      case 'ARCHIVED': return 'bg-gray-100 text-gray-700 border-gray-200';
      default: return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  };

  const formatDate = (dateString: string) => {
    try {
      return new Intl.DateTimeFormat('pt-BR', {
        day: '2-digit',
        month: 'long',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      }).format(new Date(dateString));
    } catch (_) {
      return dateString;
    }
  };

  if (isLoading) {
    return (
      <div className="flex-1 flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-brand-primary"></div>
      </div>
    );
  }

  if (error || !demand) {
    return (
      <div className="flex-1 p-8">
        <Card className="p-8 text-center max-w-md mx-auto">
          <AlertCircle size={48} className="mx-auto text-red-500 mb-4" />
          <h2 className="text-2xl font-bold text-text-heading mb-4">Demanda não encontrada</h2>
          <p className="text-text-body mb-6">Não foi possível carregar os detalhes desta demanda.</p>
          <Button onClick={() => navigate('/demandas')}>Voltar para Lista</Button>
        </Card>
      </div>
    );
  }

  return (
    <div className="flex-1 overflow-y-auto">
      <Header 
        title="Detalhes da Demanda" 
        subtitle="Visualize todas as informações e gerencie candidaturas."
      />

      <div className="p-8 max-w-[1600px] mx-auto">
        <button 
          onClick={() => navigate('/demandas')}
          className="flex items-center gap-2 text-text-muted hover:text-brand-primary transition-colors mb-6 group"
        >
          <ArrowLeft size={18} className="group-hover:-translate-x-1 transition-transform" />
          <span className="font-medium">Voltar para a lista</span>
        </button>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          <div className="lg:col-span-3 space-y-6">
            {isStudent && (
              <div className="grid grid-cols-1 md:grid-cols-5 gap-6">
                <div className="md:col-span-2">
                  <Card className="p-4 h-full">
                    <div className="flex items-center gap-2 mb-3 text-text-heading">
                      <MapPin size={18} className="text-brand-primary" />
                      <h3 className="font-bold text-sm">Localização</h3>
                    </div>
                    <div className="rounded-xl overflow-hidden border border-border-default">
                      <SchoolMap 
                        address={demand.schoolAddress} 
                        schoolName={demand.schoolName}
                        latitude={demand.schoolLatitude}
                        longitude={demand.schoolLongitude}
                      />
                    </div>
                    <div className="flex items-center text-sm gap-2 mt-4">
                      <MapPin size={16} className="text-text-muted shrink-0" />
                      <p className="text-text-muted">
                        {demand.schoolAddress}
                      </p>
                    </div>
                    {demand.distance !== undefined && demand.distance !== null && (
                      <div className="flex items-center text-sm gap-2 mt-2">
                        <MapPin size={16} className="text-brand-primary shrink-0" />
                        <p className="text-brand-primary font-medium">
                          Aproximadamente {demand.distance.toFixed(1)} km de você.
                        </p>
                      </div>
                    )}

                  </Card>
                </div>
                <div className="md:col-span-3">
                  <Card className="p-8 h-full">
                    <div className="flex items-start justify-between mb-6 flex-wrap gap-4">
                      <div className="flex items-center gap-4">
                        <div className="w-14 h-14 rounded-xl bg-brand-primary/10 flex items-center justify-center text-brand-primary">
                          <ClipboardList size={28} />
                        </div>
                        <div>
                          <h1 className="text-2xl font-bold text-text-heading">{demand.title}</h1>
                          <p className="text-text-muted">{demand.subject} • {demand.gradeLevel}</p>
                        </div>
                      </div>
                      <div className="flex items-center gap-3 flex-wrap">
                        <div className="flex gap-2 flex-wrap">
                          <span className={`px-3 py-1 rounded-full text-xs font-bold border ${getStatusColor(demand.status)}`}>
                            {formatDemandStatus(demand.status)}
                          </span>
                          {isStudent && demand.status === 'WAITING' && (
                            demand.isRecommended ? (
                              <span className="px-3 py-1 rounded-full text-xs font-bold bg-brand-primary/10 text-brand-primary border border-brand-primary/20">
                                Recomendado
                              </span>
                            ) : (
                              <span className="px-3 py-1 rounded-full text-xs font-bold bg-amber-100 text-amber-700 border border-amber-200">
                                Não Recomendado
                              </span>
                            )
                          )}
                        </div>
                      </div>
                    </div>

                    <div className="space-y-6">
                      <div>
                        <h3 className="text-sm font-bold text-text-muted uppercase tracking-wider mb-3">Descrição</h3>
                        <p className="text-text-body leading-relaxed whitespace-pre-wrap">
                          {demand.description}
                        </p>
                      </div>

                      <div className="grid grid-cols-2 sm:grid-cols-4 gap-6 pt-6 border-t border-border-default">
                        <div className="space-y-1">
                          <p className="text-xs text-text-muted font-medium">Data Prevista</p>
                          <div className="flex items-center gap-2 text-sm text-text-heading font-semibold">
                            <Calendar size={16} className="text-brand-primary" />
                            {formatDate(demand.classDate)}
                          </div>
                        </div>
                        <div className="space-y-1">
                          <p className="text-xs text-text-muted font-medium">Carga Horária</p>
                          <div className="flex items-center gap-2 text-sm text-text-heading font-semibold">
                            <Clock size={16} className="text-brand-primary" />
                            {demand.totalHours}
                          </div>
                        </div>
                        <div className="space-y-1">
                          <p className="text-xs text-text-muted font-medium">Nº de Alunos</p>
                          <div className="flex items-center gap-2 text-sm text-text-heading font-semibold">
                            <Users size={16} className="text-brand-primary" />
                            {demand.pupilAmount}
                          </div>
                        </div>
                        <div className="space-y-1">
                          <p className="text-xs text-text-muted font-medium">Sala</p>
                          <div className="flex items-center gap-2 text-sm text-text-heading font-semibold">
                            <Building2 size={16} className="text-brand-primary" />
                            {demand.room}
                          </div>
                        </div>
                      </div>
                    </div>
                  </Card>
                </div>
              </div>
            )}

            {!isStudent && (
              <Card className="p-8">
                <div className="flex items-start justify-between mb-6 flex-wrap gap-4">
                  <div className="flex items-center gap-4">
                    <div className="w-14 h-14 rounded-xl bg-brand-primary/10 flex items-center justify-center text-brand-primary">
                      <ClipboardList size={28} />
                    </div>
                    <div>
                      <h1 className="text-2xl font-bold text-text-heading">{demand.title}</h1>
                      <p className="text-text-muted">{demand.subject} • {demand.gradeLevel}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-3 flex-wrap">
                    {isDirector && demand.status === 'DRAFT' && !demand.isArchived && (
                      <Button
                        variant="primary"
                        size="sm"
                        className="bg-brand-primary text-white gap-2"
                        onClick={() => publishMutation.mutate()}
                        disabled={publishMutation.isPending}
                        isLoading={publishMutation.isPending}
                      >
                        <CheckCircle2 size={18} />
                        Publicar Demanda
                      </Button>
                    )}
                    {isDirector && demand.status === 'WAITING' && !demand.isArchived && (
                      <button 
                        onClick={() => setIsDeleteModalOpen(true)}
                        className="p-2 text-text-muted hover:text-red-500 hover:bg-red-50 rounded-lg transition-colors"
                        title="Excluir Demanda"
                      >
                        <Trash2 size={20} />
                      </button>
                    )}
                    {isDirector && demand.status === 'ONGOING' && (
                      <Button
                        variant="outline"
                        size="sm"
                        className="text-emerald-600 border-emerald-200 hover:bg-emerald-50 gap-2"
                        onClick={() => setIsFinishModalOpen(true)}
                      >
                        <CheckCircle2 size={18} />
                        Encerrar Demanda
                      </Button>
                    )}
                    <div className="flex gap-2 flex-wrap">
                      <span className={`px-3 py-1 rounded-full text-xs font-bold border ${getStatusColor(demand.status)}`}>
                        {formatDemandStatus(demand.status)}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="space-y-6">
                  <div>
                    <h3 className="text-sm font-bold text-text-muted uppercase tracking-wider mb-3">Descrição</h3>
                    <p className="text-text-body leading-relaxed whitespace-pre-wrap">
                      {demand.description}
                    </p>
                  </div>

                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-6 pt-6 border-t border-border-default">
                    <div className="space-y-1">
                      <p className="text-xs text-text-muted font-medium">Carga Horária Prevista</p>
                      <div className="flex items-center gap-2 text-sm text-text-heading font-semibold">
                        <Clock size={16} className="text-brand-primary" />
                        {demand.totalHours}
                      </div>
                    </div>
                    <div className="space-y-1">
                      <p className="text-xs text-text-muted font-medium">Nº de Alunos</p>
                      <div className="flex items-center gap-2 text-sm text-text-heading font-semibold">
                        <Users size={16} className="text-brand-primary" />
                        {demand.pupilAmount}
                      </div>
                    </div>
                    <div className="space-y-1">
                      <p className="text-xs text-text-muted font-medium">Sala</p>
                      <div className="flex items-center gap-2 text-sm text-text-heading font-semibold">
                        <Building2 size={16} className="text-brand-primary" />
                        {demand.room}
                      </div>
                    </div>
                    <div className="space-y-1">
                      <p className="text-xs text-text-muted font-medium">Dificuldade</p>
                      <div className="flex items-center gap-2 text-sm text-text-heading font-semibold">
                        <AlertCircle size={16} className="text-brand-primary" />
                        {demand.difficultyLevel}
                      </div>
                    </div>
                    <div className="space-y-1">
                      <p className="text-xs text-text-muted font-medium">Status</p>
                      <div className="flex items-center gap-2 text-sm text-text-heading font-semibold">
                        <CheckCircle2 size={16} className="text-brand-primary" />
                        {formatDemandStatus(demand.status)}
                      </div>
                    </div>
                  </div>
                </div>
              </Card>
            )}

            {isDirector && demand.status === 'WAITING' && !demand.isArchived && (
              <Card className="p-8">
                <div className="flex items-center justify-between mb-6">
                  <div>
                    <h3 className="text-lg font-bold text-text-heading">Candidatos Inscritos</h3>
                    <p className="text-sm text-text-body">Analise e aprove um estudante para esta demanda.</p>
                  </div>
                  <span className="bg-brand-primary/10 text-brand-primary px-3 py-1 rounded-full text-xs font-bold">
                    {demand.candidates.length} candidato(s)
                  </span>
                </div>

                {demand.candidates.length > 0 ? (
                  <div className="space-y-3">
                    {demand.candidates.map((candidate) => (
                      <div key={candidate.id} className="flex items-center justify-between p-4 bg-slate-50 rounded-xl border border-border-default hover:border-brand-primary transition-colors">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-white flex items-center justify-center text-brand-primary border border-border-default">
                            <UserIcon size={20} />
                          </div>
                          <div>
                            <p className="font-bold text-text-heading">{candidate.name}</p>
                            <p className="text-xs text-text-muted">Estudante de Graduação</p>
                          </div>
                        </div>
                        <Button
                          onClick={() => approveMutation.mutate(candidate.id)}
                          disabled={approveMutation.isPending}
                        >
                          Aprovar Candidato
                        </Button>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 bg-slate-50 rounded-xl border border-dashed border-border-default">
                    <p className="text-text-muted text-sm">Nenhum candidato inscrito até o momento.</p>
                  </div>
                )}
              </Card>
            )}

            {(demand.studentName || isDirector || (lessons && lessons.length > 0)) && (
              <Card className={`p-8 border-brand-primary/20 ${demand.studentId === user?.id ? 'bg-emerald-50 border-emerald-200' : 'bg-brand-primary/[0.02]'}`}>
                {demand.studentName ? (
                  <>
                    <h3 className="text-lg font-bold text-text-heading mb-4">
                      {demand.studentId === user?.id ? 'Seu Estágio' : 'Estudante Alocado'}
                    </h3>
                    <div className="flex items-center gap-4 p-4 bg-white rounded-xl border border-brand-primary/10">
                      <div className="w-12 h-12 rounded-full bg-brand-primary/10 flex items-center justify-center text-brand-primary">
                        <UserIcon size={24} />
                      </div>
                      <div>
                        <p className="font-bold text-text-heading text-lg">{demand.studentName}</p>
                        <p className="text-sm text-text-muted">
                          {demand.studentId === user?.id 
                            ? 'Você é o responsável pela execução desta demanda' 
                            : 'Responsável pela execução desta demanda'}
                        </p>
                      </div>
                    </div>
                  </>
                ) : (
                  <h3 className="text-lg font-bold text-text-heading mb-4">
                    Agenda da Demanda
                  </h3>
                )}

                {/* Agenda da Demanda para Diretor */}
                {isDirector && !demand.isArchived && (demand.status === 'DRAFT' || demand.status === 'WAITING' || demand.status === 'ONGOING') && (
                  <div className="mt-8 space-y-6">
                    <h3 className="text-lg font-bold text-text-heading flex items-center gap-2">
                      <Calendar size={20} className="text-brand-primary" />
                      Definir Agenda de Reforço
                    </h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                      <Card className="p-6">
                        <MonthCalendar 
                          currentDate={currentCalendarDate}
                          onDateChange={setCurrentCalendarDate}
                          eventDates={selectedDates}
                          onSelectDate={toggleDate}
                        />
                        <div className="mt-4 flex flex-wrap gap-1">
                          {selectedDates.sort((a,b) => a.getTime() - b.getTime()).map(date => (
                            <span key={date.toISOString()} className="px-2 py-1 bg-brand-primary/10 text-brand-primary rounded text-[10px] font-bold">
                              {date.toLocaleDateString('pt-BR', {day: '2-digit', month: '2-digit'})}
                            </span>
                          ))}
                        </div>
                      </Card>
                      <Card className="p-6 space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                          <Input 
                            label="Horário Início"
                            type="time"
                            value={startTime}
                            onChange={e => setStartTime(e.target.value)}
                          />
                          <Input 
                            label="Horário Término"
                            type="time"
                            value={endTime}
                            onChange={e => setEndTime(e.target.value)}
                          />
                        </div>
                          <Input
                            label="Quantidade de Alunos"
                            type="number"
                            min={3}
                            max={10}
                            value={pupilAmount}
                            onChange={e => setPupilAmount(Number(e.target.value))}
                            error={pupilAmountError}
                          />
                        <p className="text-xs text-text-muted italic">
                          O horário selecionado será aplicado a todos os dias marcados no calendário.
                        </p>
                        <Button 
                          className="w-full mt-4"
                          disabled={selectedDates.length === 0 || !!pupilAmountError || scheduleMultipleMutation.isPending}
                          onClick={() => scheduleMultipleMutation.mutate()}
                          isLoading={scheduleMultipleMutation.isPending}
                        >
                          Gerar {selectedDates.length} Aula(s) na Agenda
                        </Button>
                      </Card>
                    </div>
                  </div>
                )}

                {/* Listagem de Aulas da Demanda */}
                {lessons && lessons.length > 0 && (
                  <div className="mt-8 space-y-4">
                    <h4 className="font-bold text-text-heading flex items-center gap-2">
                      <Calendar size={18} className="text-brand-primary" />
                      Aulas Agendadas
                    </h4>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {lessons.map((lesson) => (
                        <div key={lesson.id} className="p-4 bg-white rounded-xl border border-border-default hover:border-brand-primary/50 transition-colors">
                          <div className="flex justify-between items-start mb-2">
                            <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${
                              lesson.status === 'SCHEDULED' ? 'bg-blue-100 text-blue-700' :
                              lesson.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' :
                              (lesson.status === 'REGISTERED' || lesson.status === 'COMPLETED') ? 'bg-purple-100 text-purple-700' :
                              'bg-emerald-100 text-emerald-700'
                            }`}>
                              {formatLessonStatus(lesson.status)}
                            </span>
                            <span className="text-xs text-text-muted">
                              {new Date(lesson.scheduledStart).toLocaleDateString()}
                            </span>
                          </div>
                          <div className="flex items-center gap-2 text-sm font-bold text-text-heading mb-1">
                            <Clock size={14} className="text-brand-primary" />
                            {new Date(lesson.scheduledStart).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} - 
                            {new Date(lesson.scheduledEnd).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                          </div>
                          <p className="text-xs text-text-muted mb-3">{lesson.enrolledCount} alunos previstos</p>
                          
                          {isDirector && (lesson.status === 'REGISTERED' || lesson.status === 'COMPLETED') && (
                            <Button 
                              size="sm" 
                              className="w-full text-xs h-8"
                              onClick={() => signMutation.mutate(lesson.id)}
                              isLoading={signMutation.isPending}
                            >
                              Validar e Assinar
                            </Button>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
                {demand.studentId === user?.id && (
                  <div className="mt-6 p-4 bg-emerald-100/50 rounded-lg flex gap-3 text-emerald-800 text-sm">
                    <Info size={20} className="shrink-0" />
                    <p>
                      Parabéns! Você foi selecionado para este estágio.
                    </p>
                  </div>
                )}
                
                {demand.studentId === user?.id && !demand.isArchived && (demand.pendingClassDate || demand.pendingRoom) && (
                  <div className="mt-6 p-6 bg-amber-50 border border-amber-200 rounded-xl space-y-4">
                    <div className="flex gap-3 text-amber-800">
                      <AlertCircle size={24} className="shrink-0" />
                      <div>
                        <h4 className="font-bold">Alterações Pendentes</h4>
                        <p className="text-sm">O diretor da escola solicitou alterações nesta demanda que precisam da sua confirmação:</p>
                        <ul className="mt-2 text-sm list-disc list-inside space-y-1">
                          {demand.pendingClassDate && (
                            <li>Novo Horário: <span className="font-bold">{formatDate(demand.pendingClassDate)}</span></li>
                          )}
                          {demand.pendingRoom && (
                            <li>Nova Sala: <span className="font-bold">{demand.pendingRoom}</span></li>
                          )}
                        </ul>
                      </div>
                    </div>
                    <Button 
                      className="w-full bg-amber-600 hover:bg-amber-700 text-white"
                      onClick={() => confirmChangeMutation.mutate()}
                      disabled={confirmChangeMutation.isPending}
                    >
                      {confirmChangeMutation.isPending ? 'Confirmando...' : 'Confirmar Alterações'}
                    </Button>
                  </div>
                )}
              </Card>
            )}
          </div>

          <div className="space-y-6">
            <Card className="p-6">
              <h3 className="font-bold text-text-heading mb-4">
                {isDirector ? 'Informações da Sua Escola' : 'Informações da Escola'}
              </h3>
              <div className="space-y-4">
                <div className="flex items-start gap-3">
                  <Building2 size={20} className="text-brand-primary shrink-0" />
                  <div>
                    <p className="text-sm font-bold text-text-heading">{demand.schoolName}</p>
                    <p className="text-xs text-text-muted">
                      {isDirector ? 'Sua instituição' : 'Instituição solicitante'}
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <UserIcon size={20} className="text-brand-primary shrink-0" />
                  <div>
                    <p className="text-sm font-bold text-text-heading">{demand.directorName}</p>
                    <p className="text-xs text-text-muted">
                      {isDirector ? 'Você' : 'Diretor(a) responsável'}
                    </p>
                  </div>
                </div>
                {!isStudent && (
                  <div className="flex items-start gap-3 pt-2 border-t border-border-default">
                    <MapPin size={20} className="text-brand-primary shrink-0" />
                    <div>
                      <p className="text-xs font-bold text-text-heading">Localização</p>
                      <p className="text-[10px] text-text-muted leading-tight">{demand.schoolAddress || 'Endereço não informado'}</p>
                    </div>
                  </div>
                )}
              </div>
            </Card>

            {!isStudent && (
              <Card className="p-6">
                <div className="flex items-center gap-2 mb-4 text-text-heading">
                  <MapPin size={20} className="text-brand-primary" />
                  <h3 className="font-bold">Localização da Escola</h3>
                </div>
                <p className="text-xs text-text-muted mb-4">{demand.schoolAddress || 'Endereço não informado'}</p>
                <SchoolMap 
                  address={demand.schoolAddress} 
                  schoolName={demand.schoolName}
                  latitude={demand.schoolLatitude}
                  longitude={demand.schoolLongitude}
                />
              </Card>
            )}

            {isStudent && demand.status === 'WAITING' && !demand.isArchived && (
              <Card className="p-6 bg-brand-primary text-white">
                <h3 className="font-bold mb-2">Interessado nesta vaga?</h3>
                <p className="text-sm opacity-90 mb-6">
                  {demand.candidates.some(c => c.id === user?.id)
                    ? 'Sua candidatura está em análise pelo diretor da escola. Fique atento às atualizações.'
                    : 'Candidate-se agora para que o diretor da escola possa avaliar seu perfil.'}
                </p>
                <button 
                  onClick={() => applyMutation.mutate()}
                  disabled={applyMutation.isPending || demand.candidates.some(c => c.id === user?.id)}
                  className="w-full bg-white text-brand-primary py-3 rounded-xl font-bold hover:bg-brand-secondary transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
                >
                  {demand.candidates.some(c => c.id === user?.id) ? (
                    <>
                      <Check size={20} /> Candidatura Enviada
                    </>
                  ) : (
                    applyMutation.isPending ? 'Candidatando...' : 'Candidatar-se'
                  )}
                </button>
              </Card>
            )}

            {isStudent && demand.status === 'ONGOING' && demand.studentId !== user?.id && (
              <Card className="p-6 bg-slate-100 border-slate-200">
                <div className="flex gap-3 text-slate-600">
                  <Info size={20} className="shrink-0" />
                  <div>
                    <h3 className="font-bold text-sm mb-1">Vaga Preenchida</h3>
                    <p className="text-xs">
                      Esta demanda já possui um estagiário alocado e está em andamento.
                    </p>
                  </div>
                </div>
              </Card>
            )}
          </div>
        </div>
      </div>

      <ConfirmModal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        onConfirm={() => deleteMutation.mutate()}
        title="Remover Demanda"
        description="Tem certeza que deseja remover esta demanda? Esta ação não pode ser desfeita e todos os candidatos serão notificados."
        isLoading={deleteMutation.isPending}
      />

      <ConfirmModal
        isOpen={isFinishModalOpen}
        onClose={() => setIsFinishModalOpen(false)}
        onConfirm={() => finishMutation.mutate()}
        title="Encerrar Demanda"
        description="Tem certeza que deseja encerrar esta demanda? Isso marcará o projeto como concluído e não permitirá novos agendamentos."
        isLoading={finishMutation.isPending}
        confirmText="Sim, Encerrar"
        variant="info"
      />

      <Modal
        isOpen={isScheduleModalOpen}
        onClose={() => setIsScheduleModalOpen(false)}
        title="Agendar Nova Aula"
      >
        <form onSubmit={handleSubmit((data) => scheduleMutation.mutate(data))} className="space-y-4">
          <p className="text-sm text-text-muted mb-4">
            Defina o horário da aula. Lembre-se que deve ser com no mínimo 2 dias de antecedência.
          </p>
          
          <Input
            label="Início da Aula"
            type="datetime-local"
            {...register('scheduledStart')}
            error={errors.scheduledStart?.message}
          />

          <Input
            label="Término da Aula"
            type="datetime-local"
            {...register('scheduledEnd')}
            error={errors.scheduledEnd?.message}
          />

          <Input
            label="Quantidade de Alunos"
            type="number"
            {...register('enrolledCount', { valueAsNumber: true })}
            error={errors.enrolledCount?.message}
          />

          <div className="flex justify-end gap-3 mt-6">
            <Button variant="outline" onClick={() => setIsScheduleModalOpen(false)}>Cancelar</Button>
            <Button type="submit" isLoading={scheduleMutation.isPending}>Confirmar Agendamento</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
