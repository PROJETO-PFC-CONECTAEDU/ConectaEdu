export const roleTranslations: Record<string, string> = {
  'PLATFORM_ADMIN': 'Administrador',
  'UNIVERSITY_ADMIN': 'Administrador de Universidade',
  'SCHOOL_DIRECTOR': 'Diretor de Escola',
  'STUDENT': 'Estudante',
};

export const demandStatusTranslations: Record<string, string> = {
  'WAITING': 'Inscrições Abertas',
  'ONGOING': 'Em Andamento',
  'FINISHED': 'Concluída',
  'ARCHIVED': 'Arquivada',
};

export const lessonStatusTranslations: Record<string, string> = {
  'SCHEDULED': 'Agendada',
  'CONFIRMED': 'Confirmada',
  'CANCELED': 'Cancelada',
  'REGISTERED': 'Registrada',
  'SIGNED': 'Assinada',
  'VALIDATED': 'Validada',
};

export const studentStatusTranslations: Record<string, string> = {
  'PENDING': 'Pendente',
  'VALIDATED': 'Validado',
  'REJECTED': 'Rejeitado',
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
  return roleTranslations[role] || role.replace('_', ' ');
};

export const formatDemandStatus = (status: string) => {
  return demandStatusTranslations[status] || status;
};

export const formatLessonStatus = (status: string) => {
  return lessonStatusTranslations[status] || status;
};

export const formatStudentStatus = (status: string) => {
  return studentStatusTranslations[status] || status;
};

export const formatLegalDocumentStatus = (status: string) => {
  return legalDocumentStatusTranslations[status] || status;
};

export const formatLegalDocumentType = (type: string) => {
  return legalDocumentTypeTranslations[type] || type;
};
