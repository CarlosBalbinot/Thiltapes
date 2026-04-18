FROM node:18-alpine

WORKDIR /app

# Copiar package.json
COPY package*.json ./

# Instalar dependências
RUN npm install

# Copiar código-fonte
COPY . .

# Expor porta 3000
EXPOSE 3000

# Comando padrão
CMD ["npm", "run", "start"]
