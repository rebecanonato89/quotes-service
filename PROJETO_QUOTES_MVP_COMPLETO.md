# 🎯 Projeto MVP: Quotes e Apólices - 10 Tasks Progressivas

**Objetivo**: Aprender Kotlin construindo um MVP real de seguros, aplicando conscientemente os **13 conceitos** ao longo das tarefas.

**Baseado em**: Requisitos reais do MVP (Alice) + 10 tasks progressivas

---

## 📋 Requisitos do MVP

### Domínio

#### QuoteRequest
- `nome` (String, obrigatório)
- `documento` (String, obrigatório - formato livre mas não vazio)
- `email` (String?, opcional - se vier, deve conter "@")
- `tipoSeguro` (AUTO | VIDA, obrigatório)
- `idade` (Int?, opcional - obrigatório se VIDA, deve ser >= 18)
- `veiculo` (VehicleData?, opcional - obrigatório se AUTO)
- `cep` (String?, opcional)
- `coberturas` (List<Coverage>, pode ser vazia)

#### Quote
- `id` (UUID)
- `status` (CREATED | PRICED | REJECTED | APPROVED | EXPIRED)
- `preco` (Double?)
- `razoesRejeicao` (List<String>?, se REJECTED)
- `timestamp` (LocalDateTime)
- `request` (QuoteRequest - dados originais)

#### Policy
- `id` (UUID)
- `quoteId` (UUID)
- `status` (ACTIVE | CANCELLED)
- `dataInicio` (LocalDate)
- `dataFim` (LocalDate)

---

### Regras de Negócio

#### Validação
1. **documento** obrigatório (não vazio)
2. **tipoSeguro** obrigatório
3. Se `tipoSeguro = AUTO` → `veiculo.placa` OU `veiculo.modelo` obrigatório
4. Se `tipoSeguro = VIDA` → `idade` obrigatória E >= 18
5. **email**, se fornecido, deve conter "@" (validação básica)

#### Precificação (algoritmo simples)
- **Base por tipo**: AUTO = 100, VIDA = 80
- **Add por cobertura**:
  - ROUBO: +20
  - COLISAO: +30
  - ASSISTENCIA: +10
  - DANOS_TERCEIROS: +25
- **Fator idade (VIDA)**:
  - < 25 anos: +20%
  - 25-50 anos: +0%
  - > 50 anos: +30%

#### Aprovação
- Se **preço final > 300** → status `REJECTED` com razão `LIMIT_EXCEEDED`
- Caso contrário → status `APPROVED`

---

### API Endpoints

```
POST   /quotes              # Cria, precifica e aprova/rejeita
GET    /quotes/{id}         # Consulta status e preço
POST   /policies            # Emite apólice de quote APPROVED
GET    /policies/{id}       # Consulta apólice
```

---

### Persistência
- **MVP**: In-memory (ConcurrentHashMap)
- **Futuro**: PostgreSQL

---

### Observabilidade
- Logs **sanitizados**: mascarar documento parcialmente
- Ex: `123.456.789-00` → `***.***.789-**`

---

## 🗺️ 10 Tasks Progressivas (Backlog)

### Task 1: Modelar Domínio e Contratos ✅ Conceitos 1, 2, 3
### Task 2: Criar Endpoints Básicos (Stubs) ✅ Conceitos 11, 12
### Task 3: Validação com Either ✅ Conceito 9
### Task 4: Precificação Funcional ✅ Conceitos 4, 7, 8
### Task 5: Normalização e Extensions ✅ Conceitos 5, 6
### Task 6: Persistência In-Memory ✅ Conceito 11 (Interop Java)
### Task 7: Camada de Serviço + Boas Práticas ✅ Conceito 12, 13
### Task 8: Processamento Assíncrono ✅ Conceito 10 (Coroutines)
### Task 9: Eventos de Domínio ✅ Conceito 4 (Lambdas)
### Task 10: Design para Microserviços ✅ Conceitos 12, 13

---

# 🚀 TASK 1: Modelar Domínio e Contratos

### 📚 Conceitos Aplicados
- ✅ **Conceito 1**: Val vs Var
- ✅ **Conceito 2**: Null Safety
- ✅ **Conceito 3**: Data Classes

### 🎯 Objetivo
Criar enums, data classes e DTOs que representam o domínio completo do MVP.

---

## Subtask 1.1: Criar Enums

### Coverage.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/domain/enums/Coverage.kt`

**Pense:**
Quais coberturas oferecemos? Cada uma tem um custo adicional.

```kotlin
package com.seguro.quotes.domain.enums

enum class Coverage(val additionalCost: Double) {
    ROUBO(20.0),
    COLISAO(30.0),
    ASSISTENCIA(10.0),
    DANOS_TERCEIROS(25.0)
}
```

**Perguntas:**
1. Por que `additionalCost` é `val` dentro do enum?
2. Como acessar o custo: `Coverage.ROUBO.additionalCost`
3. Poderia ser `var`? Deveria?

---

### InsuranceType.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/domain/enums/InsuranceType.kt`

```kotlin
package com.seguro.quotes.domain.enums

enum class InsuranceType(val basePrice: Double) {
    AUTO(100.0),
    VIDA(80.0)
}
```

**Por que armazenar `basePrice` no enum?**
- Single source of truth
- Facilita precificação depois
- Evita when/if espalhados

---

### QuoteStatus.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/domain/enums/QuoteStatus.kt`

**Exercício para você:**
```kotlin
package com.seguro.quotes.domain.enums

// TODO: Implemente o enum com os status do MVP
enum class QuoteStatus {
    CREATED,      // Acabou de ser criada
    PRICED,       // Preço calculado
    REJECTED,     // Rejeitada (preço > 300)
    APPROVED,     // Aprovada (preço <= 300)
    EXPIRED       // Expirada (após X dias sem emitir apólice)
}
```

**Diferenças do guia original:**
- ✅ Adicionado `CREATED` (antes do cálculo)
- ✅ Adicionado `PRICED` (intermediário)
- ❌ Removido `DRAFT` e `PENDING` (simplificado)

---

### PolicyStatus.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/domain/enums/PolicyStatus.kt`

**Exercício para você:**
```kotlin
package com.seguro.quotes.domain.enums

// TODO: Implemente
enum class PolicyStatus {
    ACTIVE,       // Vigente
    CANCELLED     // Cancelada pelo cliente
}
```

---

## Subtask 1.2: Modelar Dados de Veículo

### VehicleData.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/domain/model/VehicleData.kt`

**Conceitos aplicados:**
- ✅ Data class
- ✅ Null safety (placa OU modelo obrigatório)

```kotlin
package com.seguro.quotes.domain.model

// Dados do veículo (obrigatório para AUTO)
data class VehicleData(
    val placa: String?,   // Pode ser null SE modelo for fornecido
    val modelo: String?,  // Pode ser null SE placa for fornecida
    val ano: Int? = null  // Opcional
) {
    // Validação: pelo menos um dos dois deve existir
    fun isValid(): Boolean = !placa.isNullOrBlank() || !modelo.isNullOrBlank()
}
```

**Perguntas:**
1. Por que ambos são nullable se um é obrigatório?
   - **R**: Porque a obrigatoriedade é "OU", não "E"
2. Por que validação dentro da data class?
   - **R**: Encapsulamento - a classe conhece suas regras
3. O que é `isNullOrBlank()`?
   - **R**: Extension do Kotlin que verifica null OU string vazia/whitespace

---

## Subtask 1.3: Modelar QuoteRequest (DTO)

### QuoteRequest.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/dto/QuoteRequest.kt`

**Conceitos aplicados:**
- ✅ Data class
- ✅ Null safety (campos condicionalmente obrigatórios)
- ✅ Val (request é imutável)

```kotlin
package com.seguro.quotes.dto

import com.seguro.quotes.domain.enums.Coverage
import com.seguro.quotes.domain.enums.InsuranceType
import com.seguro.quotes.domain.model.VehicleData
import javax.validation.constraints.*

data class QuoteRequest(
    @field:NotBlank(message = "Nome é obrigatório")
    val nome: String,
    
    @field:NotBlank(message = "Documento é obrigatório")
    val documento: String, // CPF ou CNPJ (formato livre)
    
    val email: String? = null, // Opcional, mas se vier deve ter "@"
    
    @field:NotNull(message = "Tipo de seguro é obrigatório")
    val tipoSeguro: InsuranceType,
    
    // Obrigatório se VIDA
    @field:Min(value = 18, message = "Idade mínima: 18 anos")
    val idade: Int? = null,
    
    // Obrigatório se AUTO
    @field:Valid // Valida internamente VehicleData
    val veiculo: VehicleData? = null,
    
    val cep: String? = null,
    
    val coberturas: List<Coverage> = emptyList() // Pode ser vazia
)
```

**Perguntas reflexivas:**
1. Por que `idade` é `Int?` e não `Int`?
   - **R**: Só é obrigatório para VIDA, para AUTO é irrelevante
2. Por que `coberturas` tem valor padrão `emptyList()`?
   - **R**: Cliente pode não escolher nenhuma cobertura extra
3. O que faz `@field:Valid`?
   - **R**: Dispara validação do objeto aninhado `VehicleData`

---

## Subtask 1.4: Modelar Quote (Entidade de Domínio)

### Quote.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/domain/model/Quote.kt`

```kotlin
package com.seguro.quotes.domain.model

import com.seguro.quotes.domain.enums.QuoteStatus
import com.seguro.quotes.dto.QuoteRequest
import java.time.LocalDateTime
import java.util.UUID

data class Quote(
    val id: UUID = UUID.randomUUID(),
    val status: QuoteStatus,
    val preco: Double? = null, // Null enquanto não precificado
    val razoesRejeicao: List<String> = emptyList(),
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val request: QuoteRequest // Dados originais do request
) {
    // Helper: verificar se pode gerar apólice
    fun canGeneratePolicy(): Boolean = 
        status == QuoteStatus.APPROVED && preco != null
    
    // Helper: verificar se expirada (7 dias sem emitir apólice)
    fun isExpired(): Boolean = 
        timestamp.isBefore(LocalDateTime.now().minusDays(7))
}
```

**Diferenças do guia original:**
- ✅ `preco` é nullable (antes de calcular é null)
- ✅ `razoesRejeicao` lista ao invés de campo único
- ✅ Contém `request` completo (auditoria)

