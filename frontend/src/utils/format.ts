export const roleTranslations: Record<string, string> = {
  'PLATFORM_ADMIN': 'Administrador',
  'UNIVERSITY_ADMIN': 'Administrador de Universidade',
  'SCHOOL_DIRECTOR': 'Diretor de Escola',
  'STUDENT': 'Estudante',
};

export const demandStatusTranslations: Record<string, string> = {
  'DRAFT': 'Rascunho',
  'WAITING': 'Inscrições Abertas',
  'ONGOING': 'Em Andamento',
  'FINISHED': 'Concluída',
  'ARCHIVED': 'Arquivada',
};

export const lessonStatusTranslations: Record<string, string> = {
  'SCHEDULED': 'Agendada',
  'CONFIRMED': 'Confirmada',
  'CANCELED': 'Cancelada',
  'COMPLETED': 'Registrada',
  'REGISTERED': 'Registrada',
  'SIGNED': 'Assinada',
  'VALIDATED': 'Validada',
  'NOT_COMPLETED': 'Não Concluída',
};

export const studentStatusTranslations: Record<string, string> = {
  'PENDING': 'Pendente',
  'VALIDATED': 'Validado',
  'REJECTED': 'Rejeitado',
  'INACTIVE': 'Inativo',
};

export const genericStatusTranslations: Record<string, string> = {
  'PENDING': 'Pendente',
  'APPROVED': 'Aprovado',
  'REJECTED': 'Rejeitado',
  'ACTIVE': 'Ativo',
  'INACTIVE': 'Inativo',
  'SUCCESS': 'Sucesso',
  'ERROR': 'Erro',
  'VALIDATED': 'Validado',
};

export const legalDocumentStatusTranslations: Record<string, string> = {
  'PUBLISHED': 'Em vigor',
  'ARCHIVED': 'Arquivado',
};

export const legalDocumentTypeTranslations: Record<string, string> = {
  'TERMS_OF_USE': 'Termos de Uso',
  'PRIVACY_POLICY': 'Política de Privacidade',
};

export const formatRole = (role?: string) => {
  if (!role) return '';
  const normalized = role.toUpperCase();
  return roleTranslations[normalized] || genericStatusTranslations[normalized] || role.replace('_', ' ');
};

export const formatDemandStatus = (status: string) => {
  if (!status) return '';
  const normalized = status.toUpperCase();
  return demandStatusTranslations[normalized] || genericStatusTranslations[normalized] || status;
};

export const formatLessonStatus = (status: string) => {
  if (!status) return '';
  const normalized = status.toUpperCase();
  return lessonStatusTranslations[normalized] || genericStatusTranslations[normalized] || status;
};

export const formatStudentStatus = (status: string) => {
  if (!status) return '';
  const normalized = status.toUpperCase();
  return studentStatusTranslations[normalized] || genericStatusTranslations[normalized] || status;
};

export const formatLegalDocumentStatus = (status: string) => {
  if (!status) return '';
  const normalized = status.toUpperCase();
  return legalDocumentStatusTranslations[normalized] || genericStatusTranslations[normalized] || status;
};

export const formatGenericStatus = (status: string) => {
  if (!status) return '';
  const normalized = status.toUpperCase();
  return genericStatusTranslations[normalized] || status;
};

export const formatLegalDocumentType = (type: string) => {
  if (!type) return '';
  const normalized = type.toUpperCase();
  return legalDocumentTypeTranslations[normalized] || type;
};

export const formatDate = (dateString?: string | number[] | Date | null) => {
  if (!dateString) return '';
  
  try {
    let date: Date;
    if (Array.isArray(dateString)) {
      // Caso a API retorne LocalDateTime como array [yyyy, mm, dd, hh, mm]
      date = new Date(dateString[0], dateString[1] - 1, dateString[2], dateString[3] || 0, dateString[4] || 0);
    } else {
      date = new Date(dateString as any);
    }

    if (isNaN(date.getTime())) return 'Data inválida';

    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    }).format(date);
  } catch (e) {
    return 'Data inválida';
  }
};

export const formatDateTime = (dateString?: string | number[] | Date | null) => {
  if (!dateString) return '';
  
  try {
    let date: Date;
    if (Array.isArray(dateString)) {
      date = new Date(dateString[0], dateString[1] - 1, dateString[2], dateString[3] || 0, dateString[4] || 0);
    } else {
      date = new Date(dateString as any);
    }

    if (isNaN(date.getTime())) return 'Data inválida';

    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  } catch (e) {
    return 'Data inválida';
  }
};

export const formatTime = (dateString?: string | number[] | Date | null) => {
  if (!dateString) return '';
  
  try {
    let date: Date;
    if (Array.isArray(dateString)) {
      date = new Date(dateString[0], dateString[1] - 1, dateString[2], dateString[3] || 0, dateString[4] || 0);
    } else {
      date = new Date(dateString as any);
    }

    if (isNaN(date.getTime())) return '--:--';

    return new Intl.DateTimeFormat('pt-BR', {
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  } catch (e) {
    return '--:--';
  }
};
