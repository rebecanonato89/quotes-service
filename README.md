# 🏥 Quotes Service - MVP Seguros

Serviço de cotações e apólices de seguros (AUTO e VIDA) construído com Kotlin + Spring Boot.

## 📋 Pré-requisitos

- Java 17+
- Maven 3.8+
- IntelliJ IDEA (recomendado) ou qualquer IDE com suporte Kotlin

## 🚀 Como Rodar

### 1. Compilar o projeto
```bash
mvn clean install
```

### 2. Rodar a aplicação
```bash
mvn spring-boot:run
```

Ou no Windows:
```powershell
.\run.ps1
```

### 3. Testar
```bash
mvn test
```

A aplicação estará disponível em: `http://localhost:8080`

## 📁 Estrutura do Projeto

```
src/main/kotlin/com/seguro/quotes/
├── QuotesServiceApplication.kt          # Main class
├── domain/
│   ├── enums/
│   │   ├── Coverage.kt                  # Enum de coberturas
│   │   ├── InsuranceType.kt             # AUTO | VIDA
│   │   ├── QuoteStatus.kt               # Status da cotação
│   │   └── PolicyStatus.kt              # Status da apólice
│   ├── model/
│   │   ├── VehicleData.kt               # Dados do veículo
│   │   ├── Quote.kt                     # Entidade cotação
│   │   └── Policy.kt                    # Entidade apólice
│   ├── validation/
│   │   └── QuoteValidator.kt            # Validações com Either
│   ├── service/
│   │   ├── PriceCalculator.kt           # Cálculo de preço
│   │   └── QuoteApprovalService.kt      # Aprovação/rejeição
│   ├── extensions/
│   │   └── QuoteExtensions.kt           # Extension functions
│   └── factory/
│       └── QuoteFactory.kt              # Factories
├── repository/
│   ├── QuoteRepository.kt               # Persistência quotes
│   └── PolicyRepository.kt              # Persistência policies
├── service/
│   ├── QuoteService.kt                  # Lógica de negócio quotes
│   ├── PolicyService.kt                 # Lógica de negócio policies
│   └── RiskScoreService.kt              # Simulação risk score
├── controller/
│   ├── QuoteController.kt               # REST endpoints quotes
│   └── PolicyController.kt              # REST endpoints policies
├── dto/
│   ├── QuoteRequest.kt                  # Request DTO
│   ├── QuoteResponse.kt                 # Response DTO
│   ├── PolicyResponse.kt                # Response DTO
│   └── ErrorResponse.kt                 # Error DTO
├── common/
│   ├── Either.kt                        # Either pattern
│   ├── DomainError.kt                   # Erros tipados
│   └── StringExtensions.kt              # Extensions úteis
├── infrastructure/
│   └── event/
│       └── EventPublisher.kt            # Publicador de eventos
└── config/
    └── EventConfig.kt                   # Configuração de eventos
```

## 🔧 Tecnologias

- **Kotlin** 1.9.22
- **Spring Boot** 3.2.2
- **Java** 17
- **Maven** (build tool)
- **Coroutines** (processamento assíncrono)
- **MockK** (testes)

## 📝 Regras de Negócio (MVP)

### Precificação
- **Base AUTO**: R$ 100
- **Base VIDA**: R$ 80
- **Coberturas**:
  - ROUBO: +R$ 20
  - COLISÃO: +R$ 30
  - ASSISTÊNCIA: +R$ 10
  - DANOS TERCEIROS: +R$ 25
- **Fator Idade (VIDA)**:
  - < 25 anos: +20%
  - 25-50 anos: sem alteração
  - > 50 anos: +30%

### Aprovação
- Preço ≤ R$ 300 → **APROVADO**
- Preço > R$ 300 → **REJEITADO** (razão: LIMIT_EXCEEDED)

### Validações
- **Documento**: obrigatório (não vazio)
- **Email**: se fornecido, deve conter "@"
- **AUTO**: veículo obrigatório (placa OU modelo)
- **VIDA**: idade obrigatória (≥ 18 anos)

