# Wardrobe

Inventario de ropa multiusuario, monorepo:

- `src/` — Backend Spring Boot (Java 21, PostgreSQL, Flyway, Spring Security).
- `frontend/` — SPA Vue 3 + TypeScript que consume la API REST/JSON del
  backend.

Cada prenda es una única entidad cuyo `status` (`WARDROBE` / `FOR_SALE` /
`SOLD`) determina dónde se encuentra; no existe una tabla separada de
"armario" y "en venta". Las imágenes se guardan en el sistema de archivos,
nunca dentro de PostgreSQL.

Diseñado para funcionar en un NAS doméstico pequeño: bajo consumo de
memoria, operaciones sencillas, una sola JVM, frontend como assets estáticos
servidos desde el mismo origen.

## Stack

- Java 21
- Spring Boot 3.3.x (Web, Data JPA, Security, Validation, Actuator)
- Hibernate (proveedor JPA por defecto)
- PostgreSQL 16 + Flyway
- JUnit 5 + Mockito para tests unitarios
- Testcontainers (PostgreSQL) para tests de integración
- springdoc-openapi para `/swagger-ui.html`

## Requisitos

- JDK 21+
- Maven 3.9+
- PostgreSQL 14+ corriendo en local (o vía Docker)
- Para los tests de integración: un demonio Docker funcional

## Arranque rápido (desarrollo local)

### 1. Arrancar PostgreSQL

La forma más sencilla es Docker:

```bash
docker run -d --name wardrobe-pg \
    -e POSTGRES_DB=wardrobe \
    -e POSTGRES_USER=wardrobe \
    -e POSTGRES_PASSWORD=wardrobe \
    -p 5432:5432 \
    postgres:16-alpine
```

### 2. Configurar las variables de entorno

Copia `.env.example` a `.env` y ajústalo si es necesario. Los valores por
defecto funcionan con el contenedor Docker de arriba.

```
DB_URL=jdbc:postgresql://localhost:5432/wardrobe
DB_USERNAME=wardrobe
DB_PASSWORD=wardrobe
SPRING_PROFILES_ACTIVE=local
SEED_ENABLED=true
SEED_ADMIN_USERNAME=admin
SEED_ADMIN_PASSWORD=admin123
APP_AUTH_ALLOW_USER_CREATION=true
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
APP_STORAGE_IMAGES_PATH=./data/images
```

### 3. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

En el primer arranque, el seeder crea el usuario `admin` (solo cuando
`SEED_ENABLED=true` y el perfil `local` está activo).

La API quedará disponible en `http://localhost:8080`.

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- Health: <http://localhost:8080/actuator/health>

## Configuración

Toda la configuración de producción proviene de variables de entorno. No hay
ningún secreto en el código fuente.

| Variable                            | Por defecto                                                                          | Descripción                                                            |
| ----------------------------------- | ------------------------------------------------------------------------------------ | ---------------------------------------------------------------------- |
| `SPRING_PROFILES_ACTIVE`            | `local`                                                                              | Perfil de Spring activo (`local`, `test`, `prod`)                      |
| `SERVER_PORT`                       | `8080`                                                                               | Puerto HTTP                                                            |
| `DB_URL`                            | `jdbc:postgresql://localhost:5432/wardrobe`                                          | URL JDBC                                                               |
| `DB_USERNAME`                       | `wardrobe`                                                                           | Usuario de la base de datos                                            |
| `DB_PASSWORD`                       | `wardrobe`                                                                           | Contraseña de la base de datos                                         |
| `SESSION_COOKIE_SECURE`             | `false`                                                                              | Poner a `true` detrás de HTTPS                                         |
| `SESSION_TIMEOUT`                   | `24h`                                                                                | Duración de la sesión                                                  |
| `APP_CORS_ALLOWED_ORIGINS`          | `http://localhost:5173,http://localhost:3000`                                        | Lista separada por comas de orígenes permitidos                        |
| `APP_STORAGE_IMAGES_PATH`           | `./data/images`                                                                      | Ruta del sistema de archivos donde se guardan las imágenes subidas    |
| `APP_UPLOAD_MAX_SIZE`               | `10MB`                                                                               | Tamaño máximo de un archivo subido                                     |
| `APP_UPLOAD_MAX_REQUEST_SIZE`       | `11MB`                                                                               | Tamaño máximo de la petición multipart                                 |
| `APP_AUTH_ALLOW_USER_CREATION`      | `false` (true en `local`)                                                            | Si es `true`, se habilita `POST /api/auth/users`                       |
| `SEED_ENABLED`                      | `false` (true en `local`)                                                            | Activa el seeder de usuario inicial                                    |
| `SEED_ADMIN_USERNAME`               | `admin`                                                                              | Usuario del seed                                                       |
| `SEED_ADMIN_PASSWORD`               | `admin123`                                                                           | Contraseña del seed                                                    |
| `SEED_ADMIN_DISPLAY_NAME`           | `Admin`                                                                              | Nombre visible del seed                                                |