**Perguntas:**
1. Por que `request` é `val` e não cópia dos campos?
   - **R**: Mantém dados originais intactos (auditoria)
2. Por que `preco` é nullable?
   - **R**: Status `CREATED` ainda não tem preço calculado
3. O que são as funções `canGeneratePolicy()` e `isExpired()`?
   - **R**: Regras de negócio encapsuladas na entidade (DDD)

---

## Subtask 1.5: Modelar Policy (Entidade de Domínio)

### Policy.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/domain/model/Policy.kt`

**Exercício para você:**
```kotlin
package com.seguro.quotes.domain.model

import com.seguro.quotes.domain.enums.PolicyStatus
import java.time.LocalDate
import java.util.UUID

// TODO: Implemente a data class Policy
data class Policy(
    val id: UUID = UUID.randomUUID(),
    val quoteId: UUID, // Referência à cotação
    val status: PolicyStatus,
    val dataInicio: LocalDate,
    val dataFim: LocalDate,
    // TODO: Adicione timestamp de emissão (LocalDateTime)
    // TODO: Adicione número da apólice (policyNumber: String)
) {
    // TODO: Implemente função para verificar se está vigente
    // fun isActive(): Boolean = ...
}
```

**Dicas:**
- `dataInicio` geralmente é "hoje"
- `dataFim` geralmente é +1 ano
- `policyNumber` formato: `POL-{tipoSeguro}-{timestamp}-{random}`

---

## Subtask 1.6: Criar DTOs de Response

### QuoteResponse.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/dto/QuoteResponse.kt`

```kotlin
package com.seguro.quotes.dto

import com.seguro.quotes.domain.enums.QuoteStatus
import com.seguro.quotes.domain.model.Quote
import java.time.LocalDateTime
import java.util.UUID

data class QuoteResponse(
    val id: UUID,
    val status: QuoteStatus,
    val preco: Double?,
    val razoesRejeicao: List<String>,
    val timestamp: LocalDateTime
) {
    companion object {
        // Factory method: converter Quote → QuoteResponse
        fun from(quote: Quote): QuoteResponse = QuoteResponse(
            id = quote.id,
            status = quote.status,
            preco = quote.preco,
            razoesRejeicao = quote.razoesRejeicao,
            timestamp = quote.timestamp
        )
    }
}
```

**Por que separar Request/Response/Domain?**
- **Request**: O que o cliente envia
- **Domain**: Como armazenamos internamente (+ lógica)
- **Response**: O que o cliente recebe (sem dados sensíveis)

---

### PolicyResponse.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/dto/PolicyResponse.kt`

**Exercício para você:**
```kotlin
package com.seguro.quotes.dto

import com.seguro.quotes.domain.model.Policy
import com.seguro.quotes.domain.enums.PolicyStatus
import java.time.LocalDate
import java.util.UUID

// TODO: Implemente PolicyResponse
data class PolicyResponse(
    val id: UUID,
    val quoteId: UUID,
    val policyNumber: String,
    val status: PolicyStatus,
    val dataInicio: LocalDate,
    val dataFim: LocalDate
) {
    companion object {
        // TODO: Implemente from(policy: Policy): PolicyResponse
    }
}
```

---

### 🎓 Checkpoint Task 1

**Estrutura esperada:**
```
src/main/kotlin/com/seguro/quotes/
├── domain/
│   ├── enums/
│   │   ├── Coverage.kt              ✅
│   │   ├── InsuranceType.kt         ✅
│   │   ├── QuoteStatus.kt           ✅
│   │   └── PolicyStatus.kt          ✅
│   └── model/
│       ├── VehicleData.kt           ✅
│       ├── Quote.kt                 ✅
│       └── Policy.kt                ✅
└── dto/
    ├── QuoteRequest.kt              ✅
    ├── QuoteResponse.kt             ✅
    └── PolicyResponse.kt            ✅
```

**Perguntas de autoavaliação:**
1. ✅ Sei quando usar `val` vs `var`?
2. ✅ Entendo por que `idade` e `veiculo` são nullable?
3. ✅ Sei o que data class gera automaticamente?
4. ✅ Por que separamos Request/Domain/Response?
5. ✅ Entendo por que enums têm propriedades (`basePrice`, `additionalCost`)?

**Próxima task**: Controllers básicos (stubs)

---

# 🚀 TASK 2: Criar Endpoints Básicos (Stubs)

### 📚 Conceitos Aplicados
- ✅ **Conceito 11**: Interop Java-Kotlin (Spring annotations)
- ✅ **Conceito 12**: Boas Práticas (package structure)

### 🎯 Objetivo
Criar controllers REST que recebem/retornam dados, mas ainda **sem lógica complexa** (stubs).

---

## Subtask 2.1: QuoteController (Stubs)

**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/controller/QuoteController.kt`

```kotlin
package com.seguro.quotes.controller

import com.seguro.quotes.dto.QuoteRequest
import com.seguro.quotes.dto.QuoteResponse
import com.seguro.quotes.domain.model.Quote
import com.seguro.quotes.domain.enums.QuoteStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.validation.Valid

@RestController
@RequestMapping("/quotes")
class QuoteController {
    
    // Persistência temporária (in-memory)
    private val quotes = ConcurrentHashMap<UUID, Quote>()
    
    // POST /quotes - Criar cotação (stub)
    @PostMapping
    fun createQuote(
        @Valid @RequestBody request: QuoteRequest
    ): ResponseEntity<QuoteResponse> {
        // TODO: Por enquanto, apenas cria com status CREATED
        val quote = Quote(
            status = QuoteStatus.CREATED,
            request = request
        )
        
        quotes[quote.id] = quote
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(QuoteResponse.from(quote))
    }
    
    // GET /quotes/{id} - Consultar cotação
    @GetMapping("/{id}")
    fun getQuote(@PathVariable id: UUID): ResponseEntity<QuoteResponse> {
        val quote = quotes[id] 
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(QuoteResponse.from(quote))
    }
}
```

**O que está faltando (propositalmente):**
- ❌ Validações complexas (Task 3)
- ❌ Cálculo de preço (Task 4)
- ❌ Repository real (Task 6)
- ❌ Service layer (Task 7)

**Perguntas:**
1. O que faz `@Valid`?
   - **R**: Dispara validações do Bean Validation (`@NotBlank`, etc.)
2. Por que `ConcurrentHashMap` e não `HashMap`?
   - **R**: Thread-safe (múltiplas requisições simultâneas)
3. O que é `?:` (Elvis operator)?
   - **R**: Retorna valor à direita se esquerda for null

---

## Subtask 2.2: PolicyController (Stubs)

**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/controller/PolicyController.kt`

**Exercício para você:**
```kotlin
package com.seguro.quotes.controller

import com.seguro.quotes.dto.PolicyResponse
import com.seguro.quotes.domain.model.Policy
import com.seguro.quotes.domain.enums.PolicyStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/policies")
class PolicyController(
    // TODO: Injetar QuoteController ou compartilhar repositório
    // Por enquanto, aceite quoteId no body
) {
    private val policies = ConcurrentHashMap<UUID, Policy>()
    
    // POST /policies - Emitir apólice (stub)
    @PostMapping
    fun issuePolicy(@RequestBody body: Map<String, String>): ResponseEntity<PolicyResponse> {
        val quoteId = UUID.fromString(body["quoteId"])
        
        // TODO: Buscar quote e validar status (Task 7)
        // Por enquanto, apenas cria policy
        val policy = Policy(
            quoteId = quoteId,
            status = PolicyStatus.ACTIVE,
            dataInicio = LocalDate.now(),
            dataFim = LocalDate.now().plusYears(1),
            // TODO: Adicione policyNumber gerado
        )
        
        policies[policy.id] = policy
        
        return ResponseEntity.ok(PolicyResponse.from(policy))
    }
    
    // GET /policies/{id} - Consultar apólice
    @GetMapping("/{id}")
    fun getPolicy(@PathVariable id: UUID): ResponseEntity<PolicyResponse> {
        // TODO: Implemente busca no map
        return ResponseEntity.notFound().build()
    }
}
```

---

### 🎓 Checkpoint Task 2

**Teste manual com curl/Postman:**

```bash
# Criar cotação
POST http://localhost:8080/quotes
Content-Type: application/json

{
  "nome": "João Silva",
  "documento": "12345678900",
  "email": "joao@email.com",
  "tipoSeguro": "AUTO",
  "veiculo": {
    "placa": "ABC1234",
    "modelo": null
  },
  "coberturas": ["ROUBO", "COLISAO"]
}

# Consultar cotação
GET http://localhost:8080/quotes/{id-retornado}

# Emitir apólice
POST http://localhost:8080/policies
Content-Type: application/json

{
  "quoteId": "{id-da-quote}"
}
```

**O que deve funcionar:**
- ✅ Criar quote (retorna 201 com id)
- ✅ Consultar quote por id
- ✅ Emitir policy
- ✅ Consultar policy por id

**O que NÃO funciona ainda:**
- ❌ Validações complexas (idade, veículo)
- ❌ Cálculo de preço
- ❌ Rejeição automática

**Próxima task**: Validação com Either

---

# 🚀 TASK 3: Validação com Either

### 📚 Conceitos Aplicados
- ✅ **Conceito 9**: Either Pattern (erros sem exceptions)

### 🎯 Objetivo
Implementar validações complexas retornando `Either<DomainError, T>` ao invés de lançar exceptions.

---

## Subtask 3.1: Criar Either e DomainError

### Either.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/common/Either.kt`

```kotlin
package com.seguro.quotes.common

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()
    
    val isRight get() = this is Right<R>
    val isLeft get() = this is Left<L>
    
    fun getOrNull(): R? = when (this) {
        is Right -> value
        is Left -> null
    }
    
    // Transform Right, keep Left
    fun <T> map(transform: (R) -> T): Either<L, T> = when (this) {
        is Right -> Right(transform(value))
        is Left -> this
    }
    
    // FlatMap (evita Either<Either<...>>)
    fun <T> flatMap(transform: (R) -> Either<L, T>): Either<L, T> = when (this) {
        is Right -> transform(value)
        is Left -> this
    }
}
```

---

### DomainError.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/common/DomainError.kt`

