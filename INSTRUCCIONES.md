# Wardrobe — Manual de uso

> Tu armario, en un solo lugar. Inventario, ventas y combinaciones al azar,
> con la privacidad de tenerlo todo en tu propia red.

---

## ¿Qué es Wardrobe?

Wardrobe es una aplicación web pensada para tener **controlado todo tu
armario**: qué tienes, dónde lo guardas, qué te falta, qué quieres
vender y qué looks puedes armar con lo que ya tienes.

Funciona en casa (un NAS, un Mini PC, un Raspberry con Docker…), no en la
nube de nadie. **Tus datos son tuyos**: ni fotos, ni descripciones, ni
precios salen de tu red.

Está pensada para una sola persona o una familia: cada usuario tiene su
propio inventario, sus propias ubicaciones y sus propios conjuntos
guardados.

---

## ¿Para quién es?

- Quieres vender prendas de segunda mano sin montar una tienda online.
- Te cuesta decidir qué ponerte por las mañanas.
- Has perdido piezas tres veces y no quieres que pase una cuarta.
- Te gusta tener el armario ordenado por ubicación real (caja, percha,
  bolsa de venta…).
- No quieres compartir tus fotos de ropa con servicios externos.

---

## Lo que puedes hacer

### 1. Inventario por prenda

Cada prenda es una ficha con:

- **Nombre** (lo que tú quieras: "Camiseta azul del verano 2024").
- **Talla**, **marca**, **estado** (Nuevo / Como nuevo / Buen estado /
  Usado).
- **Categoría** (Parte de arriba, Jersey, Abrigo, Pantalón, Falda,
  Vestido, Zapatos, Accesorios…) y **subcategoría** específica
  (Camiseta / Camisa / Polo dentro de Parte de arriba; Vaqueros /
  Chinos / Pantalón de chándal dentro de Pantalón, etc.).
- **Color**: 10 colores fijos con muestras visuales
  (Blanco, Negro, Gris, Beige, Rojo, Naranja, Amarillo, Verde, Azul,
  Multicolor). Nada de escribir a mano.
- **Temporada**: Verano o Invierno. Sirve para filtrar y para que el
  generador de conjuntos acierte.
- **Ubicación**: la eliges entre las que ya tienes creadas
  ("Armario grande", "Bolsa de venta 1", "Cajón ropa interior"…) o
  escribes una nueva al vuelo.
- **Fotos**: tantas como quieras. Arrastras, sueltas, reordenas, marcas
  una como principal. La principal es la miniatura en los listados.
- **Precio de venta** y **precio de compra** (este último opcional,
  para tus cuentas internas).
- **Notas personales** (talla exacta, recordatorios, dónde la viste…).

#### Situación de cada prenda

Cada prenda vive en uno de tres estados:

- **Armario**: la tienes y la usas.
- **En venta**: la has puesto a la venta. La aplicación genera
  automáticamente una descripción en una frase con todos los datos
  (marca, color, talla, estado) y la deja lista para copiarla y
  pegarla en Wallapop, Vinted, Milanuncios…
- **Vendida**: ya la has vendido. Conserva precio y descripción para
  que cuadren tus cuentas.

Las transiciones son botones explícitos: nunca editas el estado a mano.

---

### 2. Ubicaciones (por usuario)

Creas las ubicaciones que necesites para tu casa:

- "Armario grande"
- "Bolsa de venta 1"
- "Caja ropa interior"
- "Percha izquierda"
- …

Puedes renombrarlas en cualquier momento. Si intentas borrar una que
todavía tiene prendas dentro, la app te avisa y no te deja: primero
reasignas o eliminas las prendas, luego borras la ubicación. Sin
pérdidas accidentales.

---

### 3. Búsqueda y filtros

En cada listado (Armario / En venta / Vendidas) tienes:

- **Buscador** por nombre, marca o descripción.
- Filtros combinables por:
  - **Categoría** y **subcategoría** (al elegir categoría, la
    subcategoría se reduce a las válidas).
  - **Color**.
  - **Temporada**.
  - **Ubicación**.
  - **Talla**, **marca**, **estado**.
- **Paginación** y un contador de "X resultados de Y".

Los filtros son aditivos: cuantos más pongas, menos resultados
obtendrás.

---

### 4. Generador de conjuntos aleatorios

¿No sabes qué ponerte? La sección **Conjuntos** arma un look por ti:

1. Eliges la **temporada** (Verano o Invierno).
2. Activas o desactivas dos interruptores:
   - Incluir capa exterior (chaqueta, abrigo, blazer).
   - Incluir accesorios (cinturón, bolso, bufanda, sombrero).
3. Pulsas **Generar conjunto**.

La app elige:

- **Un vestido** (si tienes vestidos y no te obligas a llevar
  pantalones), **o** un conjunto de **parte de arriba + pantalón**.
- **Unos zapatos** (obligatorio, no sales sin zapatos).
- Opcionalmente, una **capa exterior** y entre 1 y 2 **accesorios**.

Si te gusta el resultado, le pones un nombre y le das a **Guardar**:
queda archivado para siempre con la fecha de creación. Si no te gusta,
**Descartar** y vuelve a generar otro.

