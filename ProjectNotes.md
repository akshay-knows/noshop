mkdir -p config,constant,dto,enums,exception,response,util,validation

mkdir -p config,controller,service,repository,entity,dto,mapper,security,jwt,exception,client,event,util



Success → ApiResponse<T>

Error → ErrorResponse\\



|Success<br /><br />Client<br />   │<br />   ▼<br />Controller<br />   │<br />   ▼<br />Service<br />   │<br />   ▼<br />ApiResponse<T><br />   │<br />   ▼<br />JSON|\*\*Failure <br />\*\*Client<br />   │<br />   ▼<br />Controller<br />   │<br />   ▼<br />Service<br />   │<br /> throws BusinessException<br />   │<br />   ▼<br />GlobalExceptionHandler<br />   │<br />   ▼<br />ErrorResponse<br />   │<br />   ▼<br />JSON|
|-|-|



# **Admin Server** -- Admin Server never receives business requests.



It is only for monitoring.

|Angular<br />                       │<br />                       ▼<br />                 API Gateway<br />                       │<br />      ┌────────────────┼─────────────────┐<br />      │                │                 │                         <br />      ▼                ▼                 ▼<br /> Auth Service    Product Service   Cart Service<br />      │                │                 │<br />      └────────────────┼─────────────────┘<br />                       │<br />                Eureka Server<br />                       ▲<br />                       │<br />                Admin Server<br />                       │<br />        Monitors Every Service<br /><br />|
|-|



**SpringSecurity --**



**Spring Security understands only one type of user:**



*org.springframework.security.core.userdetails.UserDetails*



**So we need an adapter.**



**Database User**

&#x20;     **│**

&#x20;     **▼**

**CustomUserDetails**

&#x20;     **│**

&#x20;     **▼**

**Spring Security**







**JWT Authentication Architecture**

&#x20;                        **┌──────────────────────────┐**

&#x20;                        **│        Frontend          │**

&#x20;                        **│ (Angular / React / App) │**

&#x20;                        **└─────────────┬────────────┘**

&#x20;                                      **│**

&#x20;                   **Login Request       │**

&#x20;       **POST /api/v1/auth/login         │**

&#x20;**Email + Password                       │**

&#x20;                                      **▼**

&#x20;                    **┌──────────────────────────────┐**

&#x20;                    **│         API Gateway          │**

&#x20;                    **└─────────────┬────────────────┘**

&#x20;                                  **│**

&#x20;                                  **▼**

&#x20;                    **┌──────────────────────────────┐**

&#x20;                    **│         Auth Service         │**

&#x20;                    **└─────────────┬────────────────┘**

&#x20;                                  **│**

&#x20;                   **AuthenticationManager**

&#x20;                                  **│**

&#x20;                                  **▼**

&#x20;                   **DaoAuthenticationProvider**

&#x20;                                  **│**

&#x20;                                  **▼**

&#x20;                 **CustomUserDetailsService**

&#x20;                                  **│**

&#x20;                                  **▼**

&#x20;                        **UserRepository**

&#x20;                                  **│**

&#x20;                                  **▼**

&#x20;                             **MySQL Database**

&#x20;                                  **│**

&#x20;                     **User Found \& Password Match?**

&#x20;                                  **│**

&#x20;                        **Yes        │        No**

&#x20;                         **│         │**

&#x20;                         **▼         ▼**

&#x20;                   **JwtService    Exception**

&#x20;                         **│**

&#x20;                 **Generate JWT Token**

&#x20;                         **│**

&#x20;                         **▼**

&#x20;                 **Return JWT to Client**

&#x20;                         **│**

&#x20;                         **▼**

&#x20;            **Client stores JWT (LocalStorage/**

&#x20;            **SessionStorage/HttpOnly Cookie)**

**Every Request After Login**

**Frontend**

&#x20;    **│**

&#x20;    **│ GET /api/v1/products**

&#x20;    **│**

&#x20;    **│ Authorization:**

&#x20;    **│ Bearer eyJhbGciOiJIUzI1NiJ9...**

&#x20;    **▼**

**──────────────────────────────────────────────────────────**

&#x20;                   **API Gateway**

**──────────────────────────────────────────────────────────**

&#x20;    **│**

&#x20;    **▼**

&#x20;**Product Service (or Cart/Order/etc.)**

&#x20;    **│**

&#x20;    **▼**

**JwtAuthenticationFilter**

&#x20;    **│**

&#x20;    **├──────── Read Authorization Header**

&#x20;    **│**

&#x20;    **├──────── Starts with "Bearer " ?**

&#x20;    **│**

&#x20;    **├──────── Extract JWT**

&#x20;    **│**

&#x20;    **├──────── JwtService.validateToken()**

&#x20;    **│**

&#x20;    **├──────── Extract Username**

&#x20;    **│**

&#x20;    **├──────── Load User Details**

&#x20;    **│**

&#x20;    **├──────── Create Authentication Object**

&#x20;    **│**

&#x20;    **├──────── Store in SecurityContextHolder**

&#x20;    **│**

&#x20;    **▼**

**Controller**

&#x20;    **│**

&#x20;    **▼**

**Business Logic Executes**

**Internal Spring Security Flow**

&#x20;                   **HTTP Request**

&#x20;                         **│**

