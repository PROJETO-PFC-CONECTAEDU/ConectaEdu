import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Mail, ShieldCheck } from 'lucide-react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import toast from 'react-hot-toast';
import api from '../lib/api';

const confirmEmailSchema = z.object({
  email: z.string().email('E-mail inválido'),
  code: z.string().length(6, 'O código deve ter 6 dígitos'),
});

type ConfirmEmailForm = z.infer<typeof confirmEmailSchema>;

export function ConfirmEmail() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const emailParam = searchParams.get('email') || '';

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<ConfirmEmailForm>({
    resolver: zodResolver(confirmEmailSchema),
    defaultValues: {
      email: emailParam,
      code: '',
    },
  });

  useEffect(() => {
    if (emailParam) {
      setValue('email', emailParam);
    }
  }, [emailParam, setValue]);

  const onSubmit = async (data: ConfirmEmailForm) => {
    try {
      setIsSubmitting(true);
      await api.post('/auth/confirm-email', data);
      
      toast.success('E-mail confirmado com sucesso! Sua conta será ativada por um administrador.');
      navigate('/login');
    } catch (error: any) {
      const message = error.response?.data?.message || 'Código inválido ou expirado.';
      toast.error(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-brand p-6">
      <div className="w-full max-w-[520px] space-y-8 rounded-xl bg-white p-8 shadow-sm lg:rounded-3xl lg:p-12">
        <div className="space-y-2 text-center">
          <div className="flex justify-center mb-4">
            <div className="bg-brand-primary/10 p-3 rounded-full">
              <ShieldCheck size={32} className="text-brand-primary" />
            </div>
          </div>
          <h2 className="text-3xl font-bold text-text-heading font-display">Confirmar E-mail</h2>
          <p className="text-text-body">Insira o código de 6 dígitos enviado para o seu e-mail</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div className="relative">
            <Input
              {...register('email')}
              label="E-mail"
              placeholder="exemplo@instituicao.com"
              error={errors.email?.message}
              className="pl-10"
              readOnly={!!emailParam}
            />
            <Mail className="absolute left-3 top-[38px] h-5 w-5 text-text-muted" />
          </div>

          <div className="relative">
            <Input
              {...register('code')}
              label="Código de Confirmação"
              placeholder="000000"
              error={errors.code?.message}
              className="pl-10 text-center tracking-[0.5em] font-mono text-xl"
              maxLength={6}
            />
            <ShieldCheck className="absolute left-3 top-[38px] h-5 w-5 text-text-muted" />
          </div>

          <Button type="submit" className="w-full mt-6" disabled={isSubmitting}>
            {isSubmitting ? 'Confirmando...' : 'Confirmar E-mail'}
          </Button>
        </form>

        <div className="text-center pt-4 border-t border-gray-100">
          <p className="text-text-muted text-sm">
            Não recebeu o código?{' '}
            <button 
              type="button" 
              onClick={() => toast.success('Um novo código foi enviado para seu e-mail.')}
              className="text-brand-primary font-bold hover:underline"
            >
              Reenviar
            </button>
          </p>
          <p className="text-text-muted text-sm mt-2">
            <Link to="/login" className="text-brand-primary font-bold hover:underline">
              Voltar para o Login
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
