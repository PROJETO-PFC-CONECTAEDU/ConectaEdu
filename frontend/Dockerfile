# Estágio 1: Build
FROM node:20-alpine AS build-stage
WORKDIR /app

# Instala as dependências primeiro para aproveitar o cache das camadas do Docker
COPY package*.json ./
RUN npm install

# Copia o restante dos arquivos
COPY . .

#antes do comando de build (npm run build):
ARG VITE_GOOGLE_MAPS_API_KEY
ENV VITE_GOOGLE_MAPS_API_KEY=$VITE_GOOGLE_MAPS_API_KEY

RUN npm run build

# Estágio 2: Produção com Nginx
FROM nginx:stable-alpine

# Copia a configuração customizada do Nginx (criaremos abaixo)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copia os arquivos compilados (da pasta dist do Vite)
COPY --from=build-stage /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]