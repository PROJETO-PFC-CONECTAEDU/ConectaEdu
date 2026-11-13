import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Mail, Lock, Key } from 'lucide-react';
import { useMutation } from '@tanstack/react-query';
import api from '../../lib/api';
import { Modal } from '../ui/Modal';
import { Input } from '../ui/Input';
import { Button } from '../ui/Button';
import toast from 'react-hot-toast';

const emailSchema = z.object({
  email: z.string().email('E-mail inválido'),
});

const resetSchema = z.object({
  token: z.string().min(1, 'O código é obrigatório'),
  newPassword: z.string().min(6, 'A senha deve ter pelo menos 6 caracteres'),
  confirmPassword: z.string().min(6, 'A confirmação de senha deve ter pelo menos 6 caracteres'),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "As senhas não coincidem",
  path: ["confirmPassword"],
});

type EmailForm = z.infer<typeof emailSchema>;
type ResetForm = z.infer<typeof resetSchema>;

interface ForgotPasswordModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function ForgotPasswordModal({ isOpen, onClose }: ForgotPasswordModalProps) {
  const [step, setStep] = useState<'email' | 'reset'>('email');
  const [email, setEmail] = useState('');

  const emailForm = useForm<EmailForm>({
    resolver: zodResolver(emailSchema),
  });

  const resetForm = useForm<ResetForm>({
    resolver: zodResolver(resetSchema),
  });

  const forgotPasswordMutation = useMutation({
    mutationFn: async (data: EmailForm) => {
      await api.post('/auth/forgot-password', data);
    },
    onSuccess: (_, variables) => {
      setEmail(variables.email);
      setStep('reset');
      resetForm.setValue('token', '');
      toast.success('Código enviado para o seu e-mail!');
    },
    onError: () => {
      toast.error('Ocorreu um erro ao solicitar a recuperação de senha.');
    },
  });

  const resetPasswordMutation = useMutation({
    mutationFn: async (data: ResetForm) => {
      await api.post('/auth/reset-password', {
        token: data.token,
        newPassword: data.newPassword,
      });
    },
    onSuccess: () => {
      toast.success('Senha redefinida com sucesso!');
      handleClose();
    },
    onError: (error: any) => {
      const message = error.response?.data?.message || 'Código inválido ou expirado.';
      toast.error(message);
    },
  });

  const handleClose = () => {
    setStep('email');
    emailForm.reset();
    resetForm.reset();
    onClose();
  };

  const onEmailSubmit = (data: EmailForm) => {
    forgotPasswordMutation.mutate(data);
  };

  const onResetSubmit = (data: ResetForm) => {
    resetPasswordMutation.mutate(data);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      title={step === 'email' ? 'Recuperar Senha' : 'Definir Nova Senha'}
    >
      {step === 'email' ? (
        <form onSubmit={emailForm.handleSubmit(onEmailSubmit)} className="space-y-4">
          <p className="text-text-body text-sm">
            Insira seu e-mail cadastrado e enviaremos um código para você redefinir sua senha.
          </p>
          <div className="relative">
            <Input
              {...emailForm.register('email')}
              label="E-mail"
              placeholder="exemplo@email.com"
              error={emailForm.formState.errors.email?.message}
              className="pl-10"
            />
            <Mail className="absolute left-3 top-[38px] h-5 w-5 text-text-muted" />
          </div>
          <Button
            type="submit"
            className="w-full"
            disabled={forgotPasswordMutation.isPending}
          >
            {forgotPasswordMutation.isPending ? 'Enviando...' : 'Enviar código'}
          </Button>
        </form>
      ) : (
        <form onSubmit={resetForm.handleSubmit(onResetSubmit)} className="space-y-4">
          <p className="text-text-body text-sm">
            Insira o código enviado para <strong>{email}</strong> e escolha sua nova senha.
          </p>
          
          <div className="relative">
            <Input
              {...resetForm.register('token')}
              label="Código de verificação"
              placeholder="Insira o código"
              error={resetForm.formState.errors.token?.message}
              className="pl-10"
            />
            <Key className="absolute left-3 top-[38px] h-5 w-5 text-text-muted" />
          </div>

          <div className="relative">
            <Input
              {...resetForm.register('newPassword')}
              type="password"
              label="Nova Senha"
              placeholder="••••••••"
              error={resetForm.formState.errors.newPassword?.message}
              className="pl-10"
            />
            <Lock className="absolute left-3 top-[38px] h-5 w-5 text-text-muted" />
          </div>

          <div className="relative">
            <Input
              {...resetForm.register('confirmPassword')}
              type="password"
              label="Confirmar Nova Senha"
              placeholder="••••••••"
              error={resetForm.formState.errors.confirmPassword?.message}
              className="pl-10"
            />
            <Lock className="absolute left-3 top-[38px] h-5 w-5 text-text-muted" />
          </div>

          <Button
            type="submit"
            className="w-full"
            disabled={resetPasswordMutation.isPending}
          >
            {resetPasswordMutation.isPending ? 'Redefinindo...' : 'Redefinir senha'}
          </Button>
          
          <button
            type="button"
            onClick={() => setStep('email')}
            className="w-full text-sm text-brand-primary hover:underline"
          >
            Voltar para e-mail
          </button>
        </form>
      )}
    </Modal>
  );
}