Se incluye `./.env.example` como plantilla.

## Cómo funciona la autenticación

- No se asume un frontend sin estado: la API usa cookies de sesión HTTP.
- Las contraseñas se almacenan con BCrypt.
- `POST /api/auth/login` autentica y crea una sesión; `JSESSIONID` es la
  cookie que el frontend debe devolver.
- `POST /api/auth/logout` invalida la sesión (lo gestiona Spring Security).
- `GET /api/auth/me` devuelve el usuario autenticado actualmente.
- `POST /api/auth/users` crea un nuevo usuario. Está protegido por
  `APP_AUTH_ALLOW_USER_CREATION`. En producción se deja en `false`
  (por defecto); en desarrollo se pone a `true` para que el admin del seed
  pueda dar de alta más usuarios, o para usar el usuario seed como invitación.

El endpoint `me` nunca devuelve `passwordHash`.

CSRF está deshabilitado intencionadamente porque la API solo acepta JSON y
está pensada para ser consumida desde una SPA del mismo origen. Esto está
documentado como una decisión deliberada; si alguna vez se expone la API a
otro origen o se añaden flujos con formularios del navegador, hay que
volver a activar la protección CSRF.

## Frontend

El directorio `frontend/` contiene una SPA Vue 3 + TypeScript que consume
esta API directamente (sin mocks ni endpoints inventados). Consulta
[`frontend/README.md`](frontend/README.md) para ver la configuración, los
scripts y las notas de arquitectura.

## Endpoints

### Auth

| Método | Path                  | Auth | Descripción                                          |
| ------ | --------------------- | ---- | ---------------------------------------------------- |
| POST   | `/api/auth/login`     | no   | Body: `{username, password}` → crea la sesión        |
| POST   | `/api/auth/logout`    | no   | Invalida la sesión actual                            |
| GET    | `/api/auth/me`        | sí   | Devuelve el usuario autenticado actualmente          |
| POST   | `/api/auth/users`     | no*  | Crea un nuevo usuario (* solo cuando el flag está on)|

### Prendas

| Método | Path                                | Descripción                                |
| ------ | ----------------------------------- | ------------------------------------------ |
| GET    | `/api/garments`                     | Listado con filtros y paginación           |
| POST   | `/api/garments`                     | Crear una prenda                           |
| GET    | `/api/garments/{id}`                | Obtener una prenda (con sus imágenes)      |
| PUT    | `/api/garments/{id}`                | Actualizar una prenda                      |
| DELETE | `/api/garments/{id}`                | Eliminar una prenda (y sus archivos)       |
| POST   | `/api/garments/{id}/put-for-sale`   | Transición a `FOR_SALE`                    |
| POST   | `/api/garments/{id}/move-to-wardrobe` | Transición a `WARDROBE`                  |
| POST   | `/api/garments/{id}/mark-as-sold`   | Transición a `SOLD`                        |

### Ubicaciones

| Método | Path                  | Descripción                                                |
| ------ | --------------------- | ---------------------------------------------------------- |
| GET    | `/api/locations`      | Lista las ubicaciones del usuario con `garmentCount`       |
| POST   | `/api/locations`      | Crea una ubicación (nombre único case-insensitive)         |
| PUT    | `/api/locations/{id}` | Renombra una ubicación                                    |
| DELETE | `/api/locations/{id}` | Elimina una ubicación; 409 `LOCATION_IN_USE` si tiene prendas |

Las prendas aceptan un `locationId` (existente) o un `locationName` (crea
inline; idempotente sobre el nombre). Si pasas ambos → 400 `LOCATION_AMBIGUOUS`.

### Conjuntos

| Método | Path                       | Descripción                                                            |
| ------ | -------------------------- | ---------------------------------------------------------------------- |
| POST   | `/api/outfits/generate`    | Genera un conjunto aleatorio para `{season, includeOuterwear, includeAccessories}` |
| GET    | `/api/outfits`             | Lista los conjuntos guardados del usuario                              |
| POST   | `/api/outfits`             | Guarda un conjunto a partir de un nombre y `garmentIds`                |
| GET    | `/api/outfits/{id}`        | Recupera un conjunto (incluye snapshot de cada prenda)                 |
| DELETE | `/api/outfits/{id}`        | Elimina un conjunto                                                    |

Reglas del generador:
- Toma prendas no vendidas del usuario filtradas por temporada.
- Si hay vestidos, el 50 % de las veces se elige un vestido en lugar de la
  pareja top + bottom.
