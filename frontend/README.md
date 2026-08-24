# Wardrobe Frontend

SPA Vue 3 + TypeScript para gestionar el inventario personal de ropa definido
por el backend de este mismo repositorio. **No inventa endpoints**: consume
exactamente la API REST/JSON existente.

## Stack

- Vue 3 (`<script setup lang="ts">`)
- TypeScript estricto
- Vite
- Vue Router 4 (lazy loading + rutas protegidas)
- Pinia (solo para autenticación y toasts)
- CSS propio (sin librería de UI)

## Requisitos

- Node.js 20+ (probado con Node 22)
- Backend Spring Boot corriendo y accesible (por defecto en `http://localhost:8080`)

## Arranque rápido (desarrollo)

### 1. Backend

Sigue las instrucciones del `README.md` raíz para arrancar PostgreSQL y el
backend (perfil `local`). El backend expone:

- API en `http://localhost:8080`
- Swagger UI en `http://localhost:8080/swagger-ui.html`

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

La SPA queda disponible en `http://localhost:5173`.

Vite proxy reenvía `/api`, `/v3/api-docs`, `/swagger-ui` y `/actuator` al
backend, de modo que las cookies de sesión funcionan sin tocar CORS en
desarrollo.

### 3. Credenciales de prueba

Si el seed del backend está activo (`SEED_ENABLED=true`), puedes entrar con:

```
usuario: admin
contraseña: admin123
```

Desde la propia pantalla de login (solo en modo dev) también puedes crear
usuarios adicionales siempre que el backend tenga
`APP_AUTH_ALLOW_USER_CREATION=true`.

## Estructura

```
frontend/
├── index.html
├── vite.config.ts          # proxy /api → backend
├── tsconfig*.json
├── package.json
├── .env.example
└── src/
    ├── main.ts
    ├── App.vue
    ├── api/                # cliente HTTP tipado por recurso
    │   ├── client.ts       # wrapper fetch con credentials: 'include'
    │   ├── auth.ts
    │   ├── garments.ts
    │   └── images.ts
    ├── stores/             # Pinia (auth + toast)
    ├── types/
    │   └── api.ts          # mirror de los DTOs del backend
    ├── router/
    ├── composables/        # useGarmentList, tipos de filtros
    ├── utils/              # format (precios/fechas), errores legibles
    ├── components/         # GarmentCard, GarmentForm, etc.
    └── views/              # LoginView, WardrobeView, ...
```

## Scripts

```bash
npm run dev         # servidor Vite con HMR
npm run build       # vue-tsc + producción en dist/
npm run preview     # sirve el build de producción localmente
npm run type-check  # vue-tsc sin emitir
```

## Configuración

Variables de entorno (todas opcionales, ver `.env.example`):

| Variable                  | Default                  | Uso                                                     |
| ------------------------- | ------------------------ | ------------------------------------------------------- |
| `VITE_API_BASE_URL`       | (vacía)                  | Si se define, el frontend llama a este origen absoluto. |
| `VITE_API_PROXY_TARGET`   | `http://localhost:8080`  | Destino del proxy de Vite en desarrollo.                |

Si `VITE_API_BASE_URL` está vacío (lo normal), el frontend hace todas las
peticiones a rutas relativas (`/api/...`) y se apoya en el proxy de Vite en
desarrollo o en el mismo origen en producción.

## Modelo de pantallas

- `/login` — inicio de sesión (y, en dev, alta rápida de usuarios).
- `/wardrobe` — prendas en estado `WARDROBE` (con filtros, búsqueda, paginación).
- `/for-sale` — prendas en estado `FOR_SALE`.
- `/sold` — prendas en estado `SOLD`.
- `/outfits` — generador aleatorio de conjuntos y lista de conjuntos guardados.
- `/outfits/:id` — vista detallada de un conjunto guardado.
- `/locations` — CRUD completo de ubicaciones.
- `/garments/new` — formulario de creación.
- `/garments/:id` — detalle (galería, descripción, acciones de estado).
- `/garments/:id/edit` — edición.

La pantalla de detalle incluye:

- galería con miniatura principal marcada como “Principal”;
- descripción generada (solo visible para `FOR_SALE`) con botón **Copiar**;
- subida de imágenes por click o drag & drop;
- borrado individual de imagen;
- reordenación por drag & drop sobre las miniaturas;
- cambio de estado con confirmaciones explícitas (poner en venta, vender,
  volver al armario, eliminar);
- campo de ubicación con selector que admite crear una nueva al vuelo.

La pantalla de conjuntos incluye:

