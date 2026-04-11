# Thiltapes - Frontend Android

Frontend mobile da aplicação Thiltapes construído em **Android Studio** com **Java**, **Retrofit** e **OkHttp**.

---

## 📱 Arquitetura

### Padrão de Comunicação

```
Activity (UI)
    ↓
Retrofit Client (ApiClient)
    ↓
HTTP Request (OkHttp)
    ↓
Backend API
    ↓
PostgreSQL
```

### Estrutura de Camadas

```
com.example.frontend/
├── api/
│   ├── ApiClient.java           # Singleton Retrofit
│   ├── ApiService.java          # Interface com endpoints
│   ├── ApiInterceptor.java      # Headers customizados + JWT
│   ├── ApiResponse.java         # Modelo padrão resposta
│   └── AppConfig.java           # URLs dev/prod
├── models/
│   └── [Seus modelos de dados]  # Futuro: User, Game, GameCard, etc
├── MainActivity.java            # Tela de status da API
└── [Suas Activities/Fragments]
```

---

## 🚀 Setup Inicial

### 1. Dependências (já adicionadas)

No `build.gradle.kts`:

```gradle
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
implementation("com.google.code.gson:gson:2.10.1")
```

### 2. Permissões (já adicionadas)

No `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 3. Verificar configuração

✅ `app/src/main/java/com/example/frontend/api/AppConfig.java`

- URL development: `http://10.0.2.2:3000/api` (emulador → PC)
- URL production: `https://api.thiltapes.com/api` (seu time atualizar)

---

## 📡 Como usar a API

### Padrão de resposta

Toda resposta da API segue este padrão:

**Sucesso:**

```json
{
  "success": true,
  "data": { ... },
  "message": "Descrição",
  "timestamp": "2026-04-11T16:15:30.123Z"
}
```

**Erro:**

```json
{
  "success": false,
  "error": "ERROR_TYPE",
  "message": "Descrição do erro",
  "timestamp": "2026-04-11T16:15:30.123Z"
}
```

### Exemplo 1: GET simples

```java
public class GamesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);

        // Obter instância do serviço
        ApiService apiService = ApiClient.getApiService();

        // Fazer chamada GET /api/games
        apiService.getGames().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        // ✅ Sucesso - processar dados
                        Object gamesData = apiResponse.getData();

                        // Converter para seu modelo (futuro)
                        // List<Game> games = gson.fromJson(gson.toJson(gamesData),
                        //     new TypeToken<List<Game>>(){}.getType());

                        updateUI(gamesData);
                    } else {
                        // ❌ Erro de negócio
                        showError("Erro: " + apiResponse.getError(),
                                 apiResponse.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // ❌ Erro de conexão
                showError("Conexão falhou", t.getMessage());
            }
        });
    }

    private void updateUI(Object games) {
        // Atualizar UI com dados
    }

    private void showError(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show();
    }
}
```

### Exemplo 2: POST com dados

```java
public class CreateGameActivity extends AppCompatActivity {
    private void createGame(String gameName) {
        ApiService apiService = ApiClient.getApiService();

        // Preparar dados
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("name", gameName);
        gameData.put("adminId", getCurrentUserId());

        // Fazer POST /api/games
        apiService.createGame(gameData).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        // ✅ Game criado
                        Object newGame = apiResponse.getData();
                        Toast.makeText(CreateGameActivity.this,
                            apiResponse.getMessage(),
                            Toast.LENGTH_SHORT).show();

                        // Voltar para lista de games
                        finish();
                    } else {
                        // ❌ Erro ao criar
                        Toast.makeText(CreateGameActivity.this,
                            apiResponse.getMessage(),
                            Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(CreateGameActivity.this,
                    "Erro: " + t.getMessage(),
                    Toast.LENGTH_LONG).show();
            }
        });
    }
}
```

### Exemplo 3: GET com parâmetros

```java
public class GameDetailsActivity extends AppCompatActivity {
    private void loadGameDetails(String gameId) {
        ApiService apiService = ApiClient.getApiService();

        // GET /api/games/:gameId
        apiService.getGame(gameId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        // Mostrar detalhes do game
                        showGameDetails(apiResponse.getData());
                    } else {
                        // Game não encontrado
                        showNotFound(apiResponse.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                showNetworkError(t);
            }
        });
    }
}
```

