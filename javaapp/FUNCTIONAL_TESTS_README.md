# Suíte de Testes Funcionais - Tokugawa Discord Game

## 📋 Visão Geral

Esta suíte implementa testes funcionais **black box** para o Tokugawa Discord Game, utilizando:

- **Cucumber** com **Java** e **JUnit 5** para BDD (Behavior-Driven Development)
- **Testcontainers** para gerenciamento de containers Docker
- **WireMock** para simulação da API do Discord
- **Spring Boot Test** para integração com o framework

## 🏗️ Arquitetura

```
src/functionalTest/
├── java/io/github/disparter/tokugawa/discord/
│   ├── config/                     # Configurações de teste
│   │   └── FunctionalTestConfiguration.java
│   ├── steps/                      # Step Definitions do Cucumber
│   │   ├── AutenticacaoSteps.java
│   │   ├── DiscordMockSteps.java
│   │   ├── GerenciamentoCanalSteps.java
│   │   └── TestHooks.java
│   ├── context/                    # Contexto compartilhado
│   │   └── TestContext.java
│   ├── util/                       # Utilitários
│   │   └── JsonTemplateParser.java
│   ├── FunctionalTestRunner.java   # Runner principal
│   └── FunctionalTestApplication.java
└── resources/
    ├── features/                   # Arquivos .feature
    │   ├── autenticacao.feature
    │   └── gerenciamento-canais.feature
    ├── wiremock/                   # Templates WireMock
    │   └── __files/
    │       ├── discord-user-response.json
    │       ├── discord-channel-create-response.json
    │       ├── discord-message-response.json
    │       ├── discord-guild-response.json
    │       └── discord-error-response.json
    ├── docker/                     # Ambiente Docker
    │   ├── docker-compose.yml
    │   └── init-db.sql
    └── application-test.yml        # Configurações de teste
```

## 🚀 Executando os Testes

### Pré-requisitos

- **JDK 21+**
- **Docker** e **Docker Compose**
- **Gradle** (wrapper incluído)

### Comandos

```bash
# Executar apenas testes funcionais
./gradlew functionalTest

# Executar todos os testes (unitários + funcionais)
./gradlew clean test functionalTest

# Executar com relatórios detalhados
./gradlew functionalTest --info

# Executar cenários específicos por tag
./gradlew functionalTest -Dcucumber.filter.tags="@autenticacao"
```

### Variáveis de Ambiente

```bash
# Configurar para desenvolvimento local
export SPRING_PROFILES_ACTIVE=test
export TESTCONTAINERS_REUSE_ENABLE=true
export DISCORD_TOKEN=test-token-for-functional-tests
```

## 🐳 Ambiente Docker

### Containers Gerenciados

1. **PostgreSQL** (`postgres:15`) - Banco de dados de teste
2. **Redis** (`redis:7`) - Cache (opcional)
3. **WireMock** (`wiremock/wiremock:3.0.1`) - Mock da API Discord
4. **Aplicação** - Build da aplicação Tokugawa

### Gerenciamento Automático

Os containers são gerenciados automaticamente pelo **Testcontainers**:

- **Startup**: Containers iniciam antes dos testes
- **Health Checks**: Aguarda containers estarem prontos
- **Cleanup**: Remove containers após os testes
- **Reuse**: Reutiliza containers entre execuções (se habilitado)

## 🎭 Sistema de Mocking

### WireMock Dinâmico

O sistema utiliza **WireMock** com templates JSON dinâmicos:

```java
// Exemplo de uso no TestContext
Map<String, String> placeholders = new HashMap<>();
placeholders.put("userId", "123456789");
placeholders.put("username", "TestUser");

String mockResponse = jsonTemplateParser.parseTemplate(
    "wiremock/__files/discord-user-response.json", 
    placeholders
);
```

### Templates Disponíveis

- `discord-user-response.json` - Resposta de usuário Discord
- `discord-channel-create-response.json` - Criação de canal
- `discord-message-response.json` - Envio de mensagem
- `discord-guild-response.json` - Informações da guild
- `discord-error-response.json` - Respostas de erro

## 📝 Escrevendo Novos Testes

### 1. Criar Feature

```gherkin
# language: pt
Funcionalidade: Nova Funcionalidade
  Como um usuário
  Eu quero realizar uma ação
  Para atingir um objetivo

  Contexto:
    Dado o Discord está configurado corretamente

  Cenário: Cenário de sucesso
    Dado um usuário válido
    Quando ele executa uma ação
    Então o resultado deve ser positivo
```

### 2. Implementar Steps

