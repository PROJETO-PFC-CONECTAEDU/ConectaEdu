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
import { MapPin, Users, School as SchoolIcon, Trash2, Plus, ShieldCheck, XCircle } from 'lucide-react';
import { toast } from 'react-hot-toast';

interface School {
  id: string;
  name: string;
  cie: string;
  director?: string;
  address?: string;
  active: boolean;
}

const schoolSchema = z.object({
  name: z.string().min(3, 'Nome deve ter pelo menos 3 caracteres'),
  cie: z.string().min(1, 'CIE é obrigatório'),
  director: z.string().min(1, 'Diretor é obrigatório'),
  address: z.string().min(1, 'Endereço é obrigatório'),
});

type SchoolFormData = z.infer<typeof schoolSchema>;

export function Schools() {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<SchoolFormData>({
    resolver: zodResolver(schoolSchema)
  });
  const { data: schools, isLoading, error } = useQuery<School[]>({
    queryKey: ['schools'],
    queryFn: async () => {
      const response = await api.get('/schools');
      return response.data;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/schools/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schools'] });
      toast.success('Escola removida com sucesso!');
      setDeletingId(null);
    }
  });

  const createMutation = useMutation({
    mutationFn: async (data: SchoolFormData) => {
      await api.post('/schools', data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schools'] });
      toast.success('Escola criada com sucesso!');
      setIsModalOpen(false);
      reset();
    },
    onError: () => {
      toast.error('Erro ao criar escola.');
    }
  });

  const onSubmit = (data: SchoolFormData) => {
    createMutation.mutate(data);
  };

  const toggleStatusMutation = useMutation({
    mutationFn: async ({ id, active, cie }: { id: string, active: boolean, cie: string }) => {
      const endpoint = active ? 'deactivate' : 'activate';
      await api.patch(`/schools/${id}/${endpoint}`, { cie });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schools'] });
      toast.success('Status atualizado!');
    }
  });

  return (
    <div className="flex-1">
      <Header 
        title="Escolas Parceiras" 
        subtitle="Gerencie as instituições de ensino cadastradas." 
        action={
          <button 
            onClick={() => setIsModalOpen(true)}
            className="bg-brand-primary text-white px-4 py-2 rounded-lg flex items-center gap-2 font-bold hover:bg-brand-primary/90 transition-colors"
          >
            <Plus size={20} /> Nova Escola
          </button>
        }
      />

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Nova Escola"
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Nome da Escola"
            placeholder="Ex: Escola Estadual Machado de Assis"
            error={errors.name?.message}
            {...register('name')}
          />
          <Input
            label="CIE"
            placeholder="Código de identificação"
            error={errors.cie?.message}
            {...register('cie')}
          />
          <Input
            label="Diretor"
            placeholder="Nome do diretor"
            error={errors.director?.message}
            {...register('director')}
          />
          <Input
            label="Endereço"
            placeholder="Endereço completo"
            error={errors.address?.message}
            {...register('address')}
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
              {createMutation.isPending ? 'Criando...' : 'Criar Escola'}
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
            Erro ao carregar escolas.
          </div>
        )}

        {!isLoading && !error && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {schools?.map((school) => (
              <Card key={school.id} className="hover:border-brand-primary transition-all group">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-lg bg-brand-primary/10 flex items-center justify-center text-brand-primary">
                      <SchoolIcon size={24} />
                    </div>
                    <div>
                      <h4 className="font-bold text-text-heading group-hover:text-brand-primary transition-colors">
                        {school.name}
                      </h4>
                      <p className="text-xs text-text-muted">CIE: {school.cie}</p>
                    </div>
                  </div>
                  <button 
                    onClick={() => setDeletingId(school.id)}
                    className="text-text-muted hover:text-red-500 p-1"
                  >
                    <Trash2 size={18} />
                  </button>
                </div>

                <div className="space-y-3 pt-4 border-t border-border-default">
                  <div className="flex items-center gap-2 text-sm text-text-body">
                    <Users size={16} className="text-text-muted shrink-0" />
                    <span>Dir: {school.director || 'Não informado'}</span>
                  </div>
                  <div className="flex items-start gap-2 text-sm text-text-body">
                    <MapPin size={16} className="mt-0.5 text-text-muted shrink-0" />
                    <span>{school.address || 'Endereço não informado'}</span>
                  </div>
                </div>

                <div className="mt-6 flex items-center justify-between">
                  <span className={`px-2 py-1 rounded-md text-[10px] font-bold uppercase ${
                    school.active ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'
                  }`}>
                    {school.active ? 'Ativa' : 'Pendente'}
                  </span>
                  
                  {!school.active && (
                    <button 
                      onClick={() => toggleStatusMutation.mutate({ id: school.id, active: school.active, cie: school.cie })}
                      className="text-emerald-500 hover:text-emerald-600 transition-colors"
                      title="Ativar"
                    >
                      <ShieldCheck size={20} />
                    </button>
                  )}

                  {school.active && (
                    <div className="flex items-center gap-4">
                      <div className="flex items-center gap-1 text-emerald-600 text-xs font-medium">
                        <ShieldCheck size={14} />
                        Verificada
                      </div>
                      <button 
                        onClick={() => toggleStatusMutation.mutate({ id: school.id, active: school.active, cie: school.cie })}
                        className="text-text-muted hover:text-red-500 transition-colors"
                        title="Desativar"
                      >
                        <XCircle size={20} />
                      </button>
                    </div>
                  )}
                </div>
              </Card>
            ))}

            {schools?.length === 0 && (
              <div className="col-span-full py-12 text-center text-text-muted">
                Nenhuma escola encontrada.
              </div>
            )}
          </div>
        )}

        <ConfirmModal
          isOpen={!!deletingId}
          onClose={() => setDeletingId(null)}
          onConfirm={() => deletingId && deleteMutation.mutate(deletingId)}
          title="Remover Escola"
          description="Tem certeza que deseja remover esta escola? Esta ação não pode ser desfeita."
          isLoading={deleteMutation.isPending}
        />
      </div>
    </div>
  );
}
