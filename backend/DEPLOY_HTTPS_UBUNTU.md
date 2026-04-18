# Deploy HTTPS (Ubuntu) - Thiltapes Backend

Guia simples para subir o backend em HTTPS usando Cloudflare Tunnel (grátis, sem configuração de DNS).

Fluxo final deste projeto: deploy usa apenas `config/.env.production` como fonte de configuracao.

## 1. Pré-requisitos

- Servidor Ubuntu com acesso sudo
- Projeto clonado no servidor
- Docker e Docker Compose instalados
- Backend funcionando via `docker compose` na porta 3000
- Conta Cloudflare grátis (para gerar o túnel)

## 2. Criar Cloudflare Account e Gerar Túnel

1. Acesse https://dash.cloudflare.com
2. Crie uma conta grátis
3. Vá em **Zero Trust > Tunnels**
4. Clique em **Create a tunnel** e escolha um nome (ex: `thiltapes-prod`)
5. Na tela de instalação, copie o token do túnel

## 3. Usar Cloudflare Tunnel via Docker

Como o projeto já usa Docker Compose, o `cloudflared` também sobe como container.

## 4. Configurar o arquivo unico de producao

Edite `config/.env.production` e preencha os valores reais:

```dotenv
DB_PASSWORD=SEU_PASSWORD_REAL
DB_ADMIN_PASSWORD=SEU_PASSWORD_ADMIN_REAL
JWT_SECRET=SEU_JWT_SECRET_FORTE
CORS_ORIGIN=https://thiltapes-prod.trycloudflare.com
CLOUDFLARE_TUNNEL_TOKEN=SEU_TOKEN_DO_TUNNEL
```

## 5. Subir os containers

```bash
docker compose up -d --build
```

## 6. Garantir Backend Local

O backend deve responder dentro da rede do compose em `http://backend:3000`:

```bash
docker compose exec backend curl http://backend:3000/api/health
```

Se falhar, valide o serviço Node primeiro.

## 7. Configurar o Túnel

No painel Cloudflare (Zero Trust > Tunnels), após criar o túnel:

1. Clique em **Configure** no seu túnel `thiltapes-prod`
2. Na aba **Public Hostname**, clique em **Add a public hostname**
3. Preencha:
   - **Subdomain:** `thiltapes-prod` (ou qualquer nome)
   - **Domain:** `trycloudflare.com` (default) ou seu domínio próprio

- **Service type:** `HTTP`
- **Service URL:** `http://backend:3000`

4. Clique **Save**

Cloudflare vai gerar uma URL: `https://thiltapes-prod.trycloudflare.com`

## 8. Rodar o Túnel

Como o serviço `cloudflared` ja esta no `docker-compose.yml`, ele sobe junto com o projeto.

Verifique:

```bash
docker compose ps
docker compose logs -f cloudflared
```

Você vai ver na saída algo como:

```
INF Tunnel registered
INF Route registered: https://thiltapes-prod.trycloudflare.com
```

Copie essa URL — é a URL pública da sua API.

## 9. Testar a API

```bash
curl https://thiltapes-prod.trycloudflare.com/api/health
```

Deve retornar:

```json
{
  "status": "OK",
  "message": "Servidor está funcionando",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

## 10. Atualizar o Android para Usar Cloudflare

Edite `frontend/app/build.gradle.kts` e substitua a URL do `prod` flavor:

```kotlin
productFlavors {
    create("prod") {
        dimension = "environment"
        buildConfigField("String", "API_BASE_URL", "\"https://thiltapes-prod.trycloudflare.com/api/\"")
        manifestPlaceholders["usesCleartextTraffic"] = false
    }
}
```

## 11. Gerar APKs

No Android Studio ou terminal:

```bash
# Teste local com HTTP
./gradlew assembleDevDebug
# Gera: app/build/outputs/apk/devDebug/app-dev-debug.apk

# Produção com HTTPS (Cloudflare)
./gradlew assembleProdRelease
# Gera: app/build/outputs/apk/prodRelease/app-prod-release.apk
```

Instale o APK `prodRelease` no celular e teste.

## 12. Checklist Rápido (5 minutos)

- [ ] Conta Cloudflare criada
- [ ] `config/.env.production` preenchido com token e segredos reais
- [ ] `docker compose up -d --build` executado sem erros
- [ ] Túnel configurado no painel Cloudflare
- [ ] Backend responde em `http://backend:3000` dentro do compose
- [ ] Serviço `cloudflared` rodando via docker compose
- [ ] URL do túnel testada com curl
- [ ] `build.gradle.kts` atualizado com a URL do Cloudflare
- [ ] APK `prodRelease` gerado e instalado no celular
- [ ] App consegue conectar à API via Cloudflare

## 13. Parar o Túnel (Quando Terminar os Testes)

```bash
# Se estiver no docker compose:
docker compose down

# Deletar no painel Cloudflare:
# Zero Trust > Tunnels > Delete thiltapes-prod
```
