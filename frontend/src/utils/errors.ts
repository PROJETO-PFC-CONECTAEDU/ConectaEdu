import { AxiosError } from 'axios';
import toast from 'react-hot-toast';

interface BackendError {
  detail?: string;
  title?: string;
  status?: number;
  message?: string;
  errors?: Record<string, string[]>;
}

export function handleApiError(error: unknown) {
  if (typeof error === 'string') {
    toast.error(error);
    return;
  }

  const axiosError = error as AxiosError<BackendError>;
  
  if (!axiosError.response) {
    if (axiosError.code === 'ECONNABORTED') {
      toast.error('O servidor demorou muito para responder. Tente novamente.');
    } else {
      toast.error('Não foi possível conectar ao servidor. Verifique sua internet.');
    }
    return;
  }

  const { status, data } = axiosError.response;

  // Erros de validação (geralmente 400 com lista de erros)
  if (status === 400 && data?.errors) {
    const errorMessages = Object.values(data.errors).flat();
    if (errorMessages.length > 0) {
      errorMessages.forEach(msg => toast.error(msg));
      return;
    }
  }

  // Mensagem de detalhe do ProblemDetail (Spring)
  const message = data?.detail || data?.message || 'Ocorreu um erro inesperado.';

  switch (status) {
    case 400:
      toast.error(message);
      break;
    case 401:
      if (window.location.pathname.includes('/login')) {
        toast.error(message);
      }
      break;
    case 403:
      toast.error('Você não tem permissão para realizar esta ação.');
      break;
    case 404:
      toast.error(message || 'Recurso não encontrado.');
      break;
    case 409:
      toast.error(message);
      break;
    case 422:
      toast.error(message);
      break;
    case 500:
      toast.error('Erro interno no servidor. Tente novamente mais tarde.');
      break;
    default:
      toast.error(message);
  }
}
