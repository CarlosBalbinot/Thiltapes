# Setup Ubuntu VM - Thiltapes Backend

Guia para preparar a VM Ubuntu do zero até estar pronto para o deploy com Cloudflare Tunnel.

## O que Você Precisa Baixar na VM (Execute Isto Primeiro)

```bash
# 1. Atualizar sistema
sudo apt update && sudo apt upgrade -y

# 2. Instalar Docker e Docker Compose
sudo apt install -y docker.io docker-compose curl wget git

# 3. Adicionar seu usuário ao grupo docker (para não precisar sudo)
sudo usermod -aG docker $USER
newgrp docker

# 4. Verificar instalações
docker --version
docker-compose --version
```

## Estrutura da VM

Após clonar o projeto, sua VM vai ter:

```
/home/seu_usuario/Thiltapes/
├── backend/
│   ├── src/
│   ├── scripts/
│   ├── config/
│   ├── docker-compose.yml ✅ (já vem no repositório)
│   ├── Dockerfile ✅ (já vem no repositório)
│   ├── .env.production ✅ (já vem no repositório)
│   └── package.json
└── frontend/
    └── (Android app - não deve rodar na VM)
```

## 1. Clonar o Projeto

```bash
cd ~
git clone <seu_repo_do_thiltapes>
cd Thiltapes/backend
```

Os arquivos `Dockerfile`, `docker-compose.yml` e `.env.production` já estão no repositório, prontos para usar.

## 2. Rodar Docker Compose

```bash
cd ~/Thiltapes/backend

# Build das imagens + Start dos containers
docker-compose up -d

# Ver logs para confirmação
docker-compose logs -f backend

# Pressione Ctrl+C para parar de ver logs (containers continuam rodando)
```

## 3. Verificar Se Funcionou

```bash
# Dentro da VM
curl http://127.0.0.1:3000/api/health

# Deve retornar:
# {"status":"OK","message":"Servidor está funcionando","timestamp":"2024-01-15T10:30:00.000Z"}
```

## 4. Rodar Migrations (Primeira Vez)

```bash
# Entrar no container do backend
docker-compose exec backend bash

# Dentro do container
npm run typeorm:migrate

# Sair
exit
```

## 5. Checklist Antes do Deploy com Cloudflare

- [ ] `docker --version` funciona
- [ ] `docker-compose --version` funciona
- [ ] Projeto clonado em `~/Thiltapes/backend`
- [ ] `docker-compose up -d` subiu sem erros
- [ ] `curl http://127.0.0.1:3000/api/health` retorna OK
- [ ] Migrations rodaram (`npm run typeorm:migrate`)

## 6. Próximo Passo

Quando tudo funcionar, vá para [DEPLOY_HTTPS_UBUNTU.md](./DEPLOY_HTTPS_UBUNTU.md) e configure o Cloudflare Tunnel.

## Troubleshooting

**Porta 3000 já em uso?**

```7. bash
sudo lsof -i :3000
```

**Banco não conecta?**

```bash
docker-compose logs postgres
```

**Permissão negada no docker?**

```bash
sudo usermod -aG docker $USER
# Desconecte e reconecte a sessão SSH
```

**Quer resetar tudo e começar de novo?**

```bash
docker-compose down -v
docker-compose up -d
```
