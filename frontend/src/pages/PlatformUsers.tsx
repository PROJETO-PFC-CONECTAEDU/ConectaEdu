import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import api from '../lib/api';
import { Header } from '../components/layout/Header';
import { Card } from '../components/ui/Card';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Modal } from '../components/ui/Modal';
import { ConfirmModal } from '../components/ui/ConfirmModal';
import { Users, Mail, ShieldCheck, UserX, CheckCircle, XCircle, Pencil, Trash2 } from 'lucide-react';
import { toast } from 'react-hot-toast';

interface User {
  id: string;
  name: string;
  email: string;
  userType: string;
  userRole: 'PLATFORM_ADMIN' | 'UNIVERSITY_ADMIN' | 'STUDENT' | 'SCHOOL_DIRECTOR';
  active: boolean;
  emailConfirmed: boolean;
  institutionName?: string;
}

const userSchema = z.object({
  name: z.string().min(3, 'Nome deve ter pelo menos 3 caracteres'),
  email: z.string().email('Email inválido'),
});

type UserFormData = z.infer<typeof userSchema>;

export function PlatformUsers() {
  const queryClient = useQueryClient();
  const [activatingId, setActivatingId] = useState<string | null>(null);
  const [deactivatingId, setDeactivatingId] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<string | null>(null);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [filter, setFilter] = useState<'ALL' | 'UNIVERSITY_ADMIN' | 'SCHOOL_DIRECTOR'>('ALL');

  const { register, handleSubmit, reset, formState: { errors } } = useForm<UserFormData>({
    resolver: zodResolver(userSchema),
  });

  const { data: users, isLoading, error } = useQuery<User[]>({
    queryKey: ['platform-users'],
    queryFn: async () => {
      const response = await api.get('/users');
      return response.data;
    },
  });

  const activateMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.patch(`/users/${id}/activate`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['platform-users'] });
      toast.success('Usuário ativado com sucesso!');
      setActivatingId(null);
    },
    onError: () => {
      toast.error('Erro ao ativar usuário.');
    }
  });

  const deactivateMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.patch(`/users/${id}/deactivate`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['platform-users'] });
      toast.success('Usuário desativado com sucesso!');
      setDeactivatingId(null);
    },
    onError: () => {
      toast.error('Erro ao desativar usuário.');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/users/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['platform-users'] });
      toast.success('Usuário removido com sucesso!');
      setDeletingId(null);
    },
    onError: () => {
      toast.error('Erro ao remover usuário.');
    }
  });

  const updateMutation = useMutation({
    mutationFn: async ({ id, data }: { id: string; data: UserFormData }) => {
      await api.put(`/users/${id}`, data);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['platform-users'] });
      toast.success('Usuário atualizado com sucesso!');
      setEditingUser(null);
      reset();
    },
    onError: (error: any) => {
      const message = error.response?.data?.message || 'Erro ao atualizar usuário.';
      toast.error(message);
    }
  });

  const onUpdateSubmit = (data: UserFormData) => {
    if (editingUser) {
      updateMutation.mutate({ id: editingUser.id, data });
    }
  };

  const handleEdit = (user: User) => {
    setEditingUser(user);
    reset({
      name: user.name,
      email: user.email,
    });
  };

  if (isLoading) return <div className="p-8">Carregando usuários...</div>;
  if (error) return <div className="p-8 text-red-500">Erro ao carregar usuários.</div>;

  const filteredUsers = users?.filter(user => {
    if (filter === 'ALL') return user.userRole === 'UNIVERSITY_ADMIN' || user.userRole === 'SCHOOL_DIRECTOR';
    return user.userRole === filter;
  });

  return (
    <div className="flex-1">
      <Header 
        title="Usuários Administrativos" 
        subtitle="Gerencie os diretores de escola e administradores de universidade." 
      />
      
      <div className="p-8">
        <div className="flex gap-4 mb-8">
          <Button 
            variant={filter === 'ALL' ? 'primary' : 'outline'}
            onClick={() => setFilter('ALL')}
          >
            Todos
          </Button>
          <Button 
            variant={filter === 'UNIVERSITY_ADMIN' ? 'primary' : 'outline'}
            onClick={() => setFilter('UNIVERSITY_ADMIN')}
          >
            Adm. Universidade
          </Button>
          <Button 
            variant={filter === 'SCHOOL_DIRECTOR' ? 'primary' : 'outline'}
            onClick={() => setFilter('SCHOOL_DIRECTOR')}
          >
            Diretores
          </Button>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredUsers?.map((user) => (
            <Card key={user.id} className="group hover:border-brand-primary transition-all">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-4">
                  <div className={`w-12 h-12 rounded-lg flex items-center justify-center ${
                    user.userRole === 'UNIVERSITY_ADMIN' ? 'bg-blue-50 text-blue-500' : 'bg-purple-50 text-purple-500'
                  }`}>
                    <Users size={24} />
                  </div>
                  <div>
                    <h4 className="font-bold text-text-heading group-hover:text-brand-primary transition-colors">
                      {user.name}
                    </h4>
                    <p className="text-xs text-text-muted">
                      {user.institutionName || 'Plataforma ConectaEdu'}
                    </p>
                  </div>
                </div>
                <div className="flex gap-2">
                  <button 
                    onClick={() => handleEdit(user)}
                    className="text-text-muted hover:text-brand-primary p-1"
                    title="Editar"
                  >
                    <Pencil size={18} />
                  </button>
                  <button 
                    onClick={() => setDeletingId(user.id)}
                    className="text-text-muted hover:text-red-500 p-1"
                    title="Excluir"
                  >
                    <Trash2 size={18} />
                  </button>
                </div>
              </div>

              <div className="space-y-3 pt-4 border-t border-border-default">
                <div className="flex items-center gap-2 text-sm text-text-body">
                  <Mail size={16} className="text-text-muted shrink-0" />
                  <span className="truncate">{user.email}</span>
                </div>
                <div className="flex items-center gap-2 text-xs">
                    <span className="font-semibold text-text-muted">Email Confirmado:</span>
                    {user.emailConfirmed ? (
                        <span className="flex items-center gap-1 text-emerald-600">
                            <CheckCircle size={14} /> Sim
                        </span>
                    ) : (
                        <span className="flex items-center gap-1 text-amber-600">
                            <XCircle size={14} /> Não
                        </span>
                    )}
                </div>
              </div>

              <div className="mt-6 flex items-center justify-between">
                <span className={`px-2 py-1 rounded-md text-[10px] font-bold uppercase ${
                  user.active ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'
                }`}>
                  {user.active ? 'Ativo' : 'Inativo'}
                </span>
                
                <div className="flex gap-2">
                  {!user.active ? (
                    <Button 
                      size="sm"
                      className="bg-emerald-500 hover:bg-emerald-600 text-white"
                      onClick={() => setActivatingId(user.id)}
                    >
                      <ShieldCheck size={16} className="mr-1" /> Ativar
                    </Button>
                  ) : (
                    <Button 
                      size="sm"
                      variant="outline"
                      className="text-amber-600 border-amber-600 hover:bg-amber-50"
                      onClick={() => setDeactivatingId(user.id)}
                    >
                      <UserX size={16} className="mr-1" /> Desativar
                    </Button>
                  )}
                </div>
              </div>
            </Card>
          ))}
        </div>

        {filteredUsers?.length === 0 && (
          <Card className="p-12 text-center">
            <Users className="mx-auto text-text-muted mb-4" size={48} />
            <h3 className="text-lg font-bold text-text-heading">Nenhum usuário encontrado</h3>
            <p className="text-text-body mt-2">Os diretores e administradores aparecerão aqui.</p>
          </Card>
        )}
      </div>

      <Modal
        isOpen={!!editingUser}
        onClose={() => setEditingUser(null)}
        title="Editar Usuário"
      >
        <form onSubmit={handleSubmit(onUpdateSubmit)} className="space-y-4">
          <Input
            label="Nome Completo"
            error={errors.name?.message}
            {...register('name')}
          />
          <Input
            label="Email"
            type="email"
            error={errors.email?.message}
            {...register('email')}
          />
          <div className="flex gap-3 pt-2">
            <Button 
              type="button" 
              variant="outline" 
              className="flex-1"
              onClick={() => setEditingUser(null)}
            >
              Cancelar
            </Button>
            <Button 
              type="submit" 
              className="flex-1"
              disabled={updateMutation.isPending}
            >
              {updateMutation.isPending ? 'Salvando...' : 'Salvar Alterações'}
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmModal
        isOpen={!!activatingId}
        onClose={() => setActivatingId(null)}
        onConfirm={() => activatingId && activateMutation.mutate(activatingId)}
        title="Ativar Usuário"
        description="Deseja ativar este usuário? Ele terá acesso total às funcionalidades do seu cargo."
        isLoading={activateMutation.isPending}
      />

      <ConfirmModal
        isOpen={!!deactivatingId}
        onClose={() => setDeactivatingId(null)}
        onConfirm={() => deactivatingId && deactivateMutation.mutate(deactivatingId)}
        title="Desativar Usuário"
        description="Deseja desativar este usuário? Ele não conseguirá mais acessar a plataforma."
        isLoading={deactivateMutation.isPending}
      />

      <ConfirmModal
        isOpen={!!deletingId}
        onClose={() => setDeletingId(null)}
        onConfirm={() => deletingId && deleteMutation.mutate(deletingId)}
        title="Remover Usuário"
        description="Tem certeza que deseja remover este usuário? Esta ação não pode ser desfeita."
        isLoading={deleteMutation.isPending}
      />
    </div>
  );
}