```kotlin
package com.seguro.quotes.common

// Sealed class: todos os erros possíveis conhecidos em tempo de compilação
sealed class DomainError(val message: String, val code: String) {
    
    // Erros de validação
    object InvalidDocument : DomainError(
        "Documento é obrigatório e não pode ser vazio",
        "INVALID_DOCUMENT"
    )
    
    object InvalidEmail : DomainError(
        "Email deve conter @",
        "INVALID_EMAIL"
    )
    
    object MissingVehicle : DomainError(
        "Veículo é obrigatório para seguro AUTO",
        "MISSING_VEHICLE"
    )
    
    object InvalidVehicle : DomainError(
        "Veículo deve ter placa OU modelo",
        "INVALID_VEHICLE"
    )
    
    object MissingAge : DomainError(
        "Idade é obrigatória para seguro VIDA",
        "MISSING_AGE"
    )
    
    object Underage : DomainError(
        "Idade mínima: 18 anos",
        "UNDERAGE"
    )
    
    // Erros de negócio
    object QuoteNotFound : DomainError(
        "Cotação não encontrada",
        "QUOTE_NOT_FOUND"
    )
    
    object QuoteNotApproved : DomainError(
        "Cotação não está aprovada para emitir apólice",
        "QUOTE_NOT_APPROVED"
    )
    
    object QuoteExpired : DomainError(
        "Cotação expirada (7 dias sem emissão)",
        "QUOTE_EXPIRED"
    )
    
    data class LimitExceeded(val limit: Double, val actual: Double) : DomainError(
        "Preço $actual excede limite de $limit",
        "LIMIT_EXCEEDED"
    )
}
```

**Por que sealed class?**
- Compilador garante tratamento exaustivo no `when`
- IDE autocompleta todos os casos
- Type-safe: não usa strings mágicas

---

## Subtask 3.2: Validador com Either

### QuoteValidator.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/domain/validation/QuoteValidator.kt`

```kotlin
package com.seguro.quotes.domain.validation

import com.seguro.quotes.common.Either
import com.seguro.quotes.common.DomainError
import com.seguro.quotes.dto.QuoteRequest
import com.seguro.quotes.domain.enums.InsuranceType

object QuoteValidator {
    
    // Validação completa (encadeia todas)
    fun validate(request: QuoteRequest): Either<DomainError, QuoteRequest> {
        return validateDocument(request.documento)
            .flatMap { validateEmail(request.email) }
            .flatMap { validateInsuranceSpecificRules(request) }
            .map { request } // Se tudo OK, retorna request
    }
    
    // Valida documento (não vazio)
    private fun validateDocument(documento: String): Either<DomainError, String> {
        return if (documento.isNotBlank()) {
            Either.Right(documento)
        } else {
            Either.Left(DomainError.InvalidDocument)
        }
    }
    
    // Valida email (se fornecido, deve ter @)
    private fun validateEmail(email: String?): Either<DomainError, String?> {
        return when {
            email == null -> Either.Right(null) // OK, é opcional
            email.contains("@") -> Either.Right(email)
            else -> Either.Left(DomainError.InvalidEmail)
        }
    }
    
    // Valida regras específicas por tipo de seguro
    private fun validateInsuranceSpecificRules(
        request: QuoteRequest
    ): Either<DomainError, QuoteRequest> {
        return when (request.tipoSeguro) {
            InsuranceType.AUTO -> validateAutoRules(request)
            InsuranceType.VIDA -> validateLifeRules(request)
        }
    }
    
    // AUTO: veículo obrigatório
    private fun validateAutoRules(request: QuoteRequest): Either<DomainError, QuoteRequest> {
        val veiculo = request.veiculo 
            ?: return Either.Left(DomainError.MissingVehicle)
        
        return if (veiculo.isValid()) {
            Either.Right(request)
        } else {
            Either.Left(DomainError.InvalidVehicle)
        }
    }
    
    // VIDA: idade obrigatória e >= 18
    private fun validateLifeRules(request: QuoteRequest): Either<DomainError, QuoteRequest> {
        val idade = request.idade 
            ?: return Either.Left(DomainError.MissingAge)
        
        return if (idade >= 18) {
            Either.Right(request)
        } else {
            Either.Left(DomainError.Underage)
        }
    }
}
```

**Vantagens sobre exceptions:**
```kotlin
// ❌ Com exceptions (fluxo escondido)
try {
    val quote = service.createQuote(request)
    // ... sucesso
} catch (e: InvalidDocumentException) {
    // ...
} catch (e: UnderageException) {
    // ...
}

// ✅ Com Either (fluxo explícito)
when (val result = service.createQuote(request)) {
    is Either.Right -> // sucesso: result.value
    is Either.Left -> when (result.value) {
        is DomainError.InvalidDocument -> // ...
        is DomainError.Underage -> // ...
        // Compilador força tratar TODOS os casos!
    }
}
```

---

## Subtask 3.3: Usar Either no Controller

**O que fazer:**
Refatore `QuoteController` para usar validação.

```kotlin
@RestController
@RequestMapping("/quotes")
class QuoteController {
    private val quotes = ConcurrentHashMap<UUID, Quote>()
    
    @PostMapping
    fun createQuote(
        @Valid @RequestBody request: QuoteRequest
    ): ResponseEntity<*> { // Usa * porque pode retornar QuoteResponse OU ErrorResponse
        
        // Validar com Either
        return when (val validation = QuoteValidator.validate(request)) {
            is Either.Left -> {
                val error = validation.value
                ResponseEntity
                    .badRequest()
                    .body(ErrorResponse(error.code, error.message))
            }
            
            is Either.Right -> {
                val quote = Quote(
                    status = QuoteStatus.CREATED,
                    request = request
                )
                quotes[quote.id] = quote
                
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(QuoteResponse.from(quote))
            }
        }
    }
    
    // ...existing code...
}

// DTO para erro
data class ErrorResponse(
    val code: String,
    val message: String
)
```

---

### 🎓 Checkpoint Task 3

**Teste validações:**

```bash
# Deve falhar: documento vazio
POST /quotes
{
  "nome": "João",
  "documento": "",  # ❌
  "tipoSeguro": "AUTO",
  "veiculo": {"placa": "ABC1234"}
}
# Esperado: 400 Bad Request com INVALID_DOCUMENT

# Deve falhar: email inválido
POST /quotes
{
  "nome": "João",
  "documento": "123",
  "email": "invalido",  # ❌ sem @
  "tipoSeguro": "AUTO",
  "veiculo": {"placa": "ABC1234"}
}
# Esperado: 400 Bad Request com INVALID_EMAIL

# Deve falhar: AUTO sem veículo
POST /quotes
{
  "nome": "João",
  "documento": "123",
  "tipoSeguro": "AUTO"
  # ❌ falta veiculo
}
# Esperado: 400 Bad Request com MISSING_VEHICLE

# Deve falhar: VIDA sem idade
POST /quotes
{
  "nome": "João",
  "documento": "123",
  "tipoSeguro": "VIDA"
  # ❌ falta idade
}
# Esperado: 400 Bad Request com MISSING_AGE

# Deve falhar: VIDA com idade < 18
POST /quotes
{
  "nome": "João",
  "documento": "123",
  "tipoSeguro": "VIDA",
  "idade": 17  # ❌
}
# Esperado: 400 Bad Request com UNDERAGE

# Deve passar ✅
POST /quotes
{
  "nome": "João Silva",
  "documento": "12345678900",
  "email": "joao@email.com",
  "tipoSeguro": "AUTO",
  "veiculo": {"placa": "ABC1234"},
  "coberturas": ["ROUBO"]
}
# Esperado: 201 Created
```

**Próxima task**: Precificação funcional

---

# 🚀 TASK 4: Precificação Funcional

### 📚 Conceitos Aplicados
- ✅ **Conceito 4**: Funções e Lambdas
- ✅ **Conceito 7**: Collections (map, filter, fold)
- ✅ **Conceito 8**: When expression

### 🎯 Objetivo
Implementar cálculo de preço usando operações funcionais e decidir aprovação/rejeição.

---

## Subtask 4.1: PriceCalculator com Lambdas

### PriceCalculator.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/domain/service/PriceCalculator.kt`

```kotlin
package com.seguro.quotes.domain.service

import com.seguro.quotes.dto.QuoteRequest
import com.seguro.quotes.domain.enums.Coverage
import com.seguro.quotes.domain.enums.InsuranceType

// Type alias para estratégia de pricing
typealias PricingStrategy = (QuoteRequest) -> Double

object PriceCalculator {
    
    private const val LIMIT_MAX = 300.0
    
    // Calcular preço completo
    fun calculate(request: QuoteRequest): PriceResult {
        val basePrice = request.tipoSeguro.basePrice
        val coveragesCost = calculateCoveragesCost(request.coberturas)
        val ageFactor = calculateAgeFactor(request)
        
        val totalPrice = (basePrice + coveragesCost) * ageFactor
        
        val isApproved = totalPrice <= LIMIT_MAX
        val rejectionReason = if (isApproved) null else "LIMIT_EXCEEDED"
        
        return PriceResult(
            price = totalPrice,
            approved = isApproved,
            rejectionReason = rejectionReason
        )
    }
    
    // Soma custos de coberturas (usando fold)
    private fun calculateCoveragesCost(coverages: List<Coverage>): Double {
        return coverages.fold(0.0) { acc, coverage ->
            acc + coverage.additionalCost
        }
        // Alternativa com sumOf:
        // return coverages.sumOf { it.additionalCost }
    }
    
    // Fator de idade (VIDA)
    private fun calculateAgeFactor(request: QuoteRequest): Double {
        // Se não é VIDA, fator = 1.0 (sem alteração)
        if (request.tipoSeguro != InsuranceType.VIDA) return 1.0
        
        val idade = request.idade ?: return 1.0
        
        return when {
            idade < 25 -> 1.20  // +20%
            idade <= 50 -> 1.0  // +0%
            else -> 1.30        // +30%
        }
    }
}

data class PriceResult(
    val price: Double,
    val approved: Boolean,
    val rejectionReason: String?
)
```

**Conceitos aplicados:**
1. **Object** = Singleton (única instância)
2. **fold** = reduce funcional (acumula valor)
3. **when** = switch melhorado com ranges
4. **Type alias** = apelido para tipo complexo

