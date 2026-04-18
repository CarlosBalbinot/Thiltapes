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

## 7. Checklist Rápido (5 minutos)

- [ ] IP cadastrado no DuckDNS
- [ ] `config/.env.production` preenchido com o domínio e segredos reais
- [ ] Portas 80 e 443 liberadas na VM e no painel da Nuvem
- [ ] `docker compose up -d --build` executado sem erros
- [ ] Backend responde via HTTPS no navegador
- [ ] `build.gradle.kts` atualizado com a URL do DuckDNS
- [ ] APK `prodRelease` gerado e instalado no celular
