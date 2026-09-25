import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Lock, Mail } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import api from '../lib/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

const loginSchema = z.object({
  email: z.string().email('E-mail inválido'),
  password: z.string().min(6, 'A senha deve ter pelo menos 6 caracteres'),
});

type LoginForm = z.infer<typeof loginSchema>;

export function Login() {
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();
  const { signIn } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
  });

  const loginMutation = useMutation({
    mutationFn: async (data: LoginForm) => {
      const response = await api.post('/auth/login', data);
      return response.data;
    },
    onSuccess: (data) => {
      signIn({
        token: data.token,
        user: {
          id: data.id,
          name: data.name,
          email: data.email,
          role: data.role
        }
      });
      toast.success('Bem-vindo ao ConectaEdu!');
      navigate('/dashboard');
    },
    onError: () => {
      toast.error('E-mail ou senha incorretos.');
    },
  });

  const onSubmit = (data: LoginForm) => {
    loginMutation.mutate(data);
  };

  return (
    <div className="flex min-h-screen">
      {/* Painel de Marca */}
      <div className="hidden w-1/2 flex-col justify-between bg-brand-dark p-12 text-white lg:flex">
        <div className="flex items-center gap-2">
          <div className="h-8 w-8 rounded-lg bg-brand-primary" />
          <span className="text-2xl font-bold font-display tracking-tight">ConectaEdu</span>
        </div>

        <div className="space-y-6">
          <h1 className="text-5xl font-bold leading-tight font-display">
            Sua jornada pedagógica começa <span className="text-brand-primary">aqui</span>
          </h1>
          <p className="text-xl text-text-muted max-w-md">
            Conectando estudantes de Pedagogia às melhores oportunidades em escolas parceiras.
          </p>
        </div>
      </div>

      {/* Painel de Formulário */}
      <div className="flex w-full items-center justify-center bg-gradient-brand p-6 lg:w-1/2">
        <div className="w-full max-w-[480px] space-y-8 rounded-xl bg-white p-8 shadow-sm lg:rounded-3xl lg:p-12">
          <div className="space-y-2">
            <h2 className="text-3xl font-bold text-text-heading font-display">Entrar no ConectaEdu</h2>
            <p className="text-text-body">Acesse sua conta para gerenciar seus estágios</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <div className="relative">
              <Input
                {...register('email')}
                label="E-mail"
                placeholder="exemplo@email.com"
                error={errors.email?.message}
                className="pl-10"
              />
              <Mail className="absolute left-3 top-[38px] h-5 w-5 text-text-muted" />
            </div>

            <div className="relative">
              <Input
                {...register('password')}
                type={showPassword ? 'text' : 'password'}
                label="Senha"
                placeholder="••••••••"
                error={errors.password?.message}
                className="pl-10 pr-10"
              />
              <Lock className="absolute left-3 top-[38px] h-5 w-5 text-text-muted" />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-[38px] text-text-muted hover:text-text-body"
              >
                {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
              </button>
            </div>

            <Button
              type="submit"
              className="w-full"
              disabled={loginMutation.isPending}
            >
              {loginMutation.isPending ? 'Entrando...' : 'Entrar'}
            </Button>
          </form>
        </div>
      </div>
    </div>
  );
}
