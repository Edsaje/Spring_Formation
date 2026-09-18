---
tags:
  - SpringBoot
  - Java
  - Securite
  - SpringSecurity
  - JWT
  - JJWT
  - RBAC
  - Microservices
  - REST
  - API
  - Cours
  - Visuel
date: 2026-09-18
subject: Java Spring Boot — Sécurité, Spring Security, Authentification JWT & Contrôle d'Accès par Rôles (RBAC)
course: 5.1 à 5.3 | Square Games — Sécurité, Spring Security, Authentification JWT & RBAC
status: In Progress
---

# 5.1 à 5.3 | Square Games — Sécurité, Spring Security, Authentification JWT & RBAC (Édition Masterclass Visuelle)

> [!INFO] **Informations sur le module**
> - **Itérations couvertes** :
>   - **5.1** : Concepts JWT & Configuration Spring Security (chaîne de filtres, mode stateless, CSRF, BCrypt).
>   - **5.2** : Authentification complète : génération du JWT (`/auth/login`), `JwtService`, `UserDetailsService` et filtre d'interception `JwtAuthenticationFilter`.
>   - **5.3** : Contrôle d'accès par rôles (RBAC), sécurisation granulaire avec `@PreAuthorize` et intégration décentralisée du token dans `square_games` (remplacement définitif de `X-UserId`).
> - **Objectif pédagogique** : Quitter le modèle naïf de confiance aveugle basé sur un en-tête `X-UserId` falsifiable pour bâtir une **architecture Zero-Trust industrielle**. Vous allez maîtriser la cryptographie des tokens signés (JWT), intercepter chaque requête HTTP avec les filtres de Spring Security, appliquer des règles d'autorisation fines par rôle (`ROLE_USER` / `ROLE_ADMIN`), et permettre à des microservices de valider l'identité des utilisateurs **de façon autonome et instantanée, sans aucun appel réseau bloquant**.

---

## 🗂️ Table des Matières