**Perguntas:**
1. Por que `fold(0.0)` e não `fold(0)`?
   - **R**: `0.0` é Double, mantém tipo consistente
2. Diferença entre `fold` e `sumOf`?
   - **R**: `fold` é genérico (qualquer acumulador), `sumOf` específico para soma
3. Por que `when` sem `else` compila?
   - **R**: Kotlin detecta que todos os casos estão cobertos (`< 25`, `<= 50`, `else`)

---

## Subtask 4.2: Integrar Pricing no Controller

**O que fazer:**
Refatore `QuoteController` para calcular preço automaticamente.

```kotlin
@RestController
@RequestMapping("/quotes")
class QuoteController {
    private val quotes = ConcurrentHashMap<UUID, Quote>()
    
    @PostMapping
    fun createQuote(
        @Valid @RequestBody request: QuoteRequest
    ): ResponseEntity<*> {
        
        // 1. Validar
        return when (val validation = QuoteValidator.validate(request)) {
            is Either.Left -> {
                val error = validation.value
                ResponseEntity
                    .badRequest()
                    .body(ErrorResponse(error.code, error.message))
            }
            
            is Either.Right -> {
                // 2. Calcular preço
                val priceResult = PriceCalculator.calculate(request)
                
                // 3. Criar quote com status apropriado
                val quote = Quote(
                    status = if (priceResult.approved) QuoteStatus.APPROVED else QuoteStatus.REJECTED,
                    preco = priceResult.price,
                    razoesRejeicao = priceResult.rejectionReason?.let { listOf(it) } ?: emptyList(),
                    request = request
                )
                
                quotes[quote.id] = quote
                
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(QuoteResponse.from(quote))
            }
        }
    }
    
    // ...existing code...
}
```

---

### 🎓 Checkpoint Task 4

**Teste precificação:**

```bash
# Caso 1: AUTO simples (deve aprovar)
POST /quotes
{
  "nome": "João",
  "documento": "123",
  "tipoSeguro": "AUTO",
  "veiculo": {"placa": "ABC1234"},
  "coberturas": []
}
# Esperado:
# - preco: 100.0 (base AUTO)
# - status: APPROVED

# Caso 2: AUTO com coberturas (deve aprovar)
POST /quotes
{
  "nome": "João",
  "documento": "123",
  "tipoSeguro": "AUTO",
  "veiculo": {"placa": "ABC1234"},
  "coberturas": ["ROUBO", "COLISAO"]  # +20 +30 = +50
}
# Esperado:
# - preco: 150.0 (100 + 50)
# - status: APPROVED

# Caso 3: AUTO com muitas coberturas (deve rejeitar)
POST /quotes
{
  "nome": "João",
  "documento": "123",
  "tipoSeguro": "AUTO",
  "veiculo": {"placa": "ABC1234"},
  "coberturas": ["ROUBO", "COLISAO", "ASSISTENCIA", "DANOS_TERCEIROS"]  # +85
}
# Esperado:
# - preco: 185.0 (ainda aprovado, < 300)
# - status: APPROVED

# Caso 4: VIDA jovem sem coberturas (deve aprovar)
POST /quotes
{
  "nome": "João",
  "documento": "123",
  "tipoSeguro": "VIDA",
  "idade": 20,
  "coberturas": []
}
# Esperado:
# - preco: 96.0 (80 * 1.20)
# - status: APPROVED

# Caso 5: VIDA idoso com coberturas (pode rejeitar)
POST /quotes
{
  "nome": "João",
  "documento": "123",
  "tipoSeguro": "VIDA",
  "idade": 55,
  "coberturas": ["ROUBO", "COLISAO", "ASSISTENCIA", "DANOS_TERCEIROS"]  # +85
}
# Esperado:
# - preco: (80 + 85) * 1.30 = 214.5
# - status: APPROVED

# Caso 6: Forçar rejeição (preço > 300)
# Desafio: ajuste LIMIT_MAX para 150 temporariamente e teste
```

**Fórmulas:**
- AUTO: `base(100) + coberturas`
- VIDA: `(base(80) + coberturas) * fator_idade`

**Próxima task**: Normalização e extensions

---

# 🚀 TASK 5: Normalização e Extensions

### 📚 Conceitos Aplicados
- ✅ **Conceito 5**: Extension Functions
- ✅ **Conceito 6**: Scope Functions

### 🎯 Objetivo
Criar extensions úteis para higiene de dados (mascaramento, normalização) usando scope functions conscientemente.

---

## Subtask 5.1: Extensions para Strings

### StringExtensions.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/common/StringExtensions.kt`

```kotlin
package com.seguro.quotes.common

// Mascarar documento (para logs sanitizados)
fun String.maskDocument(): String {
    if (this.length < 4) return "***"
    
    return this.takeLast(4).let { last4 ->
        "*".repeat(this.length - 4) + last4
    }
}

// Exemplos:
// "12345678900".maskDocument() → "*******8900"
// "123".maskDocument() → "***"

// Normalizar documento (remover pontos, traços, espaços)
fun String.normalizeDocument(): String {
    return this.replace(Regex("[^0-9]"), "")
}

// Exemplos:
// "123.456.789-00".normalizeDocument() → "12345678900"
// "123 456 789 00".normalizeDocument() → "12345678900"

// Normalizar nome (trim, capitalizar primeira letra de cada palavra)
fun String.normalizeName(): String {
    return this.trim()
        .lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}

// Exemplos:
// "  joÃO   siLVA  ".normalizeName() → "João Silva"
// "MARIA".normalizeName() → "Maria"
```

**Conceitos:**
1. **Extension function**: adiciona funcionalidade a `String` sem herança
2. **let**: transforma valor e retorna resultado
3. **takeLast**: pega últimos N caracteres

---

## Subtask 5.2: Extensions para Quote

### QuoteExtensions.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/domain/extensions/QuoteExtensions.kt`

```kotlin
package com.seguro.quotes.domain.extensions

import com.seguro.quotes.domain.model.Quote
import com.seguro.quotes.common.maskDocument

// Total de coberturas
fun Quote.totalCoverages(): Int = this.request.coberturas.size

// Documento mascarado (para logs)
fun Quote.maskedDocument(): String = this.request.documento.maskDocument()

// Log seguro (sem expor dados sensíveis)
fun Quote.toSafeLogString(): String = buildString {
    append("Quote[")
    append("id=${id}, ")
    append("status=${status}, ")
    append("preco=${preco}, ")
    append("documento=${maskedDocument()}, ")  // mascarado!
    append("coberturas=${totalCoverages()}")
    append("]")
}

// Alternativa usando apply:
fun Quote.logSafe(): Quote = apply {
    println(toSafeLogString())
}
```

**Por que extension ao invés de método na classe?**
- Separação de concerns: `Quote` é domínio, log é infraestrutura
- Não polui classe com responsabilidades diferentes
- Pode ser adicionada em módulo separado

---

## Subtask 5.3: Normalizar Request com Scope Functions

### QuoteRequestExtensions.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/dto/QuoteRequestExtensions.kt`

```kotlin
package com.seguro.quotes.dto

import com.seguro.quotes.common.normalizeDocument
import com.seguro.quotes.common.normalizeName

// Normalizar request (retorna cópia com dados limpos)
fun QuoteRequest.normalized(): QuoteRequest {
    return this.copy(
        nome = nome.normalizeName(),
        documento = documento.normalizeDocument(),
        email = email?.trim()?.lowercase(),
        cep = cep?.normalizeDocument() // remove traços/espaços
    )
}

// Exemplo de uso com apply para logging:
fun QuoteRequest.normalizedWithLog(): QuoteRequest {
    return normalized().apply {
        println("Request normalizado: nome=$nome, documento=***")
    }
}
```

**Conceito `copy`:**
- Data classes geram `copy()` automaticamente
- Cria nova instância alterando apenas campos especificados
- Mantém imutabilidade

```kotlin
val original = QuoteRequest(nome = "  joão  ", ...)
val normalizado = original.normalized()
// original inalterado!
// normalizado.nome == "João"
```

---

## Subtask 5.4: Usar Extensions no Controller

**O que fazer:**
Refatore `QuoteController` para usar normalização e logging.

```kotlin
@RestController
@RequestMapping("/quotes")
class QuoteController {
    private val quotes = ConcurrentHashMap<UUID, Quote>()
    
    @PostMapping
    fun createQuote(
        @Valid @RequestBody request: QuoteRequest
    ): ResponseEntity<*> {
        
        // 1. Normalizar request
        val normalizedRequest = request.normalized()
        
        // 2. Validar
        return when (val validation = QuoteValidator.validate(normalizedRequest)) {
            is Either.Left -> {
                val error = validation.value
                ResponseEntity
                    .badRequest()
                    .body(ErrorResponse(error.code, error.message))
            }
            
            is Either.Right -> {
                // 3. Calcular preço
                val priceResult = PriceCalculator.calculate(normalizedRequest)
                
                // 4. Criar quote
                val quote = Quote(
                    status = if (priceResult.approved) QuoteStatus.APPROVED else QuoteStatus.REJECTED,
                    preco = priceResult.price,
                    razoesRejeicao = priceResult.rejectionReason?.let { listOf(it) } ?: emptyList(),
                    request = normalizedRequest
                ).apply {
                    // 5. Log seguro (usando extension + scope function)
                    println(toSafeLogString())
                }
                
                quotes[quote.id] = quote
                
                ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(QuoteResponse.from(quote))
            }
        }
    }
    
    // ...existing code...
}
```

---

### Exemplos de Scope Functions (Quando usar?)

```kotlin
// APPLY: configurar objeto e retornar o próprio objeto
val quote = Quote(...).apply {
    println(toSafeLogString())  // this = quote
} // retorna quote

// ALSO: efeito colateral e retornar o próprio objeto
val quote = Quote(...).also { q ->
    logger.info("Quote criada: ${q.id}")
} // retorna quote

// LET: transformar objeto
val masked = documento.let { it.maskDocument() } // retorna String

// RUN: executar bloco no contexto do objeto
val total = quote.run {
    preco ?: 0.0  // this = quote, retorna Double
}

// WITH: similar ao run, mas não é extension
with(quote) {
    println("ID: $id, Preço: $preco")
}
```

