Aqui está um guia técnico detalhado em formato Markdown, estruturado para que um agente de IA ou um desenvolvedor possa replicar a implementação de Autenticação de Dois Fatores (2FA) do projeto Dumply em outro sistema Spring Boot.

---

### 📖 Guia de Implementação: Autenticação 2FA (TOTP)

Este documento descreve como implementar o fluxo de **Time-based One-Time Password (TOTP)** utilizando a biblioteca `googleauth`, integrado ao Spring Security.

### 1. Pré-requisitos e Dependências

Adicione a seguinte dependência ao seu `pom.xml`:

```xml
<dependency>
    <groupId>com.warrenstrange</groupId>
    <artifactId>googleauth</artifactId>
    <version>1.5.0</version>
</dependency>
```

### 2. Modelo de Dados (Entity)

Sua entidade de usuário precisa dos seguintes campos para suportar o 2FA:

```java
@Entity
public class User {
    // ... outros campos (id, email, password)

    private String secret2fa;          // Armazena a chave secreta TOTP
    private boolean is2faEnabled;      // Status da ativação
    private String disable2faCode;     // Código temporário para desativação via e-mail
}
```

### 3. Configuração de Segurança (Spring Security)

No `SecurityConfig`, os endpoints de login e verificação inicial devem ser públicos, enquanto a configuração do 2FA deve ser protegida.

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
    .requestMatchers(HttpMethod.POST, "/auth/2fa/verify").permitAll()
    .anyRequest().authenticated()
)
```

### 4. Lógica de Negócio (`AuthService`)

A implementação baseia-se na classe `GoogleAuthenticator`.

#### A. Inicialização (Setup)
Gera uma chave secreta e uma URL para o QR Code (formato `otpauth://`).

```java
private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

@Transactional
public Map<String, String> setup2FA(User user) {
    final GoogleAuthenticatorKey key = gAuth.createCredentials();
    user.setSecret2fa(key.getKey());
    userRepository.save(user);

    String qrCodeUrl = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
            "NomeDoApp", user.getEmail(), key.getKey(), "NomeDoApp");
    
    return Map.of("qrCodeUrl", qrCodeUrl);
}
```

#### B. Fluxo de Login em Duas Etapas
1.  **Login Primário:** Valida e-mail/senha. Se o 2FA estiver ativo, retorna um sinalizador em vez do token JWT.
2.  **Verificação 2FA:** Valida o código TOTP e emite o token final.

```java
// Passo 1: Login
if (user.is2faEnabled()) {
    return new ResponseDTO(null, true, user.getEmail()); // true indica "awaiting_2fa"
}
return new ResponseDTO(tokenService.generateToken(user));

// Passo 2: Verificação
public ResponseDTO verify2FA(String email, int code) {
    User user = userRepository.findByEmail(email).orElseThrow();
    if (gAuth.authorize(user.getSecret2fa(), code)) {
        return new ResponseDTO(tokenService.generateToken(user));
    }
    throw new BadCredentialsException("Código 2FA inválido");
}
```

#### C. Desativação Segura
Utiliza um código enviado por e-mail para confirmar a desativação do recurso.

```java
public void requestDisable2FACode(User user) {
    String code = String.valueOf((int)((Math.random() * 899999) + 100000));
    user.setDisable2faCode(code);
    userRepository.save(user);
    emailService.sendEmail(user.getEmail(), "Código de Desativação", code);
}
```

### 5. Endpoints (Controller)

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| `POST` | `/auth/login` | Login inicial. Se 2FA ativo, retorna `awaiting_2fa: true`. |
| `POST` | `/auth/2fa/verify` | Valida o código do app autenticador e gera o JWT. |
| `POST` | `/auth/2fa/setup` | Gera a chave secreta e retorna a URL do QR Code. |
| `POST` | `/auth/2fa/confirm` | Valida o primeiro código para ativar definitivamente o 2FA. |
| `POST` | `/auth/2fa/disable/request` | Solicita código de desativação via e-mail. |
| `POST` | `/auth/2fa/disable/confirm` | Valida código do e-mail e desativa o 2FA. |

### 6. Considerações de Auditoria e Segurança
*   **Logs:** Registre cada sucesso e falha de 2FA (uso de `AuditLogService`).
*   **Persistência:** O `secret2fa` deve ser tratado como dado sensível.
*   **Time-drift:** O TOTP depende do tempo sincronizado entre servidor e dispositivo do usuário. A biblioteca `googleauth` gerencia isso automaticamente dentro de uma janela de tolerância.