&#x20;                         **▼**

&#x20;               **SecurityFilterChain**

&#x20;                         **│**

&#x20;                         **▼**

&#x20;              **JwtAuthenticationFilter**

&#x20;                         **│**

&#x20;                         **▼**

&#x20;            **SecurityContextHolder**

&#x20;                         **│**

&#x20;                         **▼**

&#x20;                **Spring Security**

&#x20;                         **│**

&#x20;                         **▼**

&#x20;                    **Controller**



**The SecurityFilterChain acts like the main security pipeline. Every request enters it before reaching your controllers.**



**Login Flow (First Time)**

**Client**

&#x20; **│**

&#x20; **▼**

**POST /login**

&#x20; **│**

&#x20; **▼**

**AuthController**

&#x20; **│**

&#x20; **▼**

**AuthService**

&#x20; **│**

&#x20; **▼**

**AuthenticationManager**

&#x20; **│**

&#x20; **▼**

**DaoAuthenticationProvider**

&#x20; **│**

&#x20; **▼**

**CustomUserDetailsService**

&#x20; **│**

&#x20; **▼**

**Database**

&#x20; **│**

&#x20; **▼**

**PasswordEncoder.matches()**

&#x20; **│**

&#x20; **▼**

**Credentials Valid**

&#x20; **│**

&#x20; **▼**

**JwtService.generateToken()**

&#x20; **│**

&#x20; **▼**

**JWT Returned**



**Notice that JWT is only generated after successful authentication.**



**Request Flow (After Login)**

**Client**

&#x20; **│**

&#x20; **│ Authorization: Bearer JWT**

&#x20; **▼**

**JwtAuthenticationFilter**

&#x20; **│**

&#x20; **▼**

**Extract Token**

&#x20; **│**

&#x20; **▼**

**JwtService.validateToken()**

&#x20; **│**

&#x20; **▼**

**Extract Username**

&#x20; **│**

&#x20; **▼**

**Load User Details**

&#x20; **│**

&#x20; **▼**

**SecurityContextHolder**

&#x20; **│**

&#x20; **▼**

**Controller**

**Your Auth Service Structure**

**auth-service**

**│**

**├── config**

**│      ├── SecurityConfig**

**│      └── PasswordConfig**

**│**

**├── controller**

**│      └── AuthController**

**│**

**├── service**

**│      ├── AuthService**

**│      ├── AuthServiceImpl**

**│      └── CustomUserDetailsService**

**│**

**├── security**

**│      ├── JwtService**

**│      ├── JwtAuthenticationFilter**

**│      └── CustomUserDetailsSecurity**

**│**

**├── repository**

**│      └── UserRepository**

**│**

**├── entity**

**│      ├── User**

**│      └── Role**

**│**

**└── dto**

**Responsibilities of Each Class**

**Class	Responsibility**

**SecurityConfig	Configures Spring Security**

**PasswordConfig	Creates the PasswordEncoder bean**

**CustomUserDetailsService	Loads user from the database**

**CustomUserDetailsSecurity	Converts your User entity into Spring Security's UserDetails**

**AuthenticationManager	Starts the authentication process**

**DaoAuthenticationProvider	Verifies the password using PasswordEncoder**

**JwtService	Generates, validates, and parses JWTs**

**JwtAuthenticationFilter	Reads the JWT from incoming requests and authenticates the user**

**SecurityContextHolder	Stores the authenticated user for the current request**

**Complete Picture**

&#x20;              **LOGIN**



**Client**

&#x20;  **│**

&#x20;  **▼**

**AuthController**

&#x20;  **│**

&#x20;  **▼**

**AuthenticationManager**

&#x20;  **│**

&#x20;  **▼**

**DaoAuthenticationProvider**

&#x20;  **│**

&#x20;  **▼**

**CustomUserDetailsService**

&#x20;  **│**

&#x20;  **▼**

**Database**

&#x20;  **│**

&#x20;  **▼**

**JwtService**

&#x20;  **│**

&#x20;  **▼**

**JWT**

&#x20;  **│**

&#x20;  **▼**

**Client Stores JWT**

**════════════════════════════════════════════════════════════**



&#x20;       **EVERY FUTURE REQUEST**



**Client**

&#x20;  **│**

**Authorization: Bearer JWT**

&#x20;  **│**

&#x20;  **▼**

**SecurityFilterChain**

&#x20;  **│**

&#x20;  **▼**

**JwtAuthenticationFilter**

&#x20;  **│**

&#x20;  **▼**

**JwtService**

&#x20;  **│**

&#x20;  **▼**

**SecurityContextHolder**

&#x20;  **│**

&#x20;  **▼**

**Controller**

&#x20;  **│**

&#x20;  **▼**

**Response**

**-------------------------------------------------------------------------------**

&#x20;               **JWT**



**┌──────────────┐**

**│ Header       │**

**│ alg = HS256  │**

**└──────────────┘**

&#x20;       **│**

&#x20;       **▼**

**┌──────────────┐**

**│ Payload      │**

**│ username     │**

**│ role         │**

**│ expiry       │**

**└──────────────┘**

&#x20;       **│**

&#x20;       **▼**

**┌──────────────┐**

**│ Signature    │**

**│ Secret Key   │**

**└──────────────┘**