**Regra de ouro:**
- `apply` / `also` → retornam **o próprio objeto** (configuração, logging)
- `let` / `run` → retornam **resultado do bloco** (transformação, cálculo)

---

### 🎓 Checkpoint Task 5

**Teste normalização:**

```bash
# Enviar request "sujo"
POST /quotes
{
  "nome": "  joÃO   siLVA  ",           # espaços extras, case errado
  "documento": "123.456.789-00",        # com pontos e traços
  "email": "  JOAO@EMAIL.COM  ",        # espaços, uppercase
  "tipoSeguro": "AUTO",
  "veiculo": {"placa": "ABC1234"},
  "coberturas": ["ROUBO"]
}

# Verificar nos logs:
# - nome normalizado: "João Silva"
# - documento normalizado: "12345678900"
# - email normalizado: "joao@email.com"
# - documento mascarado no log: "*******8900"
```

**Verifique logs:**
```
Quote[id=..., status=APPROVED, preco=120.0, documento=*******8900, coberturas=1]
```

**Documento NÃO deve aparecer completo!** ✅

**Próxima task**: Persistência in-memory com concorrência segura

---

# 🚀 TASK 6: Persistência In-Memory com Concorrência Segura

### 📚 Conceitos Aplicados
- ✅ **Conceito 11**: Interoperabilidade Java-Kotlin (ConcurrentHashMap)

### 🎯 Objetivo
Criar repositórios in-memory thread-safe com idempotência mínima.

---

## Subtask 6.1: QuoteRepository

### QuoteRepository.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/repository/QuoteRepository.kt`

```kotlin
package com.seguro.quotes.repository

import com.seguro.quotes.domain.model.Quote
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
class QuoteRepository {
    
    // ConcurrentHashMap = thread-safe (múltiplas requisições)
    private val storage = ConcurrentHashMap<UUID, Quote>()
    
    // Salvar quote
    fun save(quote: Quote): Quote {
        storage[quote.id] = quote
        return quote
    }
    
    // Buscar por ID
    fun findById(id: UUID): Quote? = storage[id]
    
    // Listar todas
    fun findAll(): List<Quote> = storage.values.toList()
    
    // Deletar (para testes)
    fun deleteById(id: UUID): Boolean = storage.remove(id) != null
    
    // Contar total
    fun count(): Int = storage.size
}
```

**Por que `ConcurrentHashMap`?**
- `HashMap` normal **NÃO é thread-safe**
- Múltiplas requisições HTTP simultâneas = múltiplas threads
- `ConcurrentHashMap` permite leitura/escrita simultânea sem corrupção

---

## Subtask 6.2: PolicyRepository

### PolicyRepository.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/repository/PolicyRepository.kt`

**Exercício para você:**
```kotlin
package com.seguro.quotes.repository

import com.seguro.quotes.domain.model.Policy
import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
class PolicyRepository {
    
    private val storage = ConcurrentHashMap<UUID, Policy>()
    
    // TODO: Implemente os métodos:
    // - save(policy: Policy): Policy
    // - findById(id: UUID): Policy?
    // - findByQuoteId(quoteId: UUID): Policy?
    // - findAll(): List<Policy>
    // - deleteById(id: UUID): Boolean
}
```

**Dica:**
`findByQuoteId` precisa filtrar values:
```kotlin
fun findByQuoteId(quoteId: UUID): Policy? {
    return storage.values.firstOrNull { it.quoteId == quoteId }
}
```

---

## Subtask 6.3: Idempotência com Idempotency Key (Opcional)

### IdempotentQuoteRepository.kt
**O que fazer:**
Adicionar suporte a idempotency key (evitar duplicação de request).

```kotlin
@Repository
class QuoteRepository {
    
    private val storage = ConcurrentHashMap<UUID, Quote>()
    
    // Novo: mapear idempotencyKey → quoteId
    private val idempotencyKeys = ConcurrentHashMap<String, UUID>()
    
    // Salvar com idempotency
    fun saveIdempotent(quote: Quote, idempotencyKey: String?): Quote {
        if (idempotencyKey != null) {
            // Verificar se já existe quote com essa key
            val existingId = idempotencyKeys[idempotencyKey]
            if (existingId != null) {
                // Retornar quote existente (idempotente!)
                return storage[existingId]!!
            }
            
            // Primeira vez: registrar key
            idempotencyKeys[idempotencyKey] = quote.id
        }
        
        storage[quote.id] = quote
        return quote
    }
    
    // ...existing methods...
}
```

**Como usar:**
Cliente envia header:
```
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
```

Se enviar mesma key novamente, recebe mesma quote (não cria duplicata).

---

### 🎓 Checkpoint Task 6

**Remover ConcurrentHashMap dos controllers:**

Antes (Task 2):
```kotlin
@RestController
class QuoteController {
    private val quotes = ConcurrentHashMap<UUID, Quote>() // ❌ direto no controller
}
```

Agora (Task 6):
```kotlin
@RestController
class QuoteController(
    private val quoteRepository: QuoteRepository // ✅ injetado
) {
    // use quoteRepository.save(), findById(), etc.
}
```

**Próxima task**: Camada de serviço + boas práticas

---

# 🚀 TASK 7: Camada de Serviço + Boas Práticas

### 📚 Conceitos Aplicados
- ✅ **Conceito 12**: Boas Práticas (package structure, separation of concerns)
- ✅ **Conceito 13**: Convenções (naming, val padrão, evitar `!!`)

### 🎯 Objetivo
Separar Controller/Service/Repository, aplicar boas práticas de Kotlin.

---

## Subtask 7.1: QuoteService

### QuoteService.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/service/QuoteService.kt`

```kotlin
package com.seguro.quotes.service

import com.seguro.quotes.common.Either
import com.seguro.quotes.common.DomainError
import com.seguro.quotes.domain.model.Quote
import com.seguro.quotes.domain.enums.QuoteStatus
import com.seguro.quotes.domain.validation.QuoteValidator
import com.seguro.quotes.domain.service.PriceCalculator
import com.seguro.quotes.dto.QuoteRequest
import com.seguro.quotes.dto.normalized
import com.seguro.quotes.repository.QuoteRepository
import com.seguro.quotes.domain.extensions.toSafeLogString
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class QuoteService(
    private val quoteRepository: QuoteRepository
) {
    
    // Criar e processar quote completa
    fun createQuote(request: QuoteRequest): Either<DomainError, Quote> {
        // 1. Normalizar
        val normalizedRequest = request.normalized()
        
        // 2. Validar
        val validation = QuoteValidator.validate(normalizedRequest)
        if (validation.isLeft) {
            return validation as Either.Left<DomainError>
        }
        
        // 3. Calcular preço
        val priceResult = PriceCalculator.calculate(normalizedRequest)
        
        // 4. Criar quote
        val quote = Quote(
            status = if (priceResult.approved) QuoteStatus.APPROVED else QuoteStatus.REJECTED,
            preco = priceResult.price,
            razoesRejeicao = priceResult.rejectionReason?.let { listOf(it) } ?: emptyList(),
            request = normalizedRequest
        )
        
        // 5. Persistir
        val savedQuote = quoteRepository.save(quote)
        
        // 6. Log (sem dados sensíveis)
        println(savedQuote.toSafeLogString())
        
        return Either.Right(savedQuote)
    }
    
    // Buscar quote por ID
    fun getQuoteById(id: UUID): Either<DomainError, Quote> {
        val quote = quoteRepository.findById(id)
            ?: return Either.Left(DomainError.QuoteNotFound)
        
        return Either.Right(quote)
    }
    
    // Listar todas
    fun listAllQuotes(): List<Quote> = quoteRepository.findAll()
}
```

**Responsabilidades do Service:**
- Orquestrar validação, cálculo, persistência
- Aplicar regras de negócio
- Tratar erros de domínio
- Logging (sem expor dados sensíveis)

---

## Subtask 7.2: PolicyService

### PolicyService.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/service/PolicyService.kt`

**Exercício para você:**
```kotlin
package com.seguro.quotes.service

import com.seguro.quotes.common.Either
import com.seguro.quotes.common.DomainError
import com.seguro.quotes.domain.model.Policy
import com.seguro.quotes.domain.enums.PolicyStatus
import com.seguro.quotes.repository.QuoteRepository
import com.seguro.quotes.repository.PolicyRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class PolicyService(
    private val policyRepository: PolicyRepository,
    private val quoteRepository: QuoteRepository
) {
    
    // Emitir apólice a partir de quote
    fun issuePolicy(quoteId: UUID): Either<DomainError, Policy> {
        // 1. Buscar quote
        val quote = quoteRepository.findById(quoteId)
            ?: return Either.Left(DomainError.QuoteNotFound)
        
        // 2. Validar se pode emitir
        if (!quote.canGeneratePolicy()) {
            return Either.Left(DomainError.QuoteNotApproved)
        }
        
        if (quote.isExpired()) {
            return Either.Left(DomainError.QuoteExpired)
        }
        
        // 3. Criar policy
        val policy = Policy(
            quoteId = quoteId,
            status = PolicyStatus.ACTIVE,
            dataInicio = LocalDate.now(),
            dataFim = LocalDate.now().plusYears(1),
            // TODO: Adicionar policyNumber gerado
        )
        
        // 4. Persistir
        val savedPolicy = policyRepository.save(policy)
        
        // 5. Log
        println("Policy emitida: id=${savedPolicy.id}, quoteId=$quoteId")
        
        return Either.Right(savedPolicy)
    }
    
    // Buscar policy por ID
    fun getPolicyById(id: UUID): Either<DomainError, Policy> {
        // TODO: Implemente (similar a getQuoteById)
        TODO("Implementar busca de policy")
    }
    
    // Listar todas
    fun listAllPolicies(): List<Policy> = policyRepository.findAll()
}
```

---

## Subtask 7.3: Refatorar Controllers (Thin Controllers)

### QuoteController.kt (refatorado)
**O que fazer:**
Controller agora **delega tudo** para Service.