## 🌐 API Endpoints

### Quotes

#### POST /quotes
Cria cotação, calcula preço e aprova/rejeita automaticamente.

**Request:**
```json
{
  "nome": "João Silva",
  "documento": "12345678900",
  "email": "joao@email.com",
  "tipoSeguro": "AUTO",
  "veiculo": {
    "placa": "ABC1234",
    "modelo": "Civic",
    "ano": 2020
  },
  "coberturas": ["ROUBO", "COLISAO"]
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "status": "APPROVED",
  "preco": 150.0,
  "razoesRejeicao": [],
  "timestamp": "2026-02-11T19:00:00"
}
```

#### GET /quotes/{id}
Consulta cotação por ID.

#### GET /quotes
Lista todas as cotações.

### Policies

#### POST /policies
Emite apólice a partir de cotação aprovada.

**Request:**
```json
{
  "quoteId": "uuid"
}
```

**Response 200:**
```json
{
  "id": "uuid",
  "quoteId": "uuid",
  "policyNumber": "POL-AUTO-20260211-1234",
  "status": "ACTIVE",
  "dataInicio": "2026-02-11",
  "dataFim": "2027-02-11"
}
```

#### GET /policies/{id}
Consulta apólice por ID.

## 🧪 Exemplos de Teste

### Cotação AUTO aprovada
```bash
curl -X POST http://localhost:8080/quotes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Maria Santos",
    "documento": "98765432100",
    "tipoSeguro": "AUTO",
    "veiculo": {"placa": "XYZ9876"},
    "coberturas": ["ROUBO"]
  }'
```

### Cotação VIDA com idade
```bash
curl -X POST http://localhost:8080/quotes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Pedro Oliveira",
    "documento": "11122233344",
    "tipoSeguro": "VIDA",
    "idade": 30,
    "coberturas": ["ASSISTENCIA"]
  }'
```

## ✅ Progresso das Tasks

- [x] Task 1: Modelar domínio completo
- [x] Task 2: Criar endpoints básicos (stubs)
- [x] Task 3: Implementar validação com Either
- [x] Task 4: Implementar precificação funcional
- [x] Task 5: Adicionar extensions e normalização
- [x] Task 6: Criar repositories in-memory
- [x] Task 7: Criar service layer
- [x] Task 8: Adicionar coroutines (risk score)
- [x] Task 9: Implementar eventos de domínio
- [ ] Task 10: Preparar design para microserviços

> Marque as tasks concluídas com [x].

## 🧠 Funcionalidades Adicionais e IA

- Validação automática de dados usando IA (documentos, emails, veículos)
- Precificação inteligente: modelos de IA para cálculo de preço com dados históricos e fatores de risco
- Integração com APIs de IA (Google GenAI) para análise de risco, geração de relatórios e respostas automáticas
- Automação de testes: geração de casos de teste e validação de respostas usando IA
- Rotas síncronas e assíncronas: endpoints REST tradicionais e endpoints com coroutines para processamento paralelo (async/sync)

## 🌟 Oportunidades de Expansão

- Recomendação de coberturas personalizadas com IA
- Análise preditiva de sinistros e fraudes
- Chatbot para atendimento e suporte ao cliente
- Dashboard inteligente para métricas e insights
- Expansão para outros tipos de seguros com precificação automatizada
- Integração com sistemas externos para validação de dados em tempo real
- Design para microserviços: separar domínios, escalabilidade, comunicação via eventos

## 📚 Próximos Passos

Siga o guia: `PROJETO_QUOTES_MVP_COMPLETO.md` na raiz do workspace para implementar a Task 10 e explorar as ideias avançadas.

## 📖 Documentação Útil

- [Kotlin Docs](https://kotlinlang.org/docs/home.html)
- [Spring Boot Kotlin](https://spring.io/guides/tutorials/spring-boot-kotlin/)
- [Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

## 📝 Licença

Projeto educacional - MVP de aprendizado Kotlin.

---

**Data de criação**: 11/02/2026
**Última atualização**: 17/02/2026