- Los zapatos son obligatorios; el resto es optativo según los toggles.
- Si falta alguna categoría obligatoria → 409 `INSUFFICIENT_GARMENTS`.

### Imágenes

| Método | Path                                            | Descripción                          |
| ------ | ----------------------------------------------- | ------------------------------------ |
| POST   | `/api/garments/{garmentId}/images`              | Subir una imagen (multipart)         |
| GET    | `/api/garments/{garmentId}/images/{imageId}`    | Descargar una imagen                 |
| DELETE | `/api/garments/{garmentId}/images/{imageId}`    | Eliminar una imagen                  |
| PUT    | `/api/garments/{garmentId}/images/order`        | Reordenar imágenes                   |

### Filtros del listado

```
GET /api/garments
  ?status=FOR_SALE            # WARDROBE | FOR_SALE | SOLD
  &category=BOTTOM            # uno de Category (TOP, SWEATER, OUTERWEAR, BOTTOM, SKIRT, DRESS, SHOES, ACCESSORIES, OTHER)
  &subcategory=JEANS          # uno de Subcategory; debe pertenecer a la categoría elegida
  &color=BLUE                 # uno de los 10 valores fijos del enum Color
  &season=SUMMER              # SUMMER | WINTER
  &locationId=4               # filtra por ubicación
  &garmentSize=M              # texto libre, igualdad sin distinguir mayúsculas
  &brand=Nike                 # igualdad sin distinguir mayúsculas
  &condition=GOOD             # NEW | LIKE_NEW | GOOD | USED
  &search=camiseta            # coincide con name / brand / description
  &page=0
  &size=20
  &sort=createdAt,desc
```

El filtro de talla se expone como `garmentSize` para no chocar con el
parámetro `size` de `Pageable` de Spring.

### Formato de error

Todas las respuestas de error comparten la misma forma:

```json
{
  "timestamp": "2026-01-01T12:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/api/garments",
  "fieldErrors": {
    "name": "Name is required"
  }
}
```

Nunca se devuelven stack traces al cliente.

## Reglas de dominio

- Una prenda debe tener `name`, `size`, `category`, `condition`, `status`,
  `season`.
- `subcategory` es opcional, pero si se indica debe pertenecer a la
  categoría padre (p. ej. `JEANS` requiere `category=BOTTOM`; la API
  rechaza el descuadre con `INVALID_SUBCATEGORY`).
- `color` es opcional y está limitado a un enum de 10 valores fijos.
- `locationId`/`locationName` son opcionales; el nombre es único
  case-insensitive por usuario.
- Una prenda debe tener siempre al menos una imagen.
- `owner` es siempre el usuario autenticado. El frontend no puede elegirlo.
- `salePrice` es obligatorio cuando el estado es `FOR_SALE`; se conserva
  cuando es `SOLD`.
- `purchasePrice` es opcional.
- Las transiciones de estado son endpoints explícitos; el frontend nunca
  asigna `status` directamente en `PUT /api/garments` para esos flujos.
- `SOLD → FOR_SALE` no se permite directamente; hay que pasar antes por
  `move-to-wardrobe`.
- `SOLD → WARDROBE` se permite y limpia `soldAt`.
- `salePrice`, la descripción y las imágenes se conservan en las
  transiciones.

### Categorías, subcategorías, colores y temporadas

- **Categoría** (top-level): `TOP`, `SWEATER`, `OUTERWEAR`, `BOTTOM`,
  `SKIRT`, `DRESS`, `SHOES`, `ACCESSORIES`, `OTHER`.
- **Subcategoría** (enum cerrado por categoría; p. ej.
  `BOTTOM → {JEANS, CHINOS, DRESS_PANTS, JOGGERS, LINEN_PANTS, SHORTS,
  LEGGINGS}`).
- **Color** (10 valores fijos): `WHITE`, `BLACK`, `GRAY`, `BEIGE`, `RED`,
  `ORANGE`, `YELLOW`, `GREEN`, `BLUE`, `MULTICOLOR`.
- **Temporada** (obligatoria): `SUMMER`, `WINTER`.

## Generación de descripción

`DescriptionGenerator` es una interfaz con una única implementación basada
en plantilla (`TemplateDescriptionGenerator`) que construye una frase a
partir de los campos de la prenda. Ejemplo:

```
Camiseta Nike azul, talla M. En buen estado.
```

Los campos en blanco se omiten. Más adelante se puede añadir
`AiDescriptionGenerator` sin tocar el resto del código.

## Almacenamiento de imágenes

- Se guardan en el sistema de archivos bajo `app.storage.images-path`
  (por defecto `./data/images`).
