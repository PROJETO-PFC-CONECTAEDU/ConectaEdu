import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import api from '../lib/api';
import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { Users, Mail, GraduationCap, Trash2, ShieldCheck, XCircle, Plus } from 'lucide-react';
import { toast } from 'react-hot-toast';

interface Student {
  id: string;
  name: string;
  email: string;
  universityName: string;
  status: 'PENDING' | 'VALIDATED' | 'REJECTED';
  availability: string;
  interestAreas: string[];
}

interface University {
  id: string;
  name: string;
}

const studentSchema = z.object({
  name: z.string().min(3, 'Nome deve ter pelo menos 3 caracteres'),
  email: z.string().email('Email inválido'),
  password: z.string().min(6, 'Senha deve ter pelo menos 6 caracteres'),
  universityId: z.string().uuid('Selecione uma universidade'),
  availability: z.string().min(1, 'Disponibilidade é obrigatória'),
  interestAreas: z.string().min(1, 'Áreas de interesse são obrigatórias'),
});

type StudentFormData = z.infer<typeof studentSchema>;

export function Students() {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<StudentFormData>({
    resolver: zodResolver(studentSchema)
  });

  const { data: students, isLoading, error } = useQuery<Student[]>({
    queryKey: ['students'],
    queryFn: async () => {
      const response = await api.get('/students');
      return response.data;
    },
  });

  const { data: universities } = useQuery<University[]>({
    queryKey: ['universities'],
    queryFn: async () => {
      const response = await api.get('/universities');
      return response.data;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/students/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['students'] });
      toast.success('Estudante removido com sucesso!');
      setDeletingId(null);
    },
    onError: () => {
      toast.error('Erro ao remover estudante.');
    }
  });

  const createMutation = useMutation({
    mutationFn: async (data: any) => {
      await api.post('/students', data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['students'] });
      toast.success('Estudante cadastrado com sucesso!');
      setIsModalOpen(false);
      reset();
    },
    onError: (error: any) => {
      const message = error.response?.data?.message || 'Erro ao cadastrar estudante.';
      toast.error(message);
    }
  });

  const onSubmit = (data: StudentFormData) => {
    const formattedData = {
      ...data,
      interestAreas: data.interestAreas.split(',').map(s => s.trim()).filter(s => s !== '')
    };
    createMutation.mutate(formattedData);
  };

  const validateMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.patch(`/students/${id}/validate`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['students'] });
      toast.success('Estudante validado!');
    }
  });

  const rejectMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.patch(`/students/${id}/reject`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['students'] });
      toast.success('Estudante rejeitado!');
    }
  });

  if (isLoading) return <div className="p-8">Carregando estudantes...</div>;
  if (error) return <div className="p-8 text-red-500">Erro ao carregar estudantes.</div>;

  return (
    <div className="flex-1">
      <Header 
        title="Estudantes" 
        subtitle="Gerencie os estudantes cadastrados na plataforma." 
        action={
          <button 
            onClick={() => setIsModalOpen(true)}
            className="bg-brand-primary text-white px-4 py-2 rounded-lg flex items-center gap-2 font-bold hover:bg-brand-primary/90 transition-colors"
          >
            <Plus size={20} /> Novo Estudante
          </button>
        }
      />
      
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Novo Estudante"
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Nome Completo"
            placeholder="Ex: João Silva"
            error={errors.name?.message}
            {...register('name')}
          />
          <Input
            label="Email"
            type="email"
            placeholder="joao@exemplo.com"
            error={errors.email?.message}
            {...register('email')}
          />
          <Input
            label="Senha"
            type="password"
            placeholder="Mínimo 6 caracteres"
            error={errors.password?.message}
            {...register('password')}
          />
          
          <div className="w-full space-y-1.5">
            <label className="text-sm font-medium text-text-heading">
              Universidade
            </label>
            <select
              {...register('universityId')}
              className={`flex h-11 w-full rounded-lg border bg-white px-3 py-2 text-sm text-text-heading focus:outline-none focus:ring-2 focus:ring-brand-primary/20 ${
                errors.universityId ? 'border-red-500 focus:ring-red-500/20' : 'border-border-default'
              }`}
            >
              <option value="">Selecione uma universidade</option>
              {universities?.map(uni => (
                <option key={uni.id} value={uni.id}>{uni.name}</option>
              ))}
            </select>
            {errors.universityId && (
              <p className="text-xs text-red-500">{errors.universityId.message}</p>
            )}
          </div>

          <Input
            label="Disponibilidade"
            placeholder="Ex: Manhã e Tarde"
            error={errors.availability?.message}
            {...register('availability')}
          />
          
          <Input
            label="Áreas de Interesse (separadas por vírgula)"
            placeholder="Ex: Educação, Tecnologia, Saúde"
            error={errors.interestAreas?.message}
            {...register('interestAreas')}
          />
          
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
              {createMutation.isPending ? 'Cadastrando...' : 'Cadastrar Estudante'}
            </Button>
          </div>
        </form>
      </Modal>

      <div className="p-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {students?.map((student) => (
            <Card key={student.id} className="group hover:border-brand-primary transition-all">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 rounded-lg bg-orange-50 flex items-center justify-center text-orange-500">
                    <Users size={24} />
                  </div>
                  <div>
                    <h4 className="font-bold text-text-heading group-hover:text-brand-primary transition-colors">
                      {student.name}
                    </h4>
                    <p className="text-xs text-text-muted flex items-center gap-1">
                      <GraduationCap size={14} /> {student.universityName}
                    </p>
                  </div>
                </div>
                <button 
                  onClick={() => setDeletingId(student.id)}
                  className="text-text-muted hover:text-red-500 p-1"
                >
                  <Trash2 size={18} />
                </button>
              </div>

              <div className="space-y-3 pt-4 border-t border-border-default">
                <div className="flex items-center gap-2 text-sm text-text-body">
                  <Mail size={16} className="text-text-muted shrink-0" />
                  <span className="truncate">{student.email}</span>
                </div>
                <div className="text-xs text-text-muted">
                  <strong>Áreas:</strong> {student.interestAreas.join(', ')}
                </div>
              </div>

              <div className="mt-6 flex items-center justify-between">
                <span className={`px-2 py-1 rounded-md text-[10px] font-bold uppercase ${
                  student.status === 'VALIDATED' ? 'bg-emerald-100 text-emerald-700' :
                  student.status === 'REJECTED' ? 'bg-red-100 text-red-700' :
                  'bg-amber-100 text-amber-700'
                }`}>
                  {student.status === 'VALIDATED' ? 'Validado' : 
                   student.status === 'REJECTED' ? 'Rejeitado' : 'Pendente'}
                </span>
                
                {student.status === 'PENDING' && (
                  <div className="flex gap-2">
                    <button 
                      onClick={() => validateMutation.mutate(student.id)}
                      className="text-emerald-500 hover:text-emerald-600 transition-colors"
                      title="Validar"
                    >
                      <ShieldCheck size={20} />
                    </button>
                    <button 
                      onClick={() => rejectMutation.mutate(student.id)}
                      className="text-red-500 hover:text-red-600 transition-colors"
                      title="Rejeitar"
                    >
                      <XCircle size={20} />
                    </button>
                  </div>
                )}

                {student.status === 'VALIDATED' && (
                  <div className="flex items-center gap-1 text-emerald-600 text-xs font-medium">
                    <ShieldCheck size={14} />
                    Verificado
                  </div>
                )}
              </div>
            </Card>
          ))}
        </div>

        <ConfirmModal
          isOpen={!!deletingId}
          onClose={() => setDeletingId(null)}
          onConfirm={() => deletingId && deleteMutation.mutate(deletingId)}
          title="Remover Estudante"
          description="Tem certeza que deseja remover este estudante? Esta ação não pode ser desfeita."
          isLoading={deleteMutation.isPending}
        />

        {students?.length === 0 && (
          <Card className="p-12 text-center">
            <Users className="mx-auto text-text-muted mb-4" size={48} />
            <h3 className="text-lg font-bold text-text-heading">Nenhum estudante encontrado</h3>
            <p className="text-text-body mt-2">Os estudantes cadastrados aparecerão aqui.</p>
          </Card>
        )}
      </div>
    </div>
  );
}