```kotlin
package com.seguro.quotes.controller

import com.seguro.quotes.dto.QuoteRequest
import com.seguro.quotes.dto.QuoteResponse
import com.seguro.quotes.dto.ErrorResponse
import com.seguro.quotes.service.QuoteService
import com.seguro.quotes.common.Either
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID
import javax.validation.Valid

@RestController
@RequestMapping("/quotes")
class QuoteController(
    private val quoteService: QuoteService // ✅ injetar service
) {
    
    @PostMapping
    fun createQuote(
        @Valid @RequestBody request: QuoteRequest
    ): ResponseEntity<*> {
        return when (val result = quoteService.createQuote(request)) {
            is Either.Right -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(QuoteResponse.from(result.value))
            
            is Either.Left -> ResponseEntity
                .badRequest()
                .body(ErrorResponse(result.value.code, result.value.message))
        }
    }
    
    @GetMapping("/{id}")
    fun getQuote(@PathVariable id: UUID): ResponseEntity<*> {
        return when (val result = quoteService.getQuoteById(id)) {
            is Either.Right -> ResponseEntity.ok(QuoteResponse.from(result.value))
            is Either.Left -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse(result.value.code, result.value.message))
        }
    }
    
    @GetMapping
    fun listQuotes(): ResponseEntity<List<QuoteResponse>> {
        val quotes = quoteService.listAllQuotes()
        val responses = quotes.map { QuoteResponse.from(it) }
        return ResponseEntity.ok(responses)
    }
}
```

**Controller agora é "fino":**
- Recebe request HTTP
- Delega para Service
- Converte Either → HTTP status code
- Retorna response HTTP

---

### PolicyController.kt (refatorado)
**Exercício para você:**
```kotlin
@RestController
@RequestMapping("/policies")
class PolicyController(
    private val policyService: PolicyService
) {
    
    @PostMapping
    fun issuePolicy(@RequestBody body: Map<String, String>): ResponseEntity<*> {
        val quoteId = UUID.fromString(body["quoteId"])
        
        return when (val result = policyService.issuePolicy(quoteId)) {
            is Either.Right -> ResponseEntity.ok(PolicyResponse.from(result.value))
            is Either.Left -> ResponseEntity
                .badRequest()
                .body(ErrorResponse(result.value.code, result.value.message))
        }
    }
    
    // TODO: Implemente GET /{id} e GET /
}
```

---

## Subtask 7.4: Aplicar Boas Práticas de Kotlin

### Checklist de Boas Práticas ✅

**1. Val por padrão (imutabilidade)**
```kotlin
// ✅ BOM
val quote = Quote(...)
val preco = calculatePrice()

// ❌ RUIM (só use var se realmente precisar mutar)
var quote = Quote(...)
```

**2. Evitar `!!` (force unwrap)**
```kotlin
// ❌ RUIM (pode lançar NPE)
val quote = repository.findById(id)!!

// ✅ BOM (tratamento explícito)
val quote = repository.findById(id)
    ?: return Either.Left(DomainError.QuoteNotFound)
```

**3. Usar elvis operator `?:`**
```kotlin
// ✅ BOM
val email = request.email ?: "nao-informado@example.com"

// ❌ VERBOSO
val email = if (request.email != null) request.email else "..."
```

**4. Usar `when` ao invés de if/else encadeado**
```kotlin
// ✅ BOM
val factor = when {
    idade < 25 -> 1.20
    idade <= 50 -> 1.0
    else -> 1.30
}

// ❌ VERBOSO
val factor = if (idade < 25) {
    1.20
} else if (idade <= 50) {
    1.0
} else {
    1.30
}
```

**5. Funções pequenas (< 20 linhas)**
```kotlin
// ✅ BOM: dividir em funções menores
fun createQuote(request: QuoteRequest): Either<DomainError, Quote> {
    val validation = validateRequest(request)
    if (validation.isLeft) return validation
    
    val price = calculatePrice(request)
    return buildAndSaveQuote(request, price)
}

// ❌ RUIM: função gigante com 100+ linhas
```

**6. Named arguments para clareza**
```kotlin
// ✅ BOM (fica claro o que é cada argumento)
val policy = Policy(
    quoteId = id,
    status = PolicyStatus.ACTIVE,
    dataInicio = LocalDate.now(),
    dataFim = LocalDate.now().plusYears(1)
)

// ❌ CONFUSO
val policy = Policy(id, PolicyStatus.ACTIVE, LocalDate.now(), LocalDate.now().plusYears(1))
```

**7. Usar require/check para precondições**
```kotlin
// ✅ BOM
fun calculatePrice(request: QuoteRequest): Double {
    require(request.tipoSeguro != null) { "Tipo de seguro obrigatório" }
    // ...
}

// ❌ VERBOSO
fun calculatePrice(request: QuoteRequest): Double {
    if (request.tipoSeguro == null) {
        throw IllegalArgumentException("Tipo de seguro obrigatório")
    }
}
```

---

### 🎓 Checkpoint Task 7

**Estrutura final:**
```
src/main/kotlin/com/seguro/quotes/
├── controller/
│   ├── QuoteController.kt          (thin, delega para service)
│   └── PolicyController.kt         (thin)
├── service/
│   ├── QuoteService.kt             (orquestração + regras)
│   └── PolicyService.kt            (orquestração)
├── repository/
│   ├── QuoteRepository.kt          (persistência)
│   └── PolicyRepository.kt         (persistência)
├── domain/
│   ├── model/
│   ├── enums/
│   ├── validation/
│   ├── service/ (PriceCalculator)
│   └── extensions/
├── dto/
└── common/
```

**Responsabilidades claras:**
- **Controller**: HTTP (recebe, delega, retorna)
- **Service**: Regras de negócio, orquestração
- **Repository**: Persistência (CRUD)
- **Domain**: Entidades, enums, validações

**Próxima task**: Processamento assíncrono com Coroutines

---

# 🚀 TASK 8: Processamento Assíncrono com Coroutines

### 📚 Conceitos Aplicados
- ✅ **Conceito 10**: Coroutines (async/await)

### 🎯 Objetivo
Tornar criação de quote assíncrona, simulando chamada externa (risk score) com delay.

---

## Subtask 8.1: Adicionar Dependências

**O que fazer:**
Edite `build.gradle.kts` ou `pom.xml`:

```kotlin
// build.gradle.kts
dependencies {
    // ...existing...
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")
}
```

---

## Subtask 8.2: Simular Chamada Externa (Risk Score)

### RiskScoreService.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/service/RiskScoreService.kt`

```kotlin
package com.seguro.quotes.service

import kotlinx.coroutines.delay
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class RiskScoreService {
    
    // Simula chamada externa (API de score de risco)
    suspend fun calculateRiskScore(documento: String): Int {
        // Simula latência de rede (500ms)
        delay(500)
        
        // Retorna score aleatório entre 0-100
        return Random.nextInt(0, 100)
    }
}
```

**Conceitos:**
- `suspend` = função que pode "pausar" sem bloquear thread
- `delay()` = pausa assíncrona (não bloqueia thread!)
- Simula: chamada HTTP externa, consulta banco, etc.

---

## Subtask 8.3: QuoteService Assíncrono

### QuoteService.kt (refatorado com suspend)
**O que fazer:**
Tornar `createQuote` suspendível:

```kotlin
@Service
class QuoteService(
    private val quoteRepository: QuoteRepository,
    private val riskScoreService: RiskScoreService
) {
    
    // Agora é suspend function!
    suspend fun createQuote(request: QuoteRequest): Either<DomainError, Quote> {
        // 1. Normalizar
        val normalizedRequest = request.normalized()
        
        // 2. Validar
        val validation = QuoteValidator.validate(normalizedRequest)
        if (validation.isLeft) {
            return validation as Either.Left<DomainError>
        }
        
        // 3. Calcular risk score (assíncrono!)
        val riskScore = riskScoreService.calculateRiskScore(normalizedRequest.documento)
        println("Risk score: $riskScore")
        
        // 4. Calcular preço
        val priceResult = PriceCalculator.calculate(normalizedRequest)
        
        // 5. Ajustar preço baseado em risk (exemplo)
        val adjustedPrice = if (riskScore > 70) {
            priceResult.price * 1.10 // +10% para alto risco
        } else {
            priceResult.price
        }
        
        // 6. Criar quote
        val quote = Quote(
            status = if (adjustedPrice <= 300.0) QuoteStatus.APPROVED else QuoteStatus.REJECTED,
            preco = adjustedPrice,
            razoesRejeicao = if (adjustedPrice > 300.0) listOf("LIMIT_EXCEEDED") else emptyList(),
            request = normalizedRequest
        )
        
        // 7. Persistir
        val savedQuote = quoteRepository.save(quote)
        
        println(savedQuote.toSafeLogString())
        
        return Either.Right(savedQuote)
    }
    
    // ...existing methods...
}
```

---

## Subtask 8.4: Controller com Coroutines

### QuoteController.kt (refatorado)
**O que fazer:**
Tornar endpoint suspendível:

```kotlin
@RestController
@RequestMapping("/quotes")
class QuoteController(
    private val quoteService: QuoteService
) {
    
    // Agora é suspend function!
    @PostMapping
    suspend fun createQuote(
        @Valid @RequestBody request: QuoteRequest
    ): ResponseEntity<*> {
        // Spring Boot detecta suspend e gerencia coroutine automaticamente!
        return when (val result = quoteService.createQuote(request)) {
            is Either.Right -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(QuoteResponse.from(result.value))
            
            is Either.Left -> ResponseEntity
                .badRequest()
                .body(ErrorResponse(result.value.code, result.value.message))
        }
    }
    
    // ...existing methods...
}
```

**Magia do Spring Boot:**
- Detecta `suspend fun` automaticamente
- Gerencia coroutine context
- Não bloqueia thread pool HTTP

---

## Subtask 8.5: Processamento em Background (Opcional)

### Criar Quote Assíncrona com Polling
**Conceito:**
- POST retorna 202 Accepted com status CREATED
- Processa assíncrono em background
- Cliente faz GET para verificar status (polling)

