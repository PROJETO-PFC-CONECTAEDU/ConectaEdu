# ConectaEdu

O **ConectaEdu** é uma plataforma educacional inovadora desenvolvida para integrar escolas, universidades, alunos e professores. O objetivo central é facilitar a gestão de demandas educacionais, agendamento de aulas, acompanhamento de estágios e a comunicação entre as instituições de ensino, promovendo um ecossistema colaborativo e eficiente.

## Objetivo

O projeto visa solucionar a fragmentação na comunicação entre escolas e universidades, oferecendo ferramentas que permitem:
- Gestão centralizada de instituições e seus membros.
- Facilitação do agendamento e realização de aulas.
- Acompanhamento detalhado de programas de estágio.
- Geração de relatórios de evolução e desempenho acadêmico.
- Uso de recomendações inteligentes para otimizar o aprendizado.

## Principais Funcionalidades

- **Dashboard Integrado**: Visão geral das atividades e métricas principais para administradores.
- **Gestão de Instituições**: Cadastro e gerenciamento de Escolas e Universidades.
- **Gestão de Usuários**: Perfis distintos para administradores de escola, administradores de universidade, professores e alunos.
- **Demandas Educacionais**: Criação, acompanhamento e atendimento de necessidades pedagógicas.
- **Agendamento de Aulas**: Sistema de calendário para organizar e gerenciar aulas e encontros.
- **Gestão de Estágios**: Controle de horas, documentação legal e acompanhamento de evolução do estagiário.
- **Relatórios**: Geração de documentos e visualizações de dados sobre o progresso acadêmico.
- **Sistema de Feedback e Avaliações**: Avaliação mútua entre participantes para melhoria contínua.
- **Mapa Interativo**: Localização geográfica das instituições parceiras.
- **Sistema de Notificações**: Alertas sobre agendamentos, novas demandas e feedbacks.

## Tecnologias Utilizadas

### Backend
- **Linguagem**: Java 21
- **Framework**: Spring Boot 4.1.0
- **Segurança**: Spring Security com autenticação JWT
- **Banco de Dados**: H2 (Desenvolvimento) / Oracle Database (Produção)
- **Documentação API**: Notion
- **Gestão de Dependências**: Maven

### Frontend
- **Framework**: React 19
- **Linguagem**: TypeScript
- **Build Tool**: Vite
- **Estilização**: Tailwind CSS
- **Gerenciamento de Estado/Queries**: TanStack Query (React Query)
- **Formulários**: React Hook Form com validação Zod
- **Ícones**: Lucide React

### Infraestrutura
- **Containerização**: Docker e Docker Compose
- **Servidor Web/Proxy**: Nginx

## Como Rodar Localmente

### Pré-requisitos
Certifique-se de ter instalado em sua máquina:
- **Java 21**
- **Node.js** (versão 18 ou superior)
- **Docker** e **Docker Compose**
- **Maven** (opcional, se não quiser usar o wrapper)

### Passo 1: Clonar o Repositório
```bash
git clone https://github.com/joaosalu22/ConectaEdu.git
cd ConectaEdu
```

### Passo 2: Configurar Variáveis de Ambiente
Crie um arquivo `.env` na pasta `api/` (para o Maven) e opcionalmente na raiz do projeto (para o Docker). Defina as variáveis necessárias:

```env
# URL do Frontend (CORS e links de redirecionamento)
APP_FRONTEND_URL=http://localhost:5173
# Caso o docker-compose peça em minúsculo:
app_frontend_url=http://localhost:5173

# Segurança
JWT_SECRET=sua_chave_secreta_aqui

# Banco de Dados (Se usar Oracle/Produção no Docker)
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
DB_URL=jdbc:oracle:thin:@seu_host:1521/seu_servico
```

*Nota: O arquivo `api/pom.xml` já está configurado com o `properties-maven-plugin` para carregar automaticamente o arquivo `.env` ao executar a aplicação via Maven.*

### Passo 3: Executar com Docker Compose (Recomendado)
O projeto está configurado para rodar facilmente via Docker.
1. Certifique-se de que o arquivo `.env` está na raiz do projeto.
2. Execute o comando:
```bash
docker-compose up -d
```
*Nota: As imagens no docker-compose estão configuradas para buscar do Docker Hub (`jjsalustiano/conectaedu...`). Para rodar build local, você precisaria alterar o docker-compose.*

### Passo 4: Executar Manualmente (Desenvolvimento)

#### Backend (API)
1. Navegue até a pasta da API:
```bash
cd api
```
2. Execute o projeto usando o Maven Wrapper:
```bash
./mvnw spring-boot:run
```
A API estará disponível em `http://localhost:8080`. O console do H2 pode ser acessado em `http://localhost:8080/h2-console`.

#### Frontend
1. Navegue até a pasta do frontend:
```bash
cd frontend
```
2. Instale as dependências:
```bash
npm install
```
3. Inicie o servidor de desenvolvimento:
```bash
npm run dev
```
O frontend estará disponível em `http://localhost:5173`.


