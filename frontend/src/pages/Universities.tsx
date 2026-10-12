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
import { Building2, Users, MapPin, ShieldCheck, Trash2, Plus, XCircle } from 'lucide-react';
import { toast } from 'react-hot-toast';

interface University {
  id: string;
  name: string;
  cnpj: string;
  coordinator?: string;
  address?: string;
  active: boolean;
}

const universitySchema = z.object({
  name: z.string().min(3, 'Nome deve ter pelo menos 3 caracteres'),
  cnpj: z.string().length(14, 'CNPJ deve ter exatamente 14 dígitos'),
  coordinator: z.string().min(1, 'Coordenador é obrigatório'),
  address: z.string().min(1, 'Endereço é obrigatório'),
});

type UniversityFormData = z.infer<typeof universitySchema>;

export function Universities() {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [deletingId, setDeletingId] = useState<string | null>(null);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<UniversityFormData>({
    resolver: zodResolver(universitySchema)
  });
  const { data: universities, isLoading, error } = useQuery<University[]>({
    queryKey: ['universities'],
    queryFn: async () => {
      const response = await api.get('/universities');
      return response.data;
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/universities/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['universities'] });
      toast.success('Universidade removida com sucesso!');
      setDeletingId(null);
    }
  });

  const createMutation = useMutation({
    mutationFn: async (data: UniversityFormData) => {
      await api.post('/universities', data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['universities'] });
      toast.success('Universidade criada com sucesso!');
      setIsModalOpen(false);
      reset();
    },
    onError: () => {
      toast.error('Erro ao criar universidade. Verifique se o CNPJ tem 14 dígitos.');
    }
  });

  const onSubmit = (data: UniversityFormData) => {
    createMutation.mutate(data);
  };

  const validateMutation = useMutation({
    mutationFn: async ({ id, action }: { id: string, action: 'validate' | 'reject' }) => {
      await api.patch(`/universities/${id}/${action}`, {
        validationNotes: action === 'validate' ? 'Aprovado pelo Admin' : 'Recusado pelo Admin'
      });
    },
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['universities'] });
      toast.success(variables.action === 'validate' ? 'Universidade validada!' : 'Universidade recusada!');
    }
  });

  return (
    <div className="flex-1">
      <Header 
        title="Universidades" 
        subtitle="Gerencie as instituições de ensino superior parceiras." 
        action={
          <button 
            onClick={() => setIsModalOpen(true)}
            className="bg-brand-primary text-white px-4 py-2 rounded-lg flex items-center gap-2 font-bold hover:bg-brand-primary/90 transition-colors"
          >
            <Plus size={20} /> Nova Universidade
          </button>
        }
      />

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title="Nova Universidade"
      >
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Nome da Universidade"
            placeholder="Ex: Universidade Federal de São Paulo"
            error={errors.name?.message}
            {...register('name')}
          />
          <Input
            label="CNPJ"
            placeholder="Apenas números (14 dígitos)"
            error={errors.cnpj?.message}
            {...register('cnpj')}
          />
          <Input
            label="Coordenador"
            placeholder="Nome do coordenador"
            error={errors.coordinator?.message}
            {...register('coordinator')}
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
              {createMutation.isPending ? 'Criando...' : 'Criar Universidade'}
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
            Erro ao carregar universidades.
          </div>
        )}

        {!isLoading && !error && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {universities?.map((uni) => (
              <Card key={uni.id} className="hover:border-brand-primary transition-all group">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-lg bg-brand-primary/10 flex items-center justify-center text-brand-primary">
                      <Building2 size={24} />
                    </div>
                    <div>
                      <h4 className="font-bold text-text-heading group-hover:text-brand-primary transition-colors">
                        {uni.name}
                      </h4>
                      <p className="text-xs text-text-muted">CNPJ: {uni.cnpj}</p>
                    </div>
                  </div>
                  <button 
                    onClick={() => setDeletingId(uni.id)}
                    className="text-text-muted hover:text-red-500 p-1"
                  >
                    <Trash2 size={18} />
                  </button>
                </div>

                <div className="space-y-3 pt-4 border-t border-border-default">
                  <div className="flex items-center gap-2 text-sm text-text-body">
                    <Users size={16} className="text-text-muted shrink-0" />
                    <span>Coord: {uni.coordinator || 'Não informado'}</span>
                  </div>
                  <div className="flex items-start gap-2 text-sm text-text-body">
                    <MapPin size={16} className="mt-0.5 text-text-muted shrink-0" />
                    <span>{uni.address || 'Endereço não informado'}</span>
                  </div>
                </div>

                <div className="mt-6 flex items-center justify-between">
                  <span className={`px-2 py-1 rounded-md text-[10px] font-bold uppercase ${
                    uni.active ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'
                  }`}>
                    {uni.active ? 'Validada' : 'Pendente'}
                  </span>
                  
                  {!uni.active && (
                    <div className="flex gap-2">
                      <button 
                        onClick={() => validateMutation.mutate({ id: uni.id, action: 'validate' })}
                        className="text-emerald-500 hover:text-emerald-600 transition-colors"
                        title="Validar"
                      >
                        <ShieldCheck size={20} />
                      </button>
                      <button 
                        onClick={() => validateMutation.mutate({ id: uni.id, action: 'reject' })}
                        className="text-red-500 hover:text-red-600 transition-colors"
                        title="Rejeitar"
                      >
                        <XCircle size={20} />
                      </button>
                    </div>
                  )}

                  {uni.active && (
                    <div className="flex items-center gap-1 text-emerald-600 text-xs font-medium">
                      <ShieldCheck size={14} />
                      Verificada
                    </div>
                  )}
                </div>
              </Card>
            ))}

            {universities?.length === 0 && (
              <div className="col-span-full py-12 text-center text-text-muted">
                Nenhuma universidade encontrada.
              </div>
            )}
          </div>
        )}

        <ConfirmModal
          isOpen={!!deletingId}
          onClose={() => setDeletingId(null)}
          onConfirm={() => deletingId && deleteMutation.mutate(deletingId)}
          title="Remover Universidade"
          description="Tem certeza que deseja remover esta universidade? Esta ação não pode ser desfeita."
          isLoading={deleteMutation.isPending}
        />
      </div>
    </div>
  );
}