```kotlin
@Service
class QuoteService(
    private val quoteRepository: QuoteRepository,
    private val riskScoreService: RiskScoreService
) {
    
    // Criar quote em background
    fun createQuoteAsync(request: QuoteRequest): Quote {
        // 1. Criar quote com status CREATED
        val quote = Quote(
            status = QuoteStatus.CREATED,
            preco = null, // Ainda não calculado
            request = request.normalized()
        )
        
        val savedQuote = quoteRepository.save(quote)
        
        // 2. Processar assíncrono (lançar coroutine)
        GlobalScope.launch {
            processQuoteAsync(savedQuote.id, request)
        }
        
        return savedQuote
    }
    
    // Processar em background
    private suspend fun processQuoteAsync(quoteId: UUID, request: QuoteRequest) {
        try {
            // Calcular risk score
            val riskScore = riskScoreService.calculateRiskScore(request.documento)
            
            // Calcular preço
            val priceResult = PriceCalculator.calculate(request)
            val adjustedPrice = if (riskScore > 70) priceResult.price * 1.10 else priceResult.price
            
            // Atualizar quote
            val updatedQuote = quoteRepository.findById(quoteId)!!.copy(
                status = if (adjustedPrice <= 300.0) QuoteStatus.APPROVED else QuoteStatus.REJECTED,
                preco = adjustedPrice,
                razoesRejeicao = if (adjustedPrice > 300.0) listOf("LIMIT_EXCEEDED") else emptyList()
            )
            
            quoteRepository.save(updatedQuote)
        } catch (e: Exception) {
            // Marcar como erro
            println("Erro ao processar quote $quoteId: ${e.message}")
        }
    }
}
```

**Fluxo:**
1. Cliente: `POST /quotes` → recebe 202 + id + status=CREATED
2. Sistema: processa em background (500ms de delay)
3. Cliente: `GET /quotes/{id}` → status=CREATED (ainda processando)
4. Cliente: `GET /quotes/{id}` → status=APPROVED (pronto!)

---

### 🎓 Checkpoint Task 8

**Teste coroutines:**

```bash
# Requisição demora ~500ms (delay do risk score)
POST /quotes
{
  "nome": "João",
  "documento": "123",
  "tipoSeguro": "AUTO",
  "veiculo": {"placa": "ABC1234"},
  "coberturas": []
}

# Observe logs:
# Risk score: 75
# Quote[id=..., status=APPROVED, preco=110.0, ...]
```

**Verifique:**
- ✅ Requisição retorna após ~500ms (não imediatamente)
- ✅ Thread HTTP não está bloqueada (consegue processar outras requisições)
- ✅ Risk score aparece no log
- ✅ Preço é ajustado se risk > 70

**Próxima task**: Eventos de domínio

---

# 🚀 TASK 9: Eventos de Domínio (In-Memory)

### 📚 Conceitos Aplicados
- ✅ **Conceito 4**: Lambdas (listeners como funções)

### 🎯 Objetivo
Publicar eventos internos quando quote é aprovada ou policy emitida (preparação para Kafka futuro).

---

## Subtask 9.1: Definir Eventos de Domínio

### DomainEvents.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/domain/event/DomainEvents.kt`

```kotlin
package com.seguro.quotes.domain.event

import com.seguro.quotes.domain.model.Quote
import com.seguro.quotes.domain.model.Policy
import java.time.LocalDateTime
import java.util.UUID

// Sealed class: todos os eventos possíveis
sealed class DomainEvent {
    abstract val eventId: UUID
    abstract val timestamp: LocalDateTime
}

// Evento: Quote aprovada
data class QuoteApprovedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val quoteId: UUID,
    val preco: Double,
    val tipoSeguro: String
) : DomainEvent()

// Evento: Quote rejeitada
data class QuoteRejectedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val quoteId: UUID,
    val razoes: List<String>
) : DomainEvent()

// Evento: Policy emitida
data class PolicyIssuedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val policyId: UUID,
    val quoteId: UUID,
    val policyNumber: String
) : DomainEvent()

// Extension: criar evento a partir de Quote
fun Quote.toApprovedEvent(): QuoteApprovedEvent? {
    return if (canGeneratePolicy()) {
        QuoteApprovedEvent(
            quoteId = id,
            preco = preco!!,
            tipoSeguro = request.tipoSeguro.name
        )
    } else null
}

fun Quote.toRejectedEvent(): QuoteRejectedEvent? {
    return if (status == QuoteStatus.REJECTED) {
        QuoteRejectedEvent(
            quoteId = id,
            razoes = razoesRejeicao
        )
    } else null
}

// Extension: criar evento a partir de Policy
fun Policy.toIssuedEvent(): PolicyIssuedEvent {
    return PolicyIssuedEvent(
        policyId = id,
        quoteId = quoteId,
        policyNumber = policyNumber
    )
}
```

---

## Subtask 9.2: EventPublisher (In-Memory)

### EventPublisher.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/infrastructure/event/EventPublisher.kt`

```kotlin
package com.seguro.quotes.infrastructure.event

import com.seguro.quotes.domain.event.DomainEvent
import org.springframework.stereotype.Component

// Type alias: listener é função que recebe evento
typealias EventListener = (DomainEvent) -> Unit

@Component
class EventPublisher {
    
    // Lista de listeners registrados
    private val listeners = mutableListOf<EventListener>()
    
    // Registrar listener
    fun subscribe(listener: EventListener) {
        listeners.add(listener)
    }
    
    // Publicar evento (notifica todos os listeners)
    fun publish(event: DomainEvent) {
        listeners.forEach { listener ->
            try {
                listener(event) // Chama lambda
            } catch (e: Exception) {
                println("Erro ao processar evento: ${e.message}")
            }
        }
    }
}
```

**Conceito:**
- `EventListener` = função lambda que recebe evento
- `subscribe` = registra listener (lambda)
- `publish` = chama todos os listeners

---

## Subtask 9.3: Registrar Listeners

### EventConfig.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/config/EventConfig.kt`

```kotlin
package com.seguro.quotes.config

import com.seguro.quotes.domain.event.*
import com.seguro.quotes.infrastructure.event.EventPublisher
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventConfig {
    
    // Registrar listeners na inicialização da aplicação
    @Bean
    fun eventListenersSetup(eventPublisher: EventPublisher) = CommandLineRunner {
        
        // Listener 1: Quote aprovada
        eventPublisher.subscribe { event ->
            if (event is QuoteApprovedEvent) {
                println("📬 Evento recebido: Quote ${event.quoteId} aprovada por R$ ${event.preco}")
                // Futuro: enviar email, notificar outro serviço, etc.
            }
        }
        
        // Listener 2: Quote rejeitada
        eventPublisher.subscribe { event ->
            if (event is QuoteRejectedEvent) {
                println("❌ Evento recebido: Quote ${event.quoteId} rejeitada - ${event.razoes}")
                // Futuro: enviar email de rejeição
            }
        }
        
        // Listener 3: Policy emitida
        eventPublisher.subscribe { event ->
            if (event is PolicyIssuedEvent) {
                println("🎉 Evento recebido: Policy ${event.policyNumber} emitida!")
                // Futuro: enviar apólice por email, gerar PDF, etc.
            }
        }
        
        println("✅ Event listeners registrados")
    }
}
```

---

## Subtask 9.4: Publicar Eventos no Service

### QuoteService.kt (refatorado)
**O que fazer:**
Publicar eventos após criar quote:

```kotlin
@Service
class QuoteService(
    private val quoteRepository: QuoteRepository,
    private val riskScoreService: RiskScoreService,
    private val eventPublisher: EventPublisher // ✅ injetar
) {
    
    suspend fun createQuote(request: QuoteRequest): Either<DomainError, Quote> {
        // ...existing validation and calculation...
        
        // Criar e salvar quote
        val savedQuote = quoteRepository.save(quote)
        
        // Publicar evento apropriado
        when {
            savedQuote.canGeneratePolicy() -> {
                savedQuote.toApprovedEvent()?.let { eventPublisher.publish(it) }
            }
            savedQuote.status == QuoteStatus.REJECTED -> {
                savedQuote.toRejectedEvent()?.let { eventPublisher.publish(it) }
            }
        }
        
        return Either.Right(savedQuote)
    }
}
```

---

### PolicyService.kt (refatorado)
**O que fazer:**
Publicar evento ao emitir policy:

```kotlin
@Service
class PolicyService(
    private val policyRepository: PolicyRepository,
    private val quoteRepository: QuoteRepository,
    private val eventPublisher: EventPublisher // ✅ injetar
) {
    
    fun issuePolicy(quoteId: UUID): Either<DomainError, Policy> {
        // ...existing validation and creation...
        
        // Salvar policy
        val savedPolicy = policyRepository.save(policy)
        
        // Publicar evento
        eventPublisher.publish(savedPolicy.toIssuedEvent())
        
        return Either.Right(savedPolicy)
    }
}
```

---

### 🎓 Checkpoint Task 9

**Teste eventos:**

```bash
# Criar quote aprovada
POST /quotes
{
  "nome": "João",
  "documento": "123",
  "tipoSeguro": "AUTO",
  "veiculo": {"placa": "ABC1234"},
  "coberturas": []
}

# Observe logs:
# 📬 Evento recebido: Quote <id> aprovada por R$ 100.0

# Criar quote rejeitada (forçar preço > 300)
POST /quotes
{
  "nome": "João",
  "documento": "123",
  "tipoSeguro": "AUTO",
  "veiculo": {"placa": "ABC1234"},
  "coberturas": ["ROUBO", "COLISAO", "ASSISTENCIA", "DANOS_TERCEIROS"]
}

# Observe logs:
# ❌ Evento recebido: Quote <id> rejeitada - [LIMIT_EXCEEDED]

# Emitir policy
POST /policies
{"quoteId": "<id-da-quote-aprovada>"}

# Observe logs:
# 🎉 Evento recebido: Policy POL-... emitida!
```

**Vantagens dos eventos:**
- Desacoplamento: listeners não sabem de onde vem evento
- Extensibilidade: adicionar novo listener sem modificar service
- Preparação para Kafka: mesma estrutura, só muda implementação

**Próxima task**: Design para microserviços

---

# 🚀 TASK 10: Design para Microserviços + Kafka

### 📚 Conceitos Aplicados
- ✅ **Conceito 12**: Boas Práticas (arquitetura)
- ✅ **Conceito 13**: Convenções (naming, versionamento)

### 🎯 Objetivo
Definir fronteiras de microserviços e preparar contratos para integração futura via Kafka.

---

## Subtask 10.1: Definir Fronteiras (Bounded Contexts)

### Arquitetura Proposta

