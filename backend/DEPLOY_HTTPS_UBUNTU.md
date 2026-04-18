# Deploy Thiltapes com HTTPS Gratuito (DuckDNS + Caddy)

Este guia usa o **DuckDNS** (subdomínio gratuito) e o **Caddy** (proxy reverso com HTTPS automático) para conectar o IP público `177.44.248.28` à internet de forma segura sem Cloudflare.

---

### Passo 1: Criar o Subdomínio no DuckDNS

1. Acesse [duckdns.org](https://www.duckdns.org/) e faça login (com Google, GitHub, etc).
2. Na caixa "sub domain", digite o nome que desejar (ex: `thiltapesgo`) e clique em **add domain**.
3. O painel mostrará o domínio. Garanta que o campo de IP esteja com o IP da sua VM: `177.44.248.28` (se não estiver, altere e clique em update ip).

### Passo 2: Configurar o Domínio no `.env.production`

1. Abra o arquivo `backend/config/.env.production`.
2. O final dele deve conter a variável `DOMAIN` com o domínio que você acabou de criar:

```env
DOMAIN=thiltapesgo.duckdns.org
```

### Passo 3: Liberar as Portas na VM (Importantíssimo)

O Caddy precisa gerar certificados (Let's Encrypt). Para isso, a sua VM no provedor de nuvem DEVE permitir tráfego nas portas **80** (HTTP) e **443** (HTTPS).
Comandos no Ubuntu (caso use UFW):

```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw reload
```

_(Lembre-se também de liberar na interface gráfica da nuvem de onde sua VM está hospedada, se houver painel de Firewall/Security Group lá)._

### Passo 4: Subir os Containers

Dentro da pasta `backend`, derrube os containers velhos e suba a nova estrutura:

```bash
docker compose down
docker compose up -d --build
```

> O container `caddy` subirá, conversará com a Let's Encrypt pelo seu IP público e forjará um certificado HTTPS válido em poucos segundos.

### Passo 5: Testar

Abra no navegador ou digite no terminal:

```bash
curl https://thiltapesgo.duckdns.org/api/health
```

### Passo 6: Atualizar o App Android

1. No arquivo `frontend/app/build.gradle.kts`, certifique-se de que a `API_BASE_URL` do ambiente `:prod` está apontando para o seu novo DuckDNS:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://thiltapesgo.duckdns.org/api/\"")
```

2. Gere o app apontando para a produção.

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
