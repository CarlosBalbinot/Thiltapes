# Thiltapes - Frontend Android

Frontend nativo Android da aplicação Thiltapes construída com Kotlin, Jetpack Compose e Gradle.

## 📋 Pré-requisitos

- **Android Studio** 2024.1+
- **Android SDK** 34+ com suporte a API 36
- **JDK 21+** com suporte completo (incluindo `jlink`)
  - ⚠️ NÃO use apenas um JRE (Runtime)—Android Gradle Plugin precisa de um JDK completo

## 🚀 Configuração Inicial

### 1. Instalar Android Studio

Baixe e instale [Android Studio](https://developer.android.com/studio).

### 2. Configurar JDK para a Máquina

O Gradle precisa de um JDK com `jlink` para compilar. Configure seu próprio JDK em **`local.properties`**:

```properties
# local.properties (MÁQUINA-ESPECÍFICO - NÃO VERSIONE)
sdk.dir=C:\Users\SeuNome\AppData\Local\Android\Sdk
org.gradle.java.home=C:\Users\SeuNome\.jdks\temurin-21.0.5
```

**Opções de JDK:**

- **Android Studio JBR**: `C:\Program Files\Android\Android Studio\jbr`
- **Temurin (Recomendado)**: Download em https://adoptium.net/
- **Oracle JDK**: https://www.oracle.com/java/technologies/downloads/

**Verificar se seu JDK tem `jlink`:**

```bash
# Windows
dir "C:\Users\SeuNome\.jdks\temurin-21.0.5\bin\jlink.exe"

# Linux/Mac
ls /path/to/jdk/bin/jlink
```

Se `jlink.exe` existir, seu JDK está correto ✅

### 3. Sincronizar Gradle

Abra o projeto no Android Studio:

```bash
# Aguarde a sincronização automática ou execute:
./gradlew clean
./gradlew :app:assembleDebug
```

### 4. Compilar e Rodar

**Debug (emulador/dispositivo):**

```bash
./gradlew :app:installDebug
./gradlew :app:connectedAndroidTest
```

**Release:**

```bash
./gradlew :app:bundleRelease
```

## 📁 Estrutura do Projeto

```
frontend/
├── gradle.properties          # Configurações team-wide (VERSIONADO)
├── local.properties           # Configurações máquina-específicas (NÃO VERSIONE)
├── gradle/
│   ├── wrapper/               # Gradle Wrapper (versão centralizada)
│   └── libs.versions.toml     # Versões de dependências
├── app/
│   ├── build.gradle.kts       # Build config do app
│   ├── src/
│   │   ├── main/              # Código-fonte principal
│   │   ├── test/              # Testes unitários
│   │   └── androidTest/       # Testes de instrumentação
│   └── build/                 # Artifacts (ignorado pelo git)
└── README.md
```

## ⚙️ Configurações Importantes

### Versão do Gradle

Controlada por `gradle/wrapper/gradle-wrapper.properties`—todos os devs usam a mesma versão.

### Versões de Dependências

Em `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "1.9.20"
agp = "8.2.0"  # Android Gradle Plugin
```

**Para atualizar:**

1. Edite `gradle/libs.versions.toml`
2. Execute `./gradlew clean`
3. Sincronize no Android Studio

### JDK por Máquina

Cada dev configura seu próprio em `local.properties` (não versionado):

```properties
# local.properties
org.gradle.java.home=/seu/caminho/para/jdk-21
```

## 🧪 Testes

**Unitários (sem emulador):**

```bash
./gradlew :app:testDebugUnitTest
```

**De Instrumentação (emulador/dispositivo conectado):**

```bash
adb devices  # Verificar dispositivos/emuladores
./gradlew :app:connectedAndroidTest
```

## 🔍 Troubleshooting

### Erro: "jlink executable does not exist"

**Causa**: Seu JDK em `local.properties` é apenas um JRE (sem `jlink`).

**Solução**:

1. Verifique o caminho em `local.properties`
2. Confirme que `bin/jlink.exe` existe naquele JDK
3. Se não existe, instale um JDK completo (não JRE)

### Erro: "Could not find Android SDK"

**Causa**: `sdk.dir` em `local.properties` está errado ou SDK não está instalado.

**Solução**:

1. Abra Android Studio → Preferences → System Settings → Android SDK
2. Copie o SDK Path
3. Atualize `local.properties`:

```properties
sdk.dir=C:\seu\caminho\Android\Sdk
```

### Build lento

Limite a memória em `gradle.properties` se necessário:

```properties
# Padrão (2GB)
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8

# Menos memória (1GB)
org.gradle.jvmargs=-Xmx1024m -Dfile.encoding=UTF-8
```

### Cache corrompido

```bash
./gradlew clean
# Ou limpar completamente:
rm -rf build/
rm -rf .gradle/
./gradlew clean
```

## 📚 Referências

- [Android Gradle Plugin Docs](https://developer.android.com/build)
- [Gradle Documentation](https://gradle.org/releases/)
- [Temurin JDK](https://adoptium.net/)
- [JDK vs JRE](https://www.oracle.com/java/technologies/javase-jdk-install-overview.html)
