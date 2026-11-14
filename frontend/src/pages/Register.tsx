import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { User, Mail, Lock, Building2, School as SchoolIcon, ChevronDown } from 'lucide-react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import toast from 'react-hot-toast';
import api from '../lib/api';

const registerSchema = z.object({
  name: z.string().min(3, 'O nome deve ter pelo menos 3 caracteres'),
  email: z.string().email('E-mail inválido'),
  password: z.string().min(6, 'A senha deve ter pelo menos 6 caracteres'),
  confirmPassword: z.string().min(6, 'A confirmação de senha deve ter pelo menos 6 caracteres'),
}).refine((data) => data.password === data.confirmPassword, {
  message: "As senhas não coincidem",
  path: ["confirmPassword"],
});

type RegisterForm = z.infer<typeof registerSchema>;

interface Institution {
  id: string;
  name: string;
}

export function Register() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const role = searchParams.get('role');
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  const roleDisplay = role === 'UNIVERSITY_ADMIN' 
    ? 'Administrador da Universidade' 
    : 'Diretor da Escola';
  
  const RoleIcon = role === 'UNIVERSITY_ADMIN' ? Building2 : SchoolIcon;

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = async (data: RegisterForm) => {
    try {
      setIsSubmitting(true);
      const endpoint = role === 'UNIVERSITY_ADMIN' 
        ? '/auth/register/university-admin' 
        : '/auth/register/director';
      
      const payload = {
        name: data.name,
        email: data.email,
        password: data.password,
      };

      await api.post(endpoint, payload);
      
      toast.success('Cadastro realizado com sucesso! Verifique seu e-mail para confirmação.');
      navigate(`/confirm-email?email=${encodeURIComponent(data.email)}`);
    } catch (error: any) {
      const message = error.response?.data?.message || 'Erro ao realizar cadastro. Tente novamente.';
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!role || (role !== 'UNIVERSITY_ADMIN' && role !== 'SCHOOL_DIRECTOR')) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gradient-brand p-6">
        <div className="w-full max-w-md space-y-8 rounded-3xl bg-white p-12 shadow-sm text-center">
          <h2 className="text-2xl font-bold text-text-heading">Perfil inválido</h2>
          <p className="text-text-body">Por favor, selecione um perfil válido no login.</p>
          <Button onClick={() => navigate('/login')} className="w-full">Voltar para o Login</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-brand p-6">
      <div className="w-full max-w-[520px] space-y-8 rounded-xl bg-white p-8 shadow-sm lg:rounded-3xl lg:p-12">
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-brand-primary mb-4">
            <RoleIcon size={24} />
            <span className="text-sm font-bold uppercase tracking-wider">{roleDisplay}</span>
          </div>
          <h2 className="text-3xl font-bold text-text-heading font-display">Solicitar Acesso</h2>
          <p className="text-text-body">Preencha os dados abaixo para iniciar seu cadastro</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="relative">
            <Input
              {...register('name')}
              label="Nome Completo"
              placeholder="Seu nome"
              error={errors.name?.message}
              className="pl-10"
            />
            <User className="absolute left-3 top-[38px] h-5 w-5 text-text-muted" />
          </div>

          <div className="relative">
            <Input
              {...register('email')}
              label="E-mail Profissional"
              placeholder="exemplo@instituicao.com"
              error={errors.email?.message}
              className="pl-10"
            />
            <Mail className="absolute left-3 top-[38px] h-5 w-5 text-text-muted" />
          </div>

          <div className="relative">
            <Input
              {...register('password')}
              type="password"
              label="Senha"
              placeholder="••••••••"
              error={errors.password?.message}
              className="pl-10"
            />
            <Lock className="absolute left-3 top-[38px] h-5 w-5 text-text-muted" />
          </div>

          <div className="relative">
            <Input
              {...register('confirmPassword')}
              type="password"
              label="Confirmar Senha"
              placeholder="••••••••"
              error={errors.confirmPassword?.message}
              className="pl-10"
            />
            <Lock className="absolute left-3 top-[38px] h-5 w-5 text-text-muted" />
          </div>

          <Button type="submit" className="w-full mt-6" disabled={isSubmitting}>
            {isSubmitting ? 'Enviando...' : 'Enviar Solicitação'}
          </Button>
        </form>

        <div className="text-center pt-4 border-t border-gray-100">
          <p className="text-text-muted text-sm">
            Já tem uma conta?{' '}
            <Link to="/login" className="text-brand-primary font-bold hover:underline">
              Entrar
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