- selector de temporada (obligatorio);
- dos toggles: incluir capa exterior y/o accesorios;
- botón **Generar conjunto** que arma el look respetando las reglas del
  backend;
- botón **Guardar** sobre el resultado, con nombre libre, para conservarlo;
- lista de conjuntos guardados con acceso a detalle y borrado.

## Reglas de negocio respetadas

- El owner se obtiene siempre del backend; el frontend nunca envía un id de
  usuario.
- `status` se modifica únicamente a través de los endpoints de transición
  (`/put-for-sale`, `/move-to-wardrobe`, `/mark-as-sold`).
- El listado pasa el filtro de talla como `garmentSize` (el `size` está
  reservado para el parámetro `size` de paginación de Spring).
- La descripción la genera el backend; el frontend la muestra tal cual y la
  copia al portapapeles.
- Categorías, subcategorías, colores, temporadas y marcas usan el formato
  del backend (`TOP`, `JEANS`, `BLUE`, `SUMMER`, etc.) pero se renderizan
  con etiquetas legibles y, en el caso del color, con muestras visuales.
- Cuando cambia la categoría en el formulario o el filtro, la subcategoría
  se reinicia si ya no pertenece a la nueva categoría.
- Si la API rechaza una subcategoría por no pertenecer a la categoría
  (`INVALID_SUBCATEGORY`), el error se muestra junto al campo.
- Las acciones destructivas (eliminar prenda, eliminar imagen) pasan por un
  `ConfirmDialog`.
- Los errores de validación se muestran junto al campo correspondiente.

## Estados de UI

Cada vista lista contempla:

- **Loading**: spinner mientras se carga.
- **Empty**: estado vacío con icono, mensaje y, si aplica, acción principal
  (por ejemplo, “Añadir la primera”).
- **Error**: mensaje legible para el usuario + botón **Reintentar**.
- **Success**: toast verde usando el `useToastStore`.
- **Operaciones en curso**: botones deshabilitados con etiqueta “…”.
- **401/403**: el `authStore`bootstrap detecta la falta de sesión y la guarda
  redirige al login. No se muestra una pantalla técnica.

## Responsive

- Móvil (<600 px): grid de 2 columnas, navegación colapsada en menú.
- Tablet (600–960 px): grid de 3 columnas.
- Escritorio (≥960 px): 4–5 columnas.
- Los filtros se muestran en un panel desplegable bajo la barra de búsqueda.

## Accesibilidad

- Labels asociados a cada input.
- Botones reales (`<button>`), no `<div>` clicables.
- Foco visible (`:focus-visible`).
- Diálogos modales con `<dialog>` nativo (cerrado por `Escape`, click en
  backdrop, o botones de cancelar/confirmar).
- Estados no comunicados solo por color: las tarjetas usan texto (`Principal`)
  además del borde.

## Build de producción

En el despliegue recomendado se usa el `Dockerfile` incluido y todo el
stack se levanta con `docker compose` (Postgres + backend + nginx). El
SPA se sirve desde nginx en el mismo origen que el backend, así que las
cookies de sesión funcionan sin tocar CORS y no hace falta
`VITE_API_BASE_URL`. Solo se expone el puerto `8080`.

Si quieres construir el bundle manualmente:

```bash
npm run build
```

Genera `frontend/dist/` con todo el bundle estático.

Si necesitas separar orígenes (por ejemplo CDN + API en otro dominio),
define `VITE_API_BASE_URL=https://api.tu-dominio` antes de construir y
configura los orígenes permitidos en el backend
(`APP_CORS_ALLOWED_ORIGINS`).

## Decisiones arquitectónicas

- **Sin librería de UI**: el diseño es deliberadamente minimalista y se
  construye con CSS propio (variables, grid y media queries). Reduce el peso
  del bundle y simplifica el mantenimiento.
- **Cliente HTTP casero basado en `fetch`**: evita dependencias adicionales y
  permite tipar las respuestas exactamente como los DTOs del backend.
- **Pinia mínima**: solo autenticación y toasts; los datos de listado viven
  en el composable `useGarmentList` para evitar Pinia inflado.
- **Composables** (`useGarmentList`, `filters`): encapsulan la lógica
  común a las tres vistas de listado sin obligar a tener un componente base.
- **Tipos manuales en `types/api.ts`**: el backend no expone un generador
  automático, así que los DTOs se reflejan a mano. Cualquier cambio en el
  contrato debe replicarse aquí.
- **Vite proxy en dev**: cookies same-origin sin tocar CORS en
  `localhost`. En producción el frontend y el backend comparten origen.