1. [[#1. La Révolution de la Sécurité : Du Pass Temporaire (X-UserId) au Passeport Biométrique (JWT)]]
   - [[#1.1 L'Analogie du Festival : Le Tampon Falsifiable vs Le Bracelet NFC Crypté]]
   - [[#1.2 Pourquoi l'entête X-UserId était une faille majeure (Spoofing d'Identité)]]
   - [[#1.3 Authentification vs Autorisation : Les Deux Piliers]]
   - [[#1.4 Architecture Stateful (Sessions/Cookies) vs Stateless (JWT/Bearer)]]
2. [[#2. Anatomie & Cryptographie d'un JSON Web Token (JWT) (5.1)]]
   - [[#2.1 Les 3 Composants : Header . Payload . Signature]]
   - [[#2.2 Le Header (Métadonnées de l'Algorithme)]]
   - [[#2.3 Le Payload (Claims Publiques & Claims Personnalisées)]]
   - [[#2.4 La Signature : La garantie mathématique d'intégrité (HMAC-SHA256)]]
   - [[#2.5 Lisibilité vs Falsification : Le grand malentendu sur le JWT]]
3. [[#3. Les Fondations de Spring Security 6 (5.1)]]
   - [[#3.1 La Chaîne de Filtres (SecurityFilterChain) : La Douane de l'Application]]
   - [[#3.2 L'Effet de spring-boot-starter-security (Le Verrouillage Immédiat)]]
   - [[#3.3 Configuration Moderne : SecurityConfig & Lambda DSL]]
   - [[#3.4 Le Stockage Sécurisé des Mots de Passe : BCrypt & Salage]]
4. [[#4. Génération du JWT & Endpoint /auth/login (5.2)]]
   - [[#4.1 La Bibliothèque JJWT (Java JWT)]]
   - [[#4.2 Conception du JwtService : Fabrication, Extraction & Vérification]]
   - [[#4.3 UserDetailsService & UserDetails : Le Pont vers la BDD]]
   - [[#4.4 AuthController & AuthenticationManager : La Cinématique du Login]]
   - [[#4.5 Diagramme de Séquence : Le Voyage du Login]]
5. [[#5. Interception & Validation : Le Filtre JwtAuthenticationFilter (5.2)]]
   - [[#5.1 Pourquoi OncePerRequestFilter ?]]
   - [[#5.2 Extraction du Header Authorization: Bearer]]
   - [[#5.3 Alimentation du SecurityContextHolder]]
   - [[#5.4 Positionnement du Filtre dans la Chaîne de Sécurité]]
   - [[#5.5 Diagramme de Flux : Traitement d'une Requête Protégée]]
6. [[#6. Contrôle d'Accès par Rôles (RBAC) & @PreAuthorize (5.3)]]
   - [[#6.1 Notion de Rôles vs Autorités (Le Préfixe ROLE_)]]
   - [[#6.2 Intégration du Rôle dans les Claims du JWT]]
   - [[#6.3 Activation de @EnableMethodSecurity]]
   - [[#6.4 Protection Déclarative des Routes sensibles (@PreAuthorize)]]
   - [[#6.5 Contrôle d'Accès au Niveau Ressource (Ownership SpEL)]]
7. [[#7. Intégration Décentralisée dans Square Games (5.3)]]
   - [[#7.1 La Révolution Architecturale : Plus Aucun Appel Réseau Inter-Service !]]
   - [[#7.2 Partage de la Clé Secrète Symétrique]]
   - [[#7.3 Validation Locale du Token dans l'App de Jeux]]
   - [[#7.4 Diagramme Comparatif : Architecture Itération 4 vs Itération 5]]
8. [[#8. Plan de Réalisation Pas-à-Pas (Checklist Méticuleuse)]]
9. [[#9. Pièges Classiques, Dépannage & Bonnes Pratiques (Gotchas)]]

---

## 1. La Révolution de la Sécurité : Du Pass Temporaire (X-UserId) au Passeport Biométrique (JWT)

### 1.1 L'Analogie du Festival : Le Tampon Falsifiable vs Le Bracelet NFC Crypté

Dans l'itération 4, pour savoir qui jouait, nous demandions simplement au client : *"Dis-moi qui tu es dans l'en-tête `X-UserId`"*.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ ANALOGIE : L'ACCÈS À UN FESTIVAL                                            │
├──────────────────────────────────────┬──────────────────────────────────────┤
│ 🏷️ L'ITÉRATION 4 (X-UserId)          │ 🎫 L'ITÉRATION 5 (JWT & Spring Sec)  │
├──────────────────────────────────────┼──────────────────────────────────────┤
│ - Vous écrivez votre nom sur un      │ - Vous présentez votre carte d'id    │
│   badge en carton au marqueur.       │   à l'accueil (Login).               │
│ - N'importe qui peut écrire l'UUID   │ - On vous pose un bracelet scellé    │
│   d'Alice et se faire passer pour    │   avec une puce cryptographique.     │
│   elle.                              │ - Impossible de le contrefaire       │
│ - Le manège doit appeler l'accueil   │   sans la clé secrète du festival.   │
│   à chaque tour pour vérifier si ce  │ - Les vigiles vérifient le bracelet  │
│   nom existe en base (RestClient).   │   instantanément sans appeler        │
│                                      │   l'accueil.                         │
└──────────────────────────────────────┴──────────────────────────────────────┘
```

---

### 1.2 Pourquoi l'entête X-UserId était une faille majeure (Spoofing d'Identité)

Dans l'itération 4 :
```http
POST /games HTTP/1.1
Host: localhost:8080
X-UserId: a4f88b50-983c-4e56-b089-281b379b3211
```
N'importe quel attaquant disposant d'un outil comme `curl`, Postman ou Bruno pouvait intercepter ou deviner l'UUID d'Alice et envoyer des requêtes à sa place ! `square_games` vérifiait seulement auprès de `user-service` si cet UUID **existait**, mais n'avait **absolument aucune preuve que la requête provenait réellement d'Alice**.

> [!CAUTION] **Faille d'Usurpation d'Identité (Identity Spoofing)**
> Vérifier qu'un identifiant existe n'a jamais prouvé que l'expéditeur de la requête est le détenteur légitime de cet identifiant. C'est le rôle fondamental de l'**authentification avec mot de passe et signature cryptographique**.

---

### 1.3 Authentification vs Autorisation : Les Deux Piliers

Ces deux termes sont souvent confondus mais régissent deux étapes distinctes :

```
             ┌────────────────────────────────────────────────────────┐
             │                   REQUÊTE ENTRANTE                     │
             └──────────────────────────┬─────────────────────────────┘
                                        │
                                        ▼
             ┌────────────────────────────────────────────────────────┐
             │  1. AUTHENTIFICATION : "Qui êtes-vous ?"              │
             ├────────────────────────────────────────────────────────┤
             │ - Vérification du couple login / mot de passe          │
             │ - Ou vérification de la signature du token JWT         │
             │ - Si ÉCHEC  ──► 401 Unauthorized                       │
             │ - Si SUCCÈS ──► Identité prouvée (ex: "Je suis Bob")   │
             └──────────────────────────┬─────────────────────────────┘
                                        │
                                        ▼
             ┌────────────────────────────────────────────────────────┐
             │  2. AUTORISATION : "Avez-vous le droit de faire ça ?"  │
             ├────────────────────────────────────────────────────────┤
             │ - Bob est authentifié, mais a-t-il le rôle ROLE_ADMIN ?│
             │ - Est-ce son tour de jouer sur la partie n°42 ?        │
             │ - Si ÉCHEC  ──► 403 Forbidden                          │
             │ - Si SUCCÈS ──► Exécution du contrôleur et de la BDD   │
             └────────────────────────────────────────────────────────┘
```

---

### 1.4 Architecture Stateful (Sessions/Cookies) vs Stateless (JWT/Bearer)

Traditionnellement, les sites Web utilisent des **sessions côté serveur** :

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant Server as Serveur Web (Stateful)
    participant RAM as Mémoire / Session Store
    
    Client->>Server: POST /login (user, pass)
    Server->>RAM: Crée Session #ABC123XYZ en RAM
    Server-->>Client: 200 OK + Set-Cookie: JSESSIONID=ABC123XYZ
    
    Client->>Server: GET /profil (Cookie: JSESSIONID=ABC123XYZ)
    Server->>RAM: Cherche #ABC123XYZ en RAM
    Server-->>Client: Données du profil
```

#### Pourquoi ce modèle échoue-t-il dans les microservices et les API REST ?
1. **Passage à l'échelle impossible (Scalabilité horizontale)** : Si vous avez 5 instances de votre serveur derrière un Load Balancer, la requête n°2 peut atterrir sur le Serveur B qui ne possède pas la session en RAM !
2. **Consommation mémoire serveur** : Avec 100 000 utilisateurs connectés, le serveur sature sa RAM pour stocker les sessions.
3. **Complexité multi-domaines** : Les cookies posent de sérieux problèmes de partage inter-domaines et de sécurité CSRF.

#### La solution moderne : Le mode STATELESS (Sans État) avec JWT
Le serveur ne stocke **RIEN**. L'état de l'utilisateur est embarqué **dans le token lui-même**. Le client le conserve (en localStorage, mémoire ou client HTTP) et le transmet dans l'en-tête standard :
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...
```
Le serveur n'a qu'à valider mathématiquement la signature du token à chaque requête. S'il est valide, il fait confiance aux données qu'il contient.

---

## 2. Anatomie & Cryptographie d'un JSON Web Token (JWT) (5.1)

Un JWT est une chaîne de caractères compacte, sécurisée pour les URL, formée de **trois segments distincts séparés par des points (`.`)** :

$$\text{JWT} = \underbrace{\text{Header}}_{\text{Base64URL}} \ . \ \underbrace{\text{Payload}}_{\text{Base64URL}} \ . \ \underbrace{\text{Signature}}_{\text{Binaire crypté Base64URL}}$$

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJBbGljZSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE3MTAwMDAwMDAsImV4cCI6MTcxMDA4NjQwMH0.D9z...Signature...
└───┬───────────────────────────────┘.└───┬────────────────────────────────────────────────────────────────────────────────────────┘.└───┬─────────────┘
    │                                     │                                                                                               │
    ▼                                     ▼                                                                                               ▼
1. HEADER                             2. PAYLOAD                                                                                      3. SIGNATURE
```

---

### 2.1 Les 3 Composants : Header . Payload . Signature

Ouvrez le site [jwt.io](https://jwt.io) : lorsque vous y collez un token, il est immédiatement décodé en clair sous vos yeux !

#### A. Le Header (En-tête)
Il décrit le type de jeton et l'algorithme cryptographique utilisé :
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```
* `alg: "HS256"` indique HMAC avec SHA-256 (clé secrète symétrique).

#### B. Le Payload (Les Claims / Déclarations)
Il contient les informations sur l'utilisateur et les métadonnées du token :
```json
{
  "sub": "alice",
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "roles": ["ROLE_USER"],
  "iat": 1726650000,
  "exp": 1726736400
}
```
* **Claims réservées standards** :
  - `sub` (*Subject*) : L'identifiant principal (nom d'utilisateur ou email).
  - `iat` (*Issued At*) : Date/heure de création en timestamp UNIX (secondes).
  - `exp` (*Expiration Time*) : Date/heure limite de validité. Au-delà, le token est rejeté.
* **Claims personnalisées** :
  - `userId` : L'UUID interne de l'utilisateur.
  - `roles` : Les rôles de l'utilisateur (`ROLE_USER`, `ROLE_ADMIN`).

#### C. La Signature (Le Sceau Cryptographique)
C'est ici que réside toute la sécurité du JWT. Elle est calculée par la formule :
$$\text{Signature} = \text{HMAC-SHA256}\Big(\text{Base64URL}(\text{Header}) + "." + \text{Base64URL}(\text{Payload}), \ \mathbf{Cl\acute{e}\ Secr\grave{e}te}\Big)$$

---

### 2.2 Lisibilité vs Falsification : Le grand malentendu sur le JWT

> [!WARNING] **Attention : Un JWT n'est PAS chiffré par défaut, il est SIGNÉ !**
> - **Chiffrement** = Cacher l'information aux yeux de tous (seul celui qui a la clé peut lire).
> - **Signature** = Garantir que l'information n'a pas été modifiée depuis son émission.
> 
> Tout le monde peut lire le contenu d'un JWT (le Payload est simplement encodé en Base64, pas chiffré). **Ne mettez JAMAIS de mot de passe, de numéro de carte bancaire ou de données ultra-secrètes dans le Payload d'un JWT !**

#### Pourquoi personne ne peut modifier son rôle de `ROLE_USER` en `ROLE_ADMIN` ?
Si un pirate modifie le Payload pour s'auto-attribuer `ROLE_ADMIN` :
1. Le Payload change $\rightarrow$ la signature calculée par le serveur avec la **clé secrète** ne correspondra plus du tout à la signature envoyée par le pirate.
2. Le serveur rejettera instantanément la requête avec une erreur `SignatureException` (Code HTTP **401 Unauthorized**).
3. Le pirate ne peut pas recalculer la signature car **seul le serveur connaît la clé secrète** !

---

## 3. Les Fondations de Spring Security 6 (5.1)

### 3.1 La Chaîne de Filtres (SecurityFilterChain) : La Douane de l'Application

Dans Spring Boot, une requête HTTP n'arrive jamais directement dans votre `@RestController`. Elle traverse d'abord une succession de filtres appelée la **`SecurityFilterChain`** :

```mermaid
flowchart TD
    ClientReq([Requête HTTP du Client]) --> F1[CorsFilter]
    F1 --> F2[CsrfFilter - Désactivé en Stateless]
    F2 --> F3[JwtAuthenticationFilter - Notre Filtre Personnalisé !]
    F3 --> F4[UsernamePasswordAuthenticationFilter]
    F4 --> F5[AuthorizationFilter - Vérification des rôles @PreAuthorize]
    F5 --> Controller[GameController / UserController]
```

Chaque filtre a une mission précise :
- Soit il laisse passer la requête vers le filtre suivant (`filterChain.doFilter(request, response)`).
- Soit il bloque immédiatement la requête en renvoyant un code d'erreur HTTP (`401` ou `403`).

---

### 3.2 L'Effet de spring-boot-starter-security (Le Verrouillage Immédiat)

Dès que vous ajoutez cette dépendance dans votre `pom.xml` :
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
Sans aucune ligne de configuration supplémentaire, Spring Security active sa configuration de sécurité par défaut :
1. **Tous les endpoints** sont instantanément verrouillés (renvoient `401 Unauthorized` ou redirigent vers une page HTML de login générée automatiquement).
2. Un utilisateur par défaut `user` est créé avec un mot de passe aléatoire affiché dans la console au démarrage (`Using generated security password: ...`).
3. La protection CSRF et les sessions HTTP par Cookie sont activées par défaut.

Notre première mission en 5.1 est de **remplacer cette configuration par défaut** par notre propre configuration adaptée aux API REST Stateless.

---

### 3.3 Configuration Moderne : SecurityConfig & Lambda DSL

Depuis Spring Security 6 (Spring Boot 3 et 4), les anciennes classes comme `WebSecurityConfigurerAdapter` sont totalement supprimées. La configuration se fait via un bean `SecurityFilterChain` et des expressions lambda fluides (**Lambda DSL**).

#### Implémentation de `SecurityConfig.java` :
```java
package com.hibouxe.users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. Désactiver la protection CSRF (inutile pour les API REST utilisant des tokens Bearer)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Désactiver les sessions HTTP côté serveur (Mode 100% Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Règles de routage des requêtes HTTP
                .authorizeHttpRequests(auth -> auth
                        // Laisser libre accès à la route de login et de documentation Swagger
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users").permitAll() // Pour permettre l'inscription
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Tout le reste nécessite un token authentifié
                        .anyRequest().authenticated()
                )
                .build();
    }

    // 4. Bean de hachage de mot de passe
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

> [!NOTE] **Pourquoi désactiver CSRF (`csrf.disable()`) ?**
> Les attaques CSRF (*Cross-Site Request Forgery*) exploitent le fait que les navigateurs renvoient automatiquement les **cookies** à chaque requête vers un domaine. Comme notre API fonctionne en mode **Stateless avec des tokens JWT** stockés côté client et envoyés manuellement dans le header `Authorization`, les navigateurs n'envoient rien automatiquement. Le risque CSRF n'existe pas.

---

### 3.4 Le Stockage Sécurisé des Mots de Passe : BCrypt & Salage

En tant que développeur responsable, **vous ne devez JAMAIS stocker de mot de passe en texte clair dans une base de données**. Si votre base est compromise ou fuitée, les comptes de tous vos utilisateurs seraient immédiatement piratés.

#### Pourquoi `BCryptPasswordEncoder` ?
1. **Algorithme à sens unique (Hachage)** : Il est mathématiquement impossible de retrouver le mot de passe original à partir du hash.
2. **Salage automatique (Salt)** : BCrypt génère automatiquement une chaîne aléatoire (le sel) qu'il incorpore au mot de passe avant de le hacher. Ainsi, deux utilisateurs ayant le même mot de passe `"secret123"` auront **deux empreintes hash totalement différentes en base**. Cela neutralise les attaques par dictionnaires précalculés (*Rainbow Tables*).
3. **Facteur de coût adaptatif (Work Factor)** : BCrypt est volontairement lent à calculer (quelques dizaines de millisecondes) pour empêcher les attaques par force brute industrielle.

```java
// Exemple d'utilisation dans votre service de création d'utilisateur :
String motDePasseEnClair = dto.password();
String motDePasseHashe = passwordEncoder.encode(motDePasseEnClair);

// En base de données, on sauvegarde :
// $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

---

## 4. Génération du JWT & Endpoint `/auth/login` (5.2)

### 4.1 La Bibliothèque JJWT (Java JWT)

Pour manipuler les JWT de manière élégante et performante, la bibliothèque de référence en Java est **JJWT** (développée par Okta/jwtk).

Ajoutez les 3 dépendances indispensables dans votre `pom.xml` :
```xml
<!-- API standard JJWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>

<!-- Moteur d'implémentation d'exécution -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- Sérialiseur / Désérialiseur JSON basé sur Jackson -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

---

### 4.2 Conception du `JwtService` : Fabrication, Extraction & Vérification

Le `JwtService` est le composant central qui encapsule toute la logique mathématique des tokens.

#### Les 3 méthodes requises par le sujet :
1. `generateToken(username, roles, userId)`
2. `extractUsername(token)`
3. `isTokenValid(token, userDetails)`

```java
package com.hibouxe.users.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret:MonSecretUltraSecuriseAvecAuMoins32CaracteresPourSha256!}")
    private String secretKey;

    @Value("${jwt.expiration-ms:86400000}") // 24 heures par défaut
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, UUID userId, List<String> roles) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId.toString())
                .claim("roles", roles)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
```

---

### 4.3 `UserDetailsService` & `UserDetails` : Le Pont vers la BDD

Spring Security a besoin d'une interface standard pour charger un utilisateur depuis votre stockage : `UserDetailsService`.

```java
package com.hibouxe.users.service;

import com.hibouxe.users.dao.UserRepository;
import com.hibouxe.users.entity.UserEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + username));

        return new User(
                user.getUsername(),
                user.getPassword(), // Mot de passe haché avec BCrypt
                List.of(new SimpleGrantedAuthority(user.getRole())) // ex: "ROLE_USER"
        );
    }
}
```

---

### 4.4 `AuthController` & `AuthenticationManager` : La Cinématique du Login

L'endpoint de connexion reçoit un DTO de login et délègue la vérification du mot de passe à l'`AuthenticationManager` de Spring Security.

```java
package com.hibouxe.users.controller;

import com.hibouxe.users.dao.UserRepository;
import com.hibouxe.users.dto.LoginRequestDto;
import com.hibouxe.users.dto.LoginResponseDto;
import com.hibouxe.users.entity.UserEntity;
import com.hibouxe.users.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        try {
            // 1. Authentifie les identifiants via Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            // 2. Si aucune exception n'a été levée, les identifiants sont corrects !
            UserEntity user = userRepository.findByUsername(request.username())
                    .orElseThrow();

            // 3. Génère le JWT
            String token = jwtService.generateToken(user.getUsername(), user.getId(), List.of(user.getRole()));

            return ResponseEntity.ok(new LoginResponseDto(token, user.getId(), user.getUsername(), user.getRole()));

        } catch (BadCredentialsException e) {
            // Identifiants erronés -> Sanction HTTP 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Nom d'utilisateur ou mot de passe incorrect."));
        }
    }
}
```

---

### 4.5 Diagramme de Séquence : Le Voyage du Login

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client (Bruno / Web)
    participant AC as AuthController (/auth/login)
    participant AM as AuthenticationManager
    participant UDS as CustomUserDetailsService
    participant BDD as Base de Données
    participant PE as BCryptPasswordEncoder
    participant JWT as JwtService
    
    Client->>AC: POST /auth/login {username: "Alice", password: "password123"}
    AC->>AM: authenticate(UsernamePasswordAuthToken)
    AM->>UDS: loadUserByUsername("Alice")
    UDS->>BDD: SELECT * FROM users WHERE username='Alice'
    BDD-->>UDS: UserEntity(hash = "$2a$10$X8z...")
    UDS-->>AM: UserDetails
    AM->>PE: matches("password123", "$2a$10$X8z...")
    PE-->>AM: true (Mot de passe valide)
    AM-->>AC: Authentication validée
    AC->>JWT: generateToken("Alice", uuid, ["ROLE_USER"])
    JWT-->>AC: "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
    AC-->>Client: 200 OK {token: "eyJhbGciOiJIUzI1..."}
```

---

## 5. Interception & Validation : Le Filtre `JwtAuthenticationFilter` (5.2)

### 5.1 Pourquoi `OncePerRequestFilter` ?

Dans l'écosystème Spring MVC, une même requête peut parfois être ré-aiguillée (dispatch interne, forward ou error handling). La classe abstraite `OncePerRequestFilter` de Spring vous offre une garantie absolue : **votre filtre ne s'exécutera qu'une seule et unique fois par requête HTTP**, évitant de re-décoder le token inutilement.

---

### 5.2 Extraction du Header & Hydratation du `SecurityContextHolder`

```java
package com.hibouxe.users.config;

import com.hibouxe.users.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Vérifier si l'en-tête est présent et commence par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Laisser passer (les routes permitAll fonctionneront, les autres seront bloquées plus tard)
            return;
        }

        final String jwt = authHeader.substring(7); // Supprimer "Bearer "
        final String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            // Token corrompu ou signature invalide -> poursuivre la chaîne (SecurityContext vide -> rejet 401)
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Si un utilisateur est extrait et n'est pas déjà authentifié dans ce thread
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 3. Valider le token
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities() // Charge les rôles extraits !
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 4. ALIMENTATION DU SECURITY CONTEXT : L'utilisateur est désormais officiellement authentifié !
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

---

### 5.3 Positionnement du Filtre dans la Chaîne de Sécurité

Dans votre `SecurityConfig.java`, insérez votre filtre **avant** le filtre de formulaire standard :

```java
http
    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

---

## 6. Contrôle d'Accès par Rôles (RBAC) & `@PreAuthorize` (5.3)

### 6.1 Notion de Rôles vs Autorités (Le Préfixe `ROLE_`)

Dans Spring Security :
- Une **Autorité** (*Authority*) est une permission simple, par exemple `"READ_USERS"`, `"DELETE_GAME"`.
- Un **Rôle** (*Role*) est un regroupement d'autorités, traditionnellement préfixé par **`ROLE_`**, par exemple `"ROLE_USER"`, `"ROLE_ADMIN"`.

> [!IMPORTANT] **La Convention Spring Security sur les Rôles**
> Dans votre BDD et votre JWT, enregistrez toujours : `"ROLE_USER"` ou `"ROLE_ADMIN"`.  
> Dans vos annotations `@PreAuthorize`, utilisez :
> - `hasRole('ADMIN')` (Spring ajoute automatiquement `ROLE_` en interne).
> - OU `hasAuthority('ROLE_ADMIN')`.

---

### 6.2 Activation de `@EnableMethodSecurity`

Pour utiliser les annotations de sécurité directement au-dessus des méthodes de vos contrôleurs, ajoutez `@EnableMethodSecurity` sur votre `SecurityConfig` :

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // <-- ACTIVE @PreAuthorize, @PostAuthorize, @Secured
public class SecurityConfig { ... }
```

---

### 6.3 Protection Déclarative des Routes Sensibles

Dans `UserController.java` :

```java
@RestController
@RequestMapping("/users")
public class UserController {

    // 1. Consultation de la liste globale : Administrateur seulement
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers();
    }

    // 2. Suppression d'un utilisateur : Administrateur seulement
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // 3. Consultation d'un profil : L'admin OU le propriétaire légitime du profil
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.principal.username or @userSecurity.isOwner(authentication, #id)")
    public ResponseEntity<?> getUser(@PathVariable UUID id) {
        return userService.getUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

Si un utilisateur avec le rôle `ROLE_USER` tente d'appeler `DELETE /users/123`, Spring Security intercepte la requête avant même l'entrée dans la méthode et renvoie immédiatement :  
**`403 Forbidden`** !

---

## 7. Intégration Décentralisée dans Square Games (5.3)

### 7.1 La Révolution Architecturale : Plus Aucun Appel Réseau Inter-Service !

Observez le gain de performance et de résilience entre l'itération 4 et l'itération 5 :

```mermaid
flowchart TD
    subgraph Iteration4[" ❌ Itération 4 : Couplage Fort & Latence Réseau "]
        I4_Client([Client]) -->|1. Requête de Jeu avec X-UserId| I4_SG[square_games : 8080]
        I4_SG -->|2. Appel Synchrone GET /users/id/valid| I4_US[user-service : 8081]
        I4_US -->|3. Réponse: true| I4_SG
        I4_SG -->|4. Action de jeu exécutée| I4_Client
    end
    
    subgraph Iteration5[" ✅ Itération 5 : Architecture Décentralisée Zero-Trust "]
        I5_Client([Client]) -->|1. Requête avec Authorization: Bearer JWT| I5_SG[square_games : 8080]
        I5_SG -->|2. Validation Locale de la Signature avec la Clé Secrète| I5_SG
        I5_SG -->|3. Extraction instantanée de l'UUID du joueur depuis le JWT| I5_SG
        I5_SG -->|4. Action exécutée en quelques microsecondes !| I5_Client
    end
```

#### Pourquoi ce changement est fondamental :
1. **Zéro Latence Réseau** : `square_games` n'a plus besoin d'effectuer un aller-retour HTTP vers `user-service` à chaque coup joué ou chaque consultation de partie.
2. **Haute Disponibilité** : Même si `user-service` tombe en panne (crash serveur, maintenance BDD), les joueurs déjà connectés peuvent **continuer à jouer sur `square_games` sans aucune interruption** !
3. **Sécurité Totale** : Le token est mathématiquement infalsifiable grâce à la clé secrète partagée.

---

### 7.2 Partage de la Clé Secrète dans `application.properties`

Dans `user-service/src/main/resources/application.properties` :
```properties
jwt.secret=MaCleSecreteCommunePartageeEntreLesMicroservices123456789!
```

Dans `square_games/src/main/resources/application.properties` :
```properties
jwt.secret=MaCleSecreteCommunePartageeEntreLesMicroservices123456789!
```

---

### 7.3 Validation Locale du Token dans l'App de Jeux

`square_games` embarque son propre `JwtAuthenticationFilter` léger :
1. Il lit l'en-tête `Authorization: Bearer <token>`.
2. Il valide la signature avec `jwt.secret`.
3. Il extrait le claim `userId` et le place dans le contexte d'authentification.
4. Dans `GameController.java`, vous récupérez l'identifiant du joueur directement :

```java
@PostMapping("/games")
public ResponseEntity<?> createGame(
        Authentication authentication,
        @RequestBody GameCreationParams params) {

    // L'UUID provient de façon infalsifiable du JWT validé !
    UUID userId = UUID.fromString(authentication.getName()); // ou custom principal

    Game game = gameService.createNewGame(userId, params);
    return ResponseEntity.status(HttpStatus.CREATED).body(game);
}
```

---

## 8. Plan de Réalisation Pas-à-Pas (Checklist Méticuleuse)

### Phase 1 : Sécuriser `user-service` (5.1)
- [ ] Ajouter `spring-boot-starter-security` au `pom.xml` de `user-service`.
- [ ] Ajouter le champ `password` et le champ `role` (défaut `ROLE_USER`) à `UserEntity`.
- [ ] Créer `SecurityConfig` avec `csrf.disable()`, `SessionCreationPolicy.STATELESS` et `BCryptPasswordEncoder`.
- [ ] Mettre à jour `UserServiceImpl.createUser` pour encoder le mot de passe avec `passwordEncoder.encode()`.

### Phase 2 : Système JWT & Login (5.2)
- [ ] Ajouter les 3 dépendances JJWT (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`).
- [ ] Définir `jwt.secret` et `jwt.expiration-ms` dans `application.properties`.
- [ ] Créer `JwtService` avec les méthodes de génération, d'extraction et de validation.
- [ ] Implémenter `CustomUserDetailsService` implémentant `UserDetailsService`.
- [ ] Déclarer le bean `AuthenticationManager` dans `SecurityConfig`.
- [ ] Créer `AuthController` avec `POST /auth/login` retournant `{ "token": "..." }`.

### Phase 3 : Filtre & Interception (5.2)
- [ ] Créer `JwtAuthenticationFilter` étendant `OncePerRequestFilter`.
- [ ] Extraire l'en-tête `Authorization: Bearer <token>`, valider le token et remplir le `SecurityContextHolder`.
- [ ] Enregistrer `jwtAuthenticationFilter` avant `UsernamePasswordAuthenticationFilter` dans `SecurityConfig`.

### Phase 4 : Rôles & RBAC (5.3)
- [ ] Ajouter `@EnableMethodSecurity` sur `SecurityConfig`.
- [ ] Protéger les routes administratives avec `@PreAuthorize("hasRole('ADMIN')")`.
- [ ] Tester avec Bruno : un utilisateur `ROLE_USER` reçoit `403 Forbidden` sur `DELETE /users/{id}`.

### Phase 5 : Intégration dans `square_games` (5.3)
- [ ] Ajouter `spring-boot-starter-security` et JJWT dans `square_games/pom.xml`.
- [ ] Configurer la même clé `jwt.secret` dans `square_games`.
- [ ] Implémenter le filtre JWT dans `square_games` pour lire le token et injecter le joueur.
- [ ] Remplacer `@RequestHeader("X-UserId")` par l'injection de l'utilisateur authentifié depuis le `SecurityContext`.
- [ ] Supprimer l'appel réseau `RestClient` devenu inutile !

---

## 9. Pièges Classiques, Dépannage & Bonnes Pratiques (Gotchas)

### ⚠️ Gotcha n°1 : La clé secrète trop courte (WeakKeyException)
L'algorithme `HMAC-SHA256` exige impérativement une clé d'une longueur minimale de **256 bits (soit au moins 32 octets / caractères ASCII)**.  
Si vous mettez une clé courte comme `"mon_secret"`, JJWT lèvera immédiatement une exception au démarrage :
```text
io.jsonwebtoken.security.WeakKeyException: The specified key byte array is 80 bits which is not secure enough for any JWT HMAC-SHA algorithm: The JWT JWA Specification (RFC 7518, Section 3.2) states that keys used with HMAC-SHA algorithms MUST have a size >= 256 bits...
```
👉 **Solution** : Utilisez une clé secrète longue et robuste (au moins 32 caractères aléatoires).

---

### ⚠️ Gotcha n°2 : L'espace manquant après `Bearer`
Dans les clients HTTP (Postman, Bruno, curl), l'en-tête doit strictement respecter la casse et l'espace :
```http
Authorization: Bearer eyJhbGci...
```
Si vous écrivez `Bearer_token` ou `bearer token`, votre condition `authHeader.startsWith("Bearer ")` sera fausse et le filtre ignorera le token !

---

### ⚠️ Gotcha n°3 : La confusion `hasRole` vs `hasAuthority`
Dans Spring Security :
- Si votre autorité en base s'appelle `"ROLE_ADMIN"` :
  - `@PreAuthorize("hasRole('ADMIN')")` $\rightarrow$ **FONCTIONNE** (Spring ajoute automatiquement le préfixe `ROLE_`).
  - `@PreAuthorize("hasRole('ROLE_ADMIN')")` $\rightarrow$ **ÉCHOUE** (Spring cherchera `ROLE_ROLE_ADMIN`).
  - `@PreAuthorize("hasAuthority('ROLE_ADMIN')")` $\rightarrow$ **FONCTIONNE** (Vérification exacte sans modification).

---

### ⚠️ Gotcha n°4 : Les routes Swagger et Actuator bloquées en 401
Dès que Spring Security est actif, Swagger UI (`/swagger-ui/**`) et la spec OpenAPI (`/v3/api-docs/**`) sont bloqués par défaut !  
👉 **Solution** : Ajoutez explicitement `.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()` dans votre `SecurityFilterChain`.

---

### ⚠️ Gotcha n°5 : Ne pas oublier de capturer l'expiration du Token
Lorsque le token expire, JJWT lève une `ExpiredJwtException`. Dans votre filtre, capturez cette exception pour éviter un crash 500 et laisser Spring Security renvoyer proprement une sanction **401 Unauthorized**.

---

*Félicitations ! Avec ce cours complet, vous avez entre les mains l'architecture de sécurité standard utilisée par les leaders de l'industrie technologique pour protéger les microservices modernes.*