```java
@Component
@Slf4j
public class NovaFuncionalidadeSteps {
    
    @Autowired
    private TestContext testContext;
    
    @Given("um usuário válido")
    public void umUsuarioValido() {
        // Implementação
    }
    
    @When("ele executa uma ação")
    public void eleExecutaUmaAcao() {
        // Implementação
    }
    
    @Then("o resultado deve ser positivo")
    public void oResultadoDeveSerPositivo() {
        // Verificações
    }
}
```

### 3. Configurar Mocks (se necessário)

```java
@Given("o Discord está esperando uma nova requisição")
public void mockDiscordNovaRequisicao() {
    Map<String, String> placeholders = new HashMap<>();
    placeholders.put("responseData", "valor dinâmico");
    
    String responseBody = jsonTemplateParser.parseTemplate(
        "wiremock/__files/novo-template.json", 
        placeholders
    );
    
    wireMockServer.stubFor(get(urlPathMatching("/api/novo-endpoint"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(responseBody)));
}
```

## 🏷️ Tags e Organização

### Tags Disponíveis

- `@autenticacao` - Testes de autenticação
- `@canais` - Gerenciamento de canais
- `@slow` - Testes que demoram mais para executar
- `@integration` - Testes de integração completa
- `@mock-heavy` - Testes com muito uso de mocks
- `@ignore` - Testes temporariamente desabilitados

### Executar por Tags

```bash
# Apenas testes de autenticação
./gradlew functionalTest -Dcucumber.filter.tags="@autenticacao"

# Excluir testes lentos
./gradlew functionalTest -Dcucumber.filter.tags="not @slow"

# Combinação de tags
./gradlew functionalTest -Dcucumber.filter.tags="@autenticacao and not @ignore"
```

## 📊 Relatórios

### Localizações dos Relatórios

- **Cucumber HTML**: `target/cucumber-reports/`
- **JUnit XML**: `target/cucumber-reports/Cucumber.xml`
- **Gradle Reports**: `build/reports/tests/functionalTest/`

### Visualização

```bash
# Abrir relatório HTML do Cucumber
open target/cucumber-reports/index.html

# Abrir relatório do Gradle
open build/reports/tests/functionalTest/index.html
```

## 🔧 Configuração e Customização

### Propriedades de Teste

Edite `src/functionalTest/resources/application-test.yml`:

```yaml
functional-test:
  timeout:
    default: 30s
    long: 60s
  retry:
    max-attempts: 3
    delay: 1s
```

### Configuração do WireMock

```yaml
wiremock:
  server:
    port: 8089
    verbose: true
    global-response-templating: true
```

## 🐛 Debugging

### Logs Detalhados

```bash
# Executar com logs DEBUG
./gradlew functionalTest --debug

# Logs específicos do Cucumber
./gradlew functionalTest -Dcucumber.options="--plugin pretty"
```

### Informações de Debug

Em caso de falha, os testes automaticamente capturam:

- Estado do `TestContext`
- Requisições/Respostas HTTP
- Interações com WireMock
- Logs detalhados da aplicação

### Reutilização de Containers

Para desenvolvimento mais rápido:

```bash
# Habilitar reutilização (containers não são removidos)
export TESTCONTAINERS_REUSE_ENABLE=true

# Limpar containers quando necessário
docker container prune -f
```

## 🚦 Integração Contínua

### GitHub Actions

O workflow `.github/workflows/ci.yml` executa automaticamente:

1. **Build** completo do projeto
2. **Testes Unitários** 
3. **Testes Funcionais**
4. **Relatórios** de cobertura e resultados
5. **Build Docker** (em PRs)
6. **Security Scan** (em PRs)

### Status dos Testes

- ✅ **Verde**: Todos os testes passaram
- ⚠️ **Amarelo**: Alguns testes falharam, mas build continuou
- ❌ **Vermelho**: Build falhou devido a erros críticos

## 🤝 Contribuindo

### Diretrizes

1. **Sempre** escreva testes em português claro
2. **Mantenha** os steps reutilizáveis entre features
3. **Use** o `TestContext` para compartilhar estado
4. **Documente** novos templates WireMock
5. **Teste** localmente antes de fazer PR

### Checklist para PRs

- [ ] Novos cenários têm steps implementados
- [ ] Templates WireMock são válidos
- [ ] Testes passam localmente
- [ ] Documentação atualizada se necessário
- [ ] Tags apropriadas adicionadas

## 📚 Recursos Adicionais

- [Cucumber Documentation](https://cucumber.io/docs/)
- [Testcontainers Guide](https://testcontainers.com/)
- [WireMock Documentation](https://wiremock.org/docs/)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)

---

**Nota**: Esta suíte implementa testes **100% black box** - nenhum conhecimento interno da aplicação é usado, apenas interfaces públicas (APIs REST, Discord, etc.).