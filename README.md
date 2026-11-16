# Studio 21

Proyecto desarrollado para la materia de **Aplicaciones Web**.

Studio 21 es una tienda de reventa de mercancía original de artistas obtenida mediante donaciones. Cada producto representa una pieza única y contiene información como condición, talla, precio y disponibilidad. Antes de ser publicado, el producto pasa por un proceso de verificación de autenticidad.

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Security
- Thymeleaf
- Spring Data JPA
- MySQL
- Cloudinary
- Maven
- BCrypt

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