### Exemplo 4: POST com localização (futuro)

```java
public class GameMapActivity extends AppCompatActivity implements LocationListener {
    private void collectNearbyCard(String gameId, String cardId, double lat, double lng) {
        ApiService apiService = ApiClient.getApiService();

        // Preparar payload
        Map<String, Object> collectData = new HashMap<>();
        collectData.put("gameCardId", cardId);
        collectData.put("playerId", getCurrentPlayerId());
        collectData.put("lat", lat);
        collectData.put("lng", lng);

        // POST /api/games/:gameId/collect
        apiService.collectCard(gameId, collectData)
            .enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            // ✅ Card coletado!
                            showSuccessAnimation("Card coletado!");
                            removeCardFromMap(cardId);
                        } else {
                            // ❌ Não pode coletar
                            String reason = apiResponse.getMessage();
                            Toast.makeText(GameMapActivity.this, reason, Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    // Erro de conexão
                    showOfflineMode();
                }
            });
    }

    @Override
    public void onLocationChanged(Location location) {
        // GPS forneceu nova localização
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        // GET /api/games/:gameId/nearby?lat=X&lng=Y
        loadNearbyCards(getCurrentGameId(), lat, lng);
    }
}
```

---

## 🔐 Adicionar autenticação JWT (futuro)

### Backend: Middleware

```javascript
// src/middlewares/authMiddleware.js
export const requireAuth = (req, res, next) => {
  const token = req.headers.authorization?.split(" ")[1];

  if (!token) {
    return res
      .status(401)
      .json(errorResponse("UNAUTHORIZED", "Token não fornecido"));
  }

  // Validar token JWT
  // Se válido: req.user = decoded;
  // next();
};

// Em routes:
router.get("/", requireAuth, (req, res) => {
  // Protegido - apenas com token válido
});
```

### Android: TokenManager

```java
// app/src/main/java/com/example/frontend/api/TokenManager.java
public class TokenManager {
    private static TokenManager instance;
    private SharedPreferences prefs;

    public static TokenManager getInstance() {
        if (instance == null) {
            instance = new TokenManager();
        }
        return instance;
    }

    public void saveToken(String token) {
        // Salvar token criptografado
    }

    public String getToken() {
        // Retornar token salvo
        return null;
    }

    public void clearToken() {
        // Limpar token (logout)
    }
}
```

### Android: Atualizar ApiInterceptor

```java
// Já tem placeholder - apenas descomentar:
String token = TokenManager.getInstance().getToken();
if (token != null && !token.isEmpty()) {
    requestBuilder.header("Authorization", "Bearer " + token);
}
```

---

## 📊 Adicionar novos endpoints

### Passo 1: Backend - Criar rota

```javascript
// src/routes/inventory.js
import { Router } from "express";
import { successResponse } from "../utils/apiResponse.js";

const router = Router();

router.get("/me", async (req, res) => {
  // req.user contém dados do token JWT
  const inventory = []; // await cardsDoUsuario(req.user.id);

  res.status(200).json(successResponse(inventory, "Inventory carregado"));
});

export default router;
```

### Passo 2: Backend - Registrar rota

```javascript
// src/routes/index.js
import inventoryRouter from "./inventory.js";
router.use("/inventory", inventoryRouter);
```

### Passo 3: Android - Adicionar método

```java
// api/ApiService.java
@GET("inventory/me")
Call<ApiResponse> getMyInventory();
```

### Passo 4: Android - Usar em Activity

```java
ApiClient.getApiService().getMyInventory().enqueue(
    new Callback<ApiResponse>() {
        @Override
        public void onResponse(...) {
            // Processar resultado
        }

        @Override
        public void onFailure(...) {
            // Tratar erro
        }
    }
);
```

---

## 🔧 Configuração de Ambientes

### Development (Local)

**Backend rodando:**

```bash
npm run dev
# Server running on port 3000
```

**Android (AppConfig.java):**

```java
public static final String API_BASE_URL = "http://10.0.2.2:3000/api";
```

