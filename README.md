# Studio 21

Proyecto desarrollado para la materia de **Aplicaciones Web**.

Studio 21 es una tienda de reventa de mercancía original de artistas obtenida mediante donaciones. Cada producto representa una pieza única y contiene información como condición, talla, precio y disponibilidad. Antes de ser publicado, el producto pasa por un proceso de verificación de autenticidad.

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Security
- JWT (JJWT)
- Thymeleaf
- Spring Data JPA
- Bean Validation
- MySQL
- Cloudinary
- Maven
- BCrypt
- Postman

## Funcionalidades principales

El sistema cuenta con un panel de administración protegido mediante autenticación y roles.

Desde el panel se pueden administrar:

- Productos
- Donaciones y donantes
- Verificación de autenticidad
- Inventario
- Pedidos
- Categorías
- Artistas

También permite:

- Iniciar y cerrar sesión.
- Registrar y editar productos.
- Subir imágenes de productos a Cloudinary.
- Publicar o retirar productos.
- Aprobar o rechazar la autenticidad de una pieza.
- Registrar movimientos de inventario.
- Administrar el estado de los pedidos.

## Estructura del proyecto

La aplicación sigue una arquitectura organizada en capas:

- `controller`: manejo de solicitudes y navegación.
- `service`: lógica de negocio.
- `repository`: acceso a datos mediante Spring Data JPA.
- `model`: entidades del sistema.
- `templates`: vistas Thymeleaf.

## Configuración

Antes de ejecutar el proyecto se deben configurar las siguientes variables de entorno:

```properties
DB_URL
DB_USERNAME
DB_PASSWORD

CLOUDINARY_CLOUD_NAME
CLOUDINARY_API_KEY
CLOUDINARY_API_SECRET
```

Las credenciales de Cloudinary y de la base de datos **no deben subirse al repositorio**.

## Cómo ejecutar el proyecto

1. Tener instalado **Java 21** y **MySQL**.

2. Configurar las variables de entorno de MySQL y Cloudinary.

3. Abrir en IntelliJ IDEA la carpeta que contiene el archivo `pom.xml`.

4. Configurar el proyecto para utilizar JDK 21.

5. Ejecutar:

```text
Studio21Application.java
```

6. Abrir el panel de administración en:

```text
http://localhost:8080/admin
```

La tienda pública se encuentra en:

```text
http://localhost:8080/
```

## Datos de demostración

El proyecto incluye `datos-mock.sql` para cargar información de prueba en una base de datos local.

Antes de utilizarlo, se debe permitir que JPA cree las tablas de la aplicación.

Credenciales administrativas de prueba:

| Usuario | Correo | Contraseña |
|---|---|---|
| Ori | `ori@studio21.com` | `admin123` |
| Jos | `jos@studio21.com` | `admin123` |
| Piña | `pina@studio21.com` | `admin123` |

> Las cuentas y datos incluidos en el script son únicamente para demostración.

## Pruebas

Las pruebas automatizadas pueden ejecutarse con:

```bash
./mvnw test
```

El entorno de pruebas utiliza una base de datos H2 independiente de la base de datos MySQL utilizada por la aplicación.

## Avance 4.1 · API REST de la tienda


### Requisitos de instalación

- Java 21 (JDK)
- MySQL 8 o superior corriendo en `localhost:3306`
- IntelliJ IDEA u otro IDE con soporte para Maven
- Postman, para probar la API

No es necesario instalar Maven: el proyecto incluye el wrapper (`mvnw` y `mvnw.cmd`).

### Configuración

En `src/main/resources/application.properties`, cambiar el usuario y la contraseña de MySQL por los del equipo local:

```properties
spring.datasource.username=root
spring.datasource.password=1234
```

Es lo único obligatorio para que arranque. La base `studio21db` se crea sola. El JWT y Cloudinary tienen valores por defecto, así que no hace falta configurarlos para probar la API.

### Ejecución

1. Ejecutar `Studio21Application.java`.
2. Detener la aplicación y ejecutar `datos-mock.sql` en MySQL Workbench.
3. Volver a ejecutar `Studio21Application.java`.
4. La API queda en `http://localhost:8080/api`.

### Credenciales de prueba de la API

| Rol | Correo | Contraseña |
|---|---|---|
| Cliente | `cliente@example.com` | `cliente123` |

El cliente inicia sesión con `POST /api/auth/login` y recibe un `token`. Las rutas protegidas se llaman con el header `Authorization: Bearer {token}`.

### Endpoints

| Método | Endpoint | Pantalla que lo consume | Token |
|---|---|---|---|
| POST | `/api/auth/registro` | `registro.html` | No |
| POST | `/api/auth/login` | `login.html` | No |
| GET | `/api/cuenta` | `cuenta.html` (Mis datos) | Sí |
| PUT | `/api/cuenta` | `cuenta.html` (guardar cambios) | Sí |
| GET | `/api/productos` | `catalogo.html` (lista, buscador, filtros y orden) | No |
| GET | `/api/productos/{id}` | `producto.html` | No |
| GET | `/api/categorias` | `catalogo.html` (filtro por categoría) | No |
| GET | `/api/artistas` | `catalogo.html` (filtro por artista) | No |
| GET | `/api/direcciones` | `direcciones.html` y `checkout.html` (paso 1) | Sí |
| POST | `/api/direcciones` | `direcciones.html` (guardar dirección) | Sí |
| PUT | `/api/direcciones/{id}` | `direcciones.html` (editar dirección) | Sí |
| DELETE | `/api/direcciones/{id}` | `direcciones.html` (quitar dirección) | Sí |
| GET | `/api/carrito` | `carrito.html` | Sí |
| POST | `/api/carrito/items` | `producto.html` (agregar al carrito) | Sí |
| DELETE | `/api/carrito/items/{productoId}` | `carrito.html` (quitar pieza) | Sí |
| POST | `/api/pedidos` | `checkout.html` (confirmar pedido) | Sí |
| GET | `/api/pedidos` | `pedidos.html` | Sí |
| GET | `/api/pedidos/{id}` | `pedido.html` | Sí |
| PATCH | `/api/pedidos/{id}/cancelacion` | `pedido.html` (cancelar) | Sí |

### Pruebas con Postman

1. En Postman, `Import` y seleccionar los dos archivos de la carpeta `postman`.
2. Elegir el environment **Studio21 Local** arriba a la derecha.
3. Con la aplicación corriendo y los datos de demostración cargados, abrir la colección **Studio21 API** y dar clic en **Run**.