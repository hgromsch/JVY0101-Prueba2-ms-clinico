# ms-clinico

Fichas clínicas de los pacientes. Cubre **RF-09**. **Zona crítica de seguridad**:
el contenido de las notas se **cifra a nivel de aplicación** (AES-GCM, clave custodiada
por KMS en AWS) antes de persistir, y la lectura exige el header `X-Usuario-Rol` con valor
`PSICOLOGO` o `ADMIN` (en AWS proviene del JWT de Cognito). Cada acceso queda en el log de auditoría.

**Puerto:** 8086 · **Base de datos:** `clinico_db` · **Swagger:** http://localhost:8086/swagger-ui.html

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/fichas` | Abre la ficha de un paciente (201; 409 si ya existe) |
| GET | `/api/fichas` | Lista fichas |
| GET | `/api/fichas/{id}` | Obtiene una ficha (404 si no existe) |
| GET | `/api/fichas/paciente/{pacienteId}` | Ficha de un paciente |
| PUT | `/api/fichas/{id}` | Actualiza antecedentes |
| DELETE | `/api/fichas/{id}` | Elimina la ficha (204) |
| POST | `/api/fichas/{id}/notas` | Registra nota de sesión, se persiste cifrada (201) |
| GET | `/api/fichas/{id}/notas` | Notas descifradas; exige `X-Usuario-Rol: PSICOLOGO` (403 si no) |

## Cómo correr localmente

```bash
docker compose -f ../docker-compose.yml up -d   # Postgres + Redis (una sola vez)
mvn spring-boot:run
```

La clave de cifrado se inyecta con la variable de entorno `CLINICO_CRYPTO_KEY`
(Base64 de 32 bytes); sin ella se usa una clave de desarrollo solo para demostración.

## Arquitectura

Capas `api` → `service` → `repository` → `domain`, más `CifradoService` (AES-GCM con IV
aleatorio por nota) y log de auditoría propio.
Relación JPA destacada: `FichaClinica` **OneToMany** `NotaClinica` (y **ManyToOne** inversa).

## Despliegue

Pensado para **Amazon ECS Fargate** con Aurora Serverless y clave KMS.
Imagen: `docker build -t ms-clinico .`