**Emulador:** Android pode acessar PC via `10.0.2.2`

### Production (Servidor)

**Backend em servidor remoto:**

```bash
# Seu time fará deploy
# https://api.thiltapes.com
```

**Android (AppConfig.java):**

```java
public static final String API_BASE_URL = "https://api.thiltapes.com/api";
```

**BuildConfig:** Muda automaticamente conforme `BuildConfig.DEBUG`

---

## 📊 Home Screen - Status da API

`MainActivity.java` mostra:

✅ **Status visual** (verde/vermelho/laranja)
✅ **URL configurada** (mostra qual API está sendo usada)
✅ **Ambiente** (debug/release)
✅ **Resposta completa** (JSON formatado)

Este é o **primeiro indicador** - se home conecta, resto funciona.

---

## 🐛 Troubleshooting

### "Falha de Conexão" na tela inicial

**Problema:** Emulador não consegue conectar ao backend

**Soluções:**

1. Backend está rodando? Veja `npm run dev`
2. URL em AppConfig.java está correta?
3. Testar via curl no PC:
   ```bash
   curl http://localhost:3000/api/health
   ```
4. Firewall bloqueando porta 3000?

### "Unable to resolve host 'api.thiltapes.com'"

**Problema:** Tentando conectar a servidor em produção que não existe ainda

**Solução:**

```java
// Em AppConfig.java, comente URL de produção por enquanto
public static final String API_BASE_URL = BuildConfig.DEBUG
    ? "http://10.0.2.2:3000/api"
    : "http://10.0.2.2:3000/api";  // Usar dev também
```

### "JSON to Java conversion failed"

**Problema:** Backend retorna formato diferente do esperado

**Solução:**

1. Verificar resposta com curl
2. Comparar com `ApiResponse.java`
3. Backend retorna sempre `{ success, data, message, error, timestamp }`?

### App congela em requisição

**Problema:** Requisição nunca retorna resposta

**Soluções:**

1. Verificar timeout em `AppConfig.API_TIMEOUT_SECONDS` (30 seg)
2. Backend respondendo?
3. Rede disponível?

---

## ✅ Checklist de Desenvolvimento

- [ ] Backend rodando: `npm run dev`
- [ ] Home conecta e mostra status ✅
- [ ] Endpoint GET /api/health testado via curl
- [ ] Novo endpoint criado em `src/routes/`
- [ ] Novo método adicionado em `ApiService.java`
- [ ] Activity/Fragment usando novo endpoint
- [ ] Testar em emulador
- [ ] Tratamento de erros implementado

---

## 📚 Referência Rápida

| Tarefa             | Arquivo                   | Método                                        |
| ------------------ | ------------------------- | --------------------------------------------- |
| Obter serviço API  | `api/ApiClient.java`      | `ApiClient.getApiService()`                   |
| Adicionar endpoint | `api/ApiService.java`     | `@GET/@POST/@PUT/@DELETE`                     |
| Fazer requisição   | Qualquer Activity         | `apiService.metodo().enqueue(callback)`       |
| Adicionar token    | `api/ApiInterceptor.java` | `requestBuilder.header("Authorization", ...)` |
| Mudar URL          | `api/AppConfig.java`      | `API_BASE_URL`                                |
| Ver resposta       | `api/ApiResponse.java`    | `Modelo com all fields`                       |

---

## 🚀 Próximas Fases

1. ✅ **Infraestrutura criada** - Retrofit, ApiClient, padrão de resposta
2. ⏳ **Endpoints implementados** - Games, Cards, Inventory
3. ⏳ **Autenticação JWT** - Login/Register
4. ⏳ **Geolocalização** - Mapa + Location listener
5. ⏳ **Deploy** - Server remoto + APK final

---

## 📖 Links úteis

- [Retrofit Docs](https://square.github.io/retrofit/)
- [OkHttp Docs](https://square.github.io/okhttp/)
- [Android Docs](https://developer.android.com/)
- [Gson Docs](https://github.com/google/gson)
- [Backend README](../backend/README.md)

---

**Gateway pronto! Bora expandir!** 🚀
