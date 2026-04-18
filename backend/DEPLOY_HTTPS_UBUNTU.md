# Deploy HTTPS (Ubuntu) - Thiltapes Backend

Guia simples para subir o backend em HTTPS usando Cloudflare Tunnel (grátis, sem configuração de DNS).

## 1. Pré-requisitos

- Servidor Ubuntu com acesso sudo
- Projeto clonado no servidor
- Backend funcionando localmente na porta 3000
- Conta Cloudflare grátis (para gerar o túnel)

## 2. Criar Cloudflare Account e Gerar Túnel

1. Acesse https://dash.cloudflare.com
2. Crie uma conta grátis
3. Vá em **Zero Trust > Tunnels**
4. Clique em **Create a tunnel** e escolha um nome (ex: `thiltapes-prod`)
5. Na tela de instalação, copie o comando (`cloudflared service install YOUR_TOKEN_HERE`)

## 3. Instalar Cloudflare Tunnel

Na VM, execute:

```bash
curl -L --output cloudflared.tgz https://github.com/cloudflare/cloudflared/releases/download/2024.1.0/cloudflared-linux-amd64.tgz
tar -xzf cloudflared.tgz
sudo cp cloudflared /usr/local/bin/
sudo chmod +x /usr/local/bin/cloudflared
```

Verificar instalação:

```bash
cloudflared --version
```

## 4. Garantir Backend Local

O backend deve responder localmente em `127.0.0.1:3000`:

```bash
curl http://127.0.0.1:3000/api/health
```

Se falhar, valide o serviço Node primeiro.

## 5. Configurar o Túnel

No painel Cloudflare (Zero Trust > Tunnels), após criar o túnel:

1. Clique em **Configure** no seu túnel `thiltapes-prod`
2. Na aba **Public Hostname**, clique em **Add a public hostname**
3. Preencha:
   - **Subdomain:** `thiltapes-prod` (ou qualquer nome)
   - **Domain:** `trycloudflare.com` (default) ou seu domínio próprio
   - **Service:** `HTTP://127.0.0.1:3000`
4. Clique **Save**

Cloudflare vai gerar uma URL: `https://thiltapes-prod.trycloudflare.com`

## 6. Rodar o Túnel (Manual ou como Serviço)

**Opção A - Rodar manualmente (para testes):**

```bash
cloudflared tunnel run --token YOUR_TOKEN_FROM_CLOUDFLARE
```

**Opção B - Instalar como serviço (para deixar sempre ativo):**

```bash
sudo cloudflared service install YOUR_TOKEN_FROM_CLOUDFLARE
sudo systemctl restart cloudflared
sudo systemctl status cloudflared
```

Você vai ver na saída algo como:

```
INF Tunnel registered
INF Route registered: https://thiltapes-prod.trycloudflare.com
```

Copie essa URL — é a URL pública da sua API.

## 7. Testar a API

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

## 8. Atualizar o Android para Usar Cloudflare

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

## 9. Gerar APKs

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

## 10. Checklist Rápido (5 minutos)

- [ ] Conta Cloudflare criada
- [ ] Túnel configurado no painel Cloudflare
- [ ] Backend responde em `127.0.0.1:3000`
- [ ] Cloudflare Tunnel instalado e rodando
- [ ] URL do túnel testada com curl
- [ ] `build.gradle.kts` atualizado com a URL do Cloudflare
- [ ] APK `prodRelease` gerado e instalado no celular
- [ ] App consegue conectar à API via Cloudflare

## 11. Parar o Túnel (Quando Terminar os Testes)

```bash
# Se for serviço:
sudo systemctl stop cloudflared

# Deletar no painel Cloudflare:
# Zero Trust > Tunnels > Delete thiltapes-prod
```