```
┌─────────────────────┐
│   quote-service     │  (porta 8080)
│  - Criar cotação    │
│  - Calcular preço   │
│  - Aprovar/rejeitar │
│  - Consultar quote  │
└──────────┬──────────┘
           │
           │ Publica: quote.approved
           │           quote.rejected
           │
           ▼
     ┌───────────┐
     │   Kafka   │
     └─────┬─────┘
           │
           │ Consome: quote.approved
           │
           ▼
┌─────────────────────┐
│   policy-service    │  (porta 8081)
│  - Emitir apólice   │
│  - Consultar policy │
│  - Cancelar policy  │
└──────────┬──────────┘
           │
           │ Publica: policy.issued
           │           policy.cancelled
           │
           ▼
┌─────────────────────┐
│   risk-service      │  (porta 8082)
│  - Calcular score   │
│  - Consultar        │
│    histórico        │
└─────────────────────┘
```

---

## Subtask 10.2: Definir Tópicos Kafka

### KafkaTopics.kt
**O que fazer:**
Crie `src/main/kotlin/com/seguro/quotes/infrastructure/kafka/KafkaTopics.kt`

```kotlin
package com.seguro.quotes.infrastructure.kafka

// Convenção: {domain}.{entity}.{action}
object KafkaTopics {
    const val QUOTE_CREATED = "insurance.quote.created"
    const val QUOTE_APPROVED = "insurance.quote.approved"
    const val QUOTE_REJECTED = "insurance.quote.rejected"
    const val POLICY_ISSUED = "insurance.policy.issued"
    const val POLICY_CANCELLED = "insurance.policy.cancelled"
    const val RISK_SCORE_CALCULATED = "insurance.risk.calculated"
}
```

---

## Subtask 10.3: Adicionar Versionamento aos Eventos

### DomainEvents.kt (refatorado com versão)
**O que fazer:**
Adicionar campo `version` para compatibilidade futura:

```kotlin
sealed class DomainEvent {
    abstract val eventId: UUID
    abstract val timestamp: LocalDateTime
    abstract val version: String // ✅ versionamento
}

data class QuoteApprovedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val version: String = "1.0", // ✅
    val quoteId: UUID,
    val preco: Double,
    val tipoSeguro: String,
    val documento: String, // ✅ adicionar para consumer
    val coberturas: List<String> // ✅
) : DomainEvent()

data class PolicyIssuedEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    override val version: String = "1.0", // ✅
    val policyId: UUID,
    val quoteId: UUID,
    val policyNumber: String,
    val dataInicio: String, // ✅ ISO-8601 format
    val dataFim: String // ✅
) : DomainEvent()
```

**Por que versionar?**
- Evolução: adicionar campos sem quebrar consumers antigos
- Compatibilidade: consumer v1 ignora campos da v2
- Rollback: consumer pode detectar versão não suportada

---

## Subtask 10.4: Preparar API para Event-Driven

### QuoteService.kt (preparado para Kafka)
**O que fazer:**
Ajustar eventos para incluir todos os dados necessários:

```kotlin
@Service
class QuoteService(
    private val quoteRepository: QuoteRepository,
    private val riskScoreService: RiskScoreService,
    private val eventPublisher: EventPublisher
) {
    
    suspend fun createQuote(request: QuoteRequest): Either<DomainError, Quote> {
        // ...existing code...
        
        // Publicar evento com TODOS os dados necessários
        // (consumer não deve chamar API de volta)
        if (savedQuote.canGeneratePolicy()) {
            val event = QuoteApprovedEvent(
                quoteId = savedQuote.id,
                preco = savedQuote.preco!!,
                tipoSeguro = savedQuote.request.tipoSeguro.name,
                documento = savedQuote.request.documento, // ✅
                coberturas = savedQuote.request.coberturas.map { it.name } // ✅
            )
            eventPublisher.publish(event)
        }
        
        return Either.Right(savedQuote)
    }
}
```

**Princípio:**
- Evento deve conter **todos os dados** que consumer precisa
- Consumer **NÃO deve** chamar API de volta para buscar detalhes
- Evento = snapshot completo do que aconteceu

---

## Subtask 10.5: Documentar APIs dos Microserviços

### API_CONTRACTS.md
**O que fazer:**
Crie `API_CONTRACTS.md` na raiz do projeto:

```markdown
# Contratos de API - Seguros MVP

## quote-service (porta 8080)

### POST /quotes
Cria cotação, calcula preço, aprova/rejeita.

**Request:**
```json
{
  "nome": "string",
  "documento": "string",
  "email": "string?",
  "tipoSeguro": "AUTO | VIDA",
  "idade": "number?",
  "veiculo": {
    "placa": "string?",
    "modelo": "string?",
    "ano": "number?"
  },
  "cep": "string?",
  "coberturas": ["ROUBO", "COLISAO", ...]
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "status": "APPROVED | REJECTED",
  "preco": 150.0,
  "razoesRejeicao": [],
  "timestamp": "2026-02-11T10:30:00"
}
```

### GET /quotes/{id}
Consulta cotação por ID.

---

## policy-service (porta 8081)

### POST /policies
Emite apólice a partir de quote aprovada.

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

---

## Eventos Kafka

### insurance.quote.approved
```json
{
  "eventId": "uuid",
  "timestamp": "2026-02-11T10:30:00Z",
  "version": "1.0",
  "quoteId": "uuid",
  "preco": 150.0,
  "tipoSeguro": "AUTO",
  "documento": "12345678900",
  "coberturas": ["ROUBO", "COLISAO"]
}
```

**Consumers:**
- policy-service (emite apólice automaticamente)
- notification-service (envia email)

### insurance.policy.issued
```json
{
  "eventId": "uuid",
  "timestamp": "2026-02-11T10:35:00Z",
  "version": "1.0",
  "policyId": "uuid",
  "quoteId": "uuid",
  "policyNumber": "POL-AUTO-20260211-1234",
  "dataInicio": "2026-02-11",
  "dataFim": "2027-02-11"
}
```

**Consumers:**
- notification-service (envia apólice por email)
- billing-service (cria cobrança)
```

---

## Subtask 10.6: Checklist de Preparação para Microserviços

### ✅ Checklist

**Isolamento:**
- [ ] Cada serviço tem seu próprio banco de dados
- [ ] Serviços se comunicam apenas via eventos/API
- [ ] Sem referências diretas entre serviços

**Event-Driven:**
- [ ] Eventos contêm todos os dados necessários
- [ ] Versionamento implementado
- [ ] Idempotência garantida (eventId)

**Observabilidade:**
- [ ] Logs estruturados (JSON)
- [ ] Trace ID propagado entre serviços
- [ ] Métricas expostas (Prometheus)

**Resiliência:**
- [ ] Timeouts configurados
- [ ] Retry policy definida
- [ ] Circuit breaker (futuro)

**Segurança:**
- [ ] Dados sensíveis mascarados nos logs
- [ ] Autenticação entre serviços (futuro: mTLS)
- [ ] Validação de schema de eventos

---

### 🎓 Checkpoint Task 10

**Perguntas de arquitetura:**

1. **Por que separar quote-service e policy-service?**
   - R: Bounded contexts diferentes, escalabilidade independente

2. **Como policy-service sabe quando criar apólice?**
   - R: Consome evento `quote.approved` do Kafka

3. **E se policy-service estiver offline quando evento chegar?**
   - R: Kafka persiste evento, consumer processa quando voltar

4. **Como evitar processar mesmo evento duas vezes?**
   - R: Usar `eventId` como idempotency key

5. **Por que incluir `documento` no evento se já tenho `quoteId`?**
   - R: Consumer não deve chamar API de volta (autonomia)

---

## 🎉 Conclusão das 10 Tasks

**Parabéns! Você completou o MVP!** 🚀

### O que você implementou:

✅ **Task 1**: Domínio completo (enums, data classes, DTOs)
✅ **Task 2**: Controllers REST (stubs)
✅ **Task 3**: Validação com Either (sem exceptions)
✅ **Task 4**: Precificação funcional (collections, when)
✅ **Task 5**: Extensions + scope functions (normalização, mascaramento)
✅ **Task 6**: Repositórios in-memory (ConcurrentHashMap)
✅ **Task 7**: Camada de serviço + boas práticas
✅ **Task 8**: Coroutines (processamento assíncrono)
✅ **Task 9**: Eventos de domínio (in-memory)
✅ **Task 10**: Design para microserviços + Kafka

### Conceitos dos 13 itens aplicados:

1. ✅ Val vs Var (imutabilidade)
2. ✅ Null Safety (?, !!, ?:)
3. ✅ Data Classes (copy, equals, toString)
4. ✅ Funções e Lambdas (PricingStrategy, EventListener)
5. ✅ Extension Functions (maskDocument, normalized)
6. ✅ Scope Functions (apply, let, run, also)
7. ✅ Collections (map, filter, fold, sumOf)
8. ✅ When Expression (precificação, validação)
9. ✅ Either Pattern (erros tipados)
10. ✅ Coroutines (suspend, async, delay)
11. ✅ Interop Java (ConcurrentHashMap, Spring annotations)
12. ✅ Boas Práticas (package structure, separation of concerns)
13. ✅ Convenções (naming, evitar !!, require/check)

---

## 🚀 Próximos Passos (Além do MVP)

### Fase 2: Produção

1. **Migrar para PostgreSQL**
   - Spring Data JPA
   - Flyway migrations
   - Connection pooling

2. **Integrar Kafka real**
   - Spring Kafka
   - Schema Registry (Avro)
   - Dead Letter Queue

3. **Adicionar autenticação**
   - Spring Security
   - JWT tokens
   - API Keys

4. **Observabilidade**
   - Micrometer + Prometheus
   - Grafana dashboards
   - Distributed tracing (Jaeger)

5. **Testes**
   - Unit tests (MockK)
   - Integration tests (Testcontainers)
   - Contract tests (Pact)

6. **CI/CD**
   - GitHub Actions
   - Docker + Kubernetes
   - Helm charts

---

## 📚 Recursos de Estudo

### Livros
- "Kotlin in Action" (Dmitry Jemerov)
- "Effective Kotlin" (Marcin Moskała)

### Cursos
- Kotlin for Java Developers (Coursera)
- Spring Boot with Kotlin (Udemy)

### Documentação
- kotlinlang.org/docs
- spring.io/guides (Kotlin)

---

**Criado baseado em**: Requisitos MVP + 10 tasks progressivas + 13 conceitos Kotlin
**Autor**: GitHub Copilot
**Data**: Fevereiro 2026

