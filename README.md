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
- **Gestão de Estágios**: Controle rigoroso de horas (pendentes, validadas, totais), documentação legal (Termos de Compromisso) e acompanhamento de evolução do estagiário.
- **Relatórios Detalhados**: Geração de visualizações de dados sobre progresso acadêmico, horas acumuladas, avaliações de desempenho e métricas de presença.
- **Sistema de Feedback e Avaliações**: Avaliação mútua (escola/estudante) com critérios de conhecimento, pontualidade, didática e comprometimento.
- **Mapa Interativo**: Localização geográfica das instituições parceiras integrada com Google Maps API.

## Tecnologias Utilizadas

### Backend
- **Linguagem**: Java 21
- **Framework**: Spring Boot 3.3.5
- **Segurança**: Spring Security com autenticação JWT e MFA (Google Authenticator)
- **Envio de E-mail**: Spring Mail (SMTP)
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

## Perfis de Execução

O sistema utiliza perfis do Spring para gerenciar diferentes ambientes:
- **`dev`**: Voltado para desenvolvimento e testes rápidos. Utiliza banco de dados **H2** (em memória) e o serviço **Mailtrap** para testes de envio de e-mail.
- **`prod`**: Configurado para ambiente de produção. Utiliza **Oracle Database** e requer configurações completas de e-mail e segurança.

---

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

O projeto utiliza variáveis de ambiente para configurações sensíveis. 
- Para execução via **Maven**: Crie um arquivo `.env` na pasta `api/`.
- Para execução via **Docker**: Crie um arquivo `.env` na raiz do projeto.
- Para o **Frontend** (opcional no dev): Crie um arquivo `.env` na pasta `frontend/`.

#### Exemplo de `.env` (API):
```env
# URL do Frontend (CORS e links de redirecionamento)
APP_FRONTEND_URL=http://localhost:5173

# Segurança
JWT_SECRET=sua_chave_secreta_aqui

# Banco de Dados (Se usar Oracle/Produção)
DB_USER=seu_usuario
DB_PASSWORD=sua_senha
DB_URL=jdbc:oracle:thin:@seu_host:1521/seu_servico

# Configuração de E-mail (Produção: Gmail / Dev: Mailtrap)
APP_USERNAME=seu_usuario_ou_email
APP_PASSWORD=sua_senha_ou_token
```

#### Exemplo de `.env` (Frontend):
```env
VITE_API_URL=http://localhost:8080/api/v1
VITE_GOOGLE_MAPS_API_KEY=sua_chave_api_google_maps
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
2. Execute o projeto ativando o perfil de desenvolvimento (`dev`):
```bash
# Usando o Maven Wrapper
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
*Nota: O perfil `dev` utiliza o banco de dados H2 em memória e o Mailtrap para e-mails (configurável via variáveis de ambiente no `.env`).*

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

### Passo 5: Configuração para Produção (Oracle Autonomous)
Para rodar em produção com Oracle Autonomous Database:
1. Certifique-se de que a **Wallet** do banco de dados está na pasta `/home/opc/app/wallet` no servidor.
2. O arquivo `.env` deve conter as credenciais e a URL correta (ex: `jdbc:oracle:thin:@nome_db_high?TNS_ADMIN=/app/wallet`).
3. As dependências de segurança (PKI) já estão incluídas no `pom.xml`.

## Estrutura do Projeto

```
ConectaEdu/
├── api/                # Backend Spring Boot
│   ├── src/
│   │   └── main/java/  # Código fonte Java (módulos)
│   └── pom.xml         # Dependências Maven
├── frontend/           # Frontend React + Vite
│   ├── src/
│   │   ├── components/ # Componentes reutilizáveis
│   │   ├── pages/      # Páginas da aplicação
│   │   └── lib/        # Configurações de API/Axios
│   └── package.json    # Dependências Node.js
└── docker-compose.yml  # Orquestração de containers
```


