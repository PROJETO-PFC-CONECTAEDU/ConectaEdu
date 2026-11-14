import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Lock, Mail } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { ForgotPasswordModal } from '../components/auth/ForgotPasswordModal';
import { RequestAccessModal } from '../components/auth/RequestAccessModal';
import { useNavigate, Link } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import api from '../lib/api';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import { TwoFactorVerifyModal } from '../components/auth/TwoFactorVerifyModal';

const loginSchema = z.object({
  email: z.string().email('E-mail inválido'),
  password: z.string().min(6, 'A senha deve ter pelo menos 6 caracteres'),
});

type LoginForm = z.infer<typeof loginSchema>;

export function Login() {
  const [showPassword, setShowPassword] = useState(false);
  const [isForgotModalOpen, setIsForgotModalOpen] = useState(false);
  const [isRequestModalOpen, setIsRequestModalOpen] = useState(false);
  const [is2FAModalOpen, setIs2FAModalOpen] = useState(false);
  const [pendingEmail, setPendingEmail] = useState('');
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
      if (data.twoFactorEnabled && !data.token) {
        setPendingEmail(data.email);
        setIs2FAModalOpen(true);
        return;
      }
      signIn({
        token: data.token,
        user: {
          id: data.id,
          name: data.name,
          email: data.email,
          role: data.role,
          universityId: data.universityId,
          schoolId: data.schoolId,
          emailConfirmed: true, // Se logou, o email está confirmado
          twoFactorEnabled: data.twoFactorEnabled
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
          <h1 className="text-[20px] text-white font-display font-bold tracking-tight">
            Conecta<span className="text-brand-accent">Edu</span>
          </h1>
        </div>

        <div className="space-y-6">
          <h1 className="text-5xl font-bold text-white leading-tight font-display">
            Sua jornada pedagógica começa <span className="text-brand-accent">aqui</span>
          </h1>
          <p className="text-xl text-text-muted max-w-md">
            Conectando estudantes de Pedagogia às melhores oportunidades em escolas.
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

            <div className="flex">
              <button
                type="button"
                onClick={() => setIsForgotModalOpen(true)}
                className="text-sm font-medium text-brand-primary hover:text-brand-dark transition-colors"
              >
                Esqueci minha senha
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
          <div className="flex justify-center gap-4">
            <p className="text-text-muted">Não tem conta?</p>
            <button
                type="button"
                onClick={() => setIsRequestModalOpen(true)}
                className="text-sm font-medium text-brand-primary hover:text-brand-dark transition-colors"
            >
              Solicite acesso a plataforma
            </button>
          </div>

          <div className="flex justify-center gap-4 border-t border-gray-100 pt-6">
            <Link
              to="/legal/TERMS_OF_USE"
              className="text-sm text-text-muted hover:text-brand-primary transition-colors underline-offset-4 hover:underline"
            >
              Termos de Uso
            </Link>
            <span className="text-text-muted text-sm">•</span>
            <Link
              to="/legal/PRIVACY_POLICY"
              className="text-sm text-text-muted hover:text-brand-primary transition-colors underline-offset-4 hover:underline"
            >
              Política de Privacidade
            </Link>
          </div>
        </div>
      </div>
      
      <ForgotPasswordModal 
        isOpen={isForgotModalOpen} 
        onClose={() => setIsForgotModalOpen(false)} 
      />

      <RequestAccessModal
        isOpen={isRequestModalOpen}
        onClose={() => setIsRequestModalOpen(false)}
      />

      <TwoFactorVerifyModal
        isOpen={is2FAModalOpen}
        onClose={() => setIs2FAModalOpen(false)}
        email={pendingEmail}
        onSuccess={(data) => {
          signIn({
            token: data.token,
            user: {
              id: data.id,
              name: data.name,
              email: data.email,
              role: data.role,
              universityId: data.universityId,
              schoolId: data.schoolId,
              emailConfirmed: true,
              twoFactorEnabled: data.twoFactorEnabled
            }
          });
          toast.success('Bem-vindo ao ConectaEdu!');
          navigate('/dashboard');
        }}
      />
    </div>
  );
}