Los conjuntos guardados se abren en una vista detalle que enseña cada
prenda como una tarjeta. Si más adelante borras una de las prendas del
armario, el conjunto sigue mostrándose con un placeholder "Esta prenda
fue eliminada del armario" para que no pierdas la referencia.

---

### 5. Privacidad y cuentas

- Cada usuario tiene sus propias prendas, ubicaciones y conjuntos.
- Las contraseñas se almacenan cifradas (BCrypt); la base de datos no
  contiene nada legible.
- La sesión vive en una cookie HTTP-only del navegador. Al cerrar
  sesión se invalida inmediatamente.
- El servidor está pensado para correr en tu LAN. Si lo expones a
  internet, ponlo detrás de un reverse proxy con HTTPS (Traefik, Nginx,
  Caddy…).

---

## Roles y permisos

Wardrobe no usa roles: todos los usuarios son iguales y ven solo lo
suyo. Para crear cuentas nuevas tienes dos opciones:

- **Manualmente** desde la base de datos (un INSERT en `users` con la
  contraseña ya cifrada en BCrypt).
- **Desde la propia pantalla de login** en modo desarrollo, si activas
  la variable `APP_AUTH_ALLOW_USER_CREATION=true`. Esto está pensado
  para que el admin inicial pueda dar de alta al resto de la familia.
  En producción, déjalo en `false`.

---

## Puesta en marcha (rápido)

Si ya tienes Docker, el stack completo (Postgres + backend + frontend)
se levanta con un único comando y expone un único puerto:

```bash
cp .env.docker.example .env.docker   # solo la primera vez
docker compose --env-file .env.docker up -d
```

Tras unos segundos tendrás la app corriendo en **http://localhost:8080**:
ahí mismo está el frontend, la API, Swagger y el healthcheck. No hay
que abrir puertos adicionales ni configurar nada.

Tras el primer arranque se crea automáticamente un usuario `admin`
(usuario: `admin`, contraseña: `admin123`) si activas el seed. **Cámbiala
en cuanto entres.**

Los datos (base de datos y fotos subidas) viven en volúmenes Docker
nombrados, así que sobreviven a `docker compose down` y vuelven a estar
disponibles con un `up -d`.

Si prefieres desarrollar el frontend en local con hot-reload:

```bash
cd frontend
npm install
npm run dev
```

Queda en `http://localhost:5173`. El proxy de Vite redirige `/api`,
`/v3/api-docs` y `/actuator` al backend para evitar líos de CORS.

---

## Flujo típico de uso

1. **Día 1**: entras, creas 4-5 ubicaciones reales de tu casa
   ("Armario grande", "Cajón ropa interior"…).
2. **Vas añadiendo prendas** según las encuentras o las sacas a lavar.
   Foto de frente, foto del detalle si tiene algo especial, marca como
   principal la que más te guste.
3. **Cuando quieras vender algo**: edítala, pon precio, pulsa **Poner en
   venta**. La app genera la descripción. La copias al marketplace que
   uses.
4. **Cuando se vende**: pulsa **Marcar como vendida**. Se registra la
   fecha y pasa al histórico.
5. **Por la mañana**: abres Conjuntos → Generar y eliges lo que
   vas a ponerte hoy.

---

## Privacidad de las fotos

Las fotos se guardan en el sistema de archivos del servidor (por
defecto `./data/images`), nunca dentro de la base de datos. Esto
permite:

- Respaldar el armario con un simple `tar` o `rsync`.
- Cambiar de servidor sin perder nada.
- Mantener la base de datos pequeña (las imágenes grandes no
  inflan la BD).

Los archivos se nombran con un UUID y el nombre original del archivo
se guarda como metadato. Si el registro de la BD se borra pero el
archivo sigue ahí, no pasa nada: la próxima limpieza los recoge.

---

## Respaldo y portabilidad

Todo tu armario cabe en tres cosas:

1. La **base de datos** (Postgres): un `pg_dump` y listo.
2. La **carpeta de imágenes**: cópiala tal cual.
3. El **código** de la aplicación: ya está en tu propio servidor.

Restaurar es igual de fácil: crear la BD, volcar el dump, copiar las
imágenes, arrancar el contenedor.

---

## Glosario rápido

| Concepto        | Qué significa                                                |
| --------------- | ------------------------------------------------------------ |
| **Prenda**      | Cualquier cosa en tu armario: camiseta, pantalón, zapato…    |
| **Ubicación**   | Dónde la guardas físicamente                                |
| **Categoría**   | El tipo general (Pantalón, Jersey, Zapato…)                  |
| **Subcategoría**| El subtipo dentro de la categoría (Vaqueros dentro de Pantalón) |
| **Temporada**   | Verano o Invierno                                            |
| **Conjunto**    | Una combinación guardada de prendas para un look             |
| **Estado**      | Armario → En venta → Vendida                                |

---

## Privacidad y código

Wardrobe es **código privado** del usuario que lo despliega. No hay
telemetría, no hay servicios externos, no hay CDN de terceros.

Si quieres estudiarlo o modificarlo, todo el código está en el
repositorio y es razonablemente pequeño: un backend Spring Boot con
JPA + Flyway y un frontend Vue 3 sin librerías pesadas de UI.

¡Disfruta de tu armario ordenado!
