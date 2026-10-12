export const roleTranslations: Record<string, string> = {
  'PLATFORM_ADMIN': 'Administrador',
  'UNIVERSITY_ADMIN': 'Administrador de Universidade',
  'SCHOOL_DIRECTOR': 'Diretor de Escola',
  'STUDENT': 'Estudante',
};

export const formatRole = (role?: string) => {
  if (!role) return '';
  return roleTranslations[role] || role.replace('_', ' ');
};