- Los archivos se escriben con un nombre basado en UUID; el nombre
  proporcionado por el usuario se sanea y se guarda como metadato.
- Se bloquea el path traversal (se rechaza `..` al resolver la ruta).
- Si se elimina un registro de la base de datos pero el archivo ya no
  existe en disco, la operación se completa correctamente (se registra en
  WARN).

## Tests

```bash
./mvnw test           # tests unitarios + de integración (Testcontainers si Docker está disponible)
./mvnw package        # construye el jar ejecutable
```

### Tests unitarios

- `TemplateDescriptionGeneratorTest` — composición de la frase y omisión de
  campos.
- `GarmentServiceTest` — transiciones de estado, reglas de validación,
  propiedad y limpieza de imágenes (usa Mockito).

### Tests de integración (Testcontainers)

- `AuthIntegrationTest` — registro, login, `/me`, la contraseña nunca se
  devuelve.
- `GarmentIntegrationTest` — ciclo de vida completo
  `WARDROBE → FOR_SALE → SOLD → WARDROBE`, aislamiento entre usuarios,
  filtros, paginación, búsqueda.
- `ImageIntegrationTest` — subida, descarga, ordenación, eliminación de la
  última imagen, acceso entre usuarios.

Los tests de integración se saltan automáticamente cuando no hay un
demonio Docker disponible
(`@Testcontainers(disabledWithoutDocker = true)`).

## Docker

Se incluye un Dockerfile multi-stage. Usa Eclipse Temurin 21 y produce una
imagen de runtime pequeña basada en JRE. La imagen no corre como root y
expone el directorio de imágenes subidas como volumen.

```bash
docker build -t wardrobe-backend .
docker run -d --name wardrobe \
    -p 8080:8080 \
    -e DB_URL=jdbc:postgresql://host.docker.internal:5432/wardrobe \
    -e DB_USERNAME=wardrobe \
    -e DB_PASSWORD=wardrobe \
    -e APP_CORS_ALLOWED_ORIGINS=http://localhost:5173 \
    -v wardrobe-images:/app/data/images \
    wardrobe-backend
```

Para un stack totalmente contenedorizado puedes emparejar el backend con la
imagen oficial de PostgreSQL y persistir tanto la base de datos como las
imágenes subidas mediante volúmenes de Docker.

## Decisiones arquitectónicas

Las siguientes decisiones se tomaron para mantener el proyecto pequeño,
convencional y fácil de mantener:

- **Monolito modular por paquetes**, no arquitectura hexagonal/clean: es
  una aplicación CRUD y el layering DDD completo añadiría ceremonia sin
  beneficio.
- **Cookies de sesión**, no JWT: el pliego dice que la SPA se servirá
  desde el mismo dominio en despliegue, así que una cookie de sesión es
  más sencilla y evita el cableado de renovación de tokens.
- **Sistema de archivos para las imágenes**, no almacenamiento de objetos:
  encaja con el objetivo de NAS pequeño y evita dependencias externas.
- **Flyway** para migraciones de esquema; `validate` para `ddl-auto` de
  JPA. Hibernate se usa solo en runtime, nunca para gestionar el esquema.
- **JPA Specifications** para el endpoint de listado: ofrece filtros
  componibles sin escribir JPQL a medida para cada combinación.
- **Interfaz `DescriptionGenerator`** con implementación por plantilla:
  permite que un futuro `AiDescriptionGenerator` encaje sin tocar el
  servicio.
- **Único endpoint `POST /api/auth/users` protegido por config**: cumple
  la regla de "sin registro público" y a la vez permite al admin/seed
  provisionar usuarios sin roles.
- **`forkCount=1` + `reuseForks=true` para surefire**: los tests de
  integración comparten un contexto de Spring por JVM para reutilizar el
  contenedor de Testcontainers.
- **CSRF deshabilitado**: la API se consume desde una SPA del mismo origen
  vía JSON. Documentado como decisión deliberada; debe revisarse si la
  API llega a servir a clientes cross-origin.

## Checklist funcional (criterio 34)

- [x] Crear usuario
- [x] Login
- [x] Crear prenda
- [x] Subir foto
- [x] Listar mis prendas
- [x] Filtrar mis prendas
- [x] Mover prenda de `WARDROBE` a `FOR_SALE`
- [x] Generar descripción automáticamente
- [x] Marcar prenda como `SOLD`
- [x] Devolver la prenda al `WARDROBE`
- [x] Eliminar prenda
- [x] Otros usuarios no pueden acceder a mis prendas (forzado en la capa
      de repositorio vía `findByIdAndOwnerId` y verificado por los tests
      de integración)

## Licencia

Código privado, sin licencia declarada.
