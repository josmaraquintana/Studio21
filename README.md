# Studio 21

Proyecto para la materia de Aplicaciones Web. La idea es una tienda de reventa de mercancía original de artistas que se obtiene mediante donaciones.

Cada producto representa una pieza única. Por eso se muestra su condición, talla cuando es ropa, precio de reventa y estado de disponibilidad. También se contempla revisar su autenticidad antes de publicarla.

## Qué incluye este avance

En este avance integramos el maquetado HTML y CSS al proyecto de Spring Boot:

- Catálogo y detalle de producto.
- Carrito, confirmación de compra, historial y detalle de pedidos.
- Registro, inicio de sesión, cuenta y direcciones.
- Panel administrativo con productos, autenticidad, donaciones, inventario, pedidos, categorías y artistas.

El diseño se adapta a móvil, tablet y computadora. El CSS está hecho con variables, Grid, Flexbox y pseudo-clases, sin Bootstrap ni Tailwind. Las páginas comparten elementos como el encabezado, el menú, el pie y las tarjetas mediante fragmentos de Thymeleaf.

## Tecnologías

- Java 21.
- Spring Boot.
- Maven.
- Thymeleaf.
- HTML y CSS.

## Cómo ejecutarlo

1. Abre en IntelliJ IDEA la carpeta que contiene el archivo `pom.xml`.
2. Configura el JDK 21 y carga el proyecto como Maven.
3. Espera a que se descarguen las dependencias.
4. Ejecuta `Studio21Application.java`.

Al iniciar, abre:

- Tienda: [http://localhost:8080/](http://localhost:8080/)
- Administración: [http://localhost:8080/admin](http://localhost:8080/admin)

Para revisar este avance no es necesario iniciar sesión ni configurar una base de datos.

## Estado actual

Los productos, pedidos y donaciones utilizan datos de ejemplo. Los formularios y cambios de estado sirven para mostrar cómo será el recorrido de la aplicación; todavía no guardan información ni realizan compras o pagos reales.

La autenticación, la búsqueda, la conexión con MySQL y la integración de las reglas de negocio con las pantallas quedan pendientes. El proyecto conserva las dependencias de JPA y MySQL, pero la configuración de la base de datos está desactivada para poder ejecutar el maquetado.

## Archivos principales

- `src/main/resources/templates/`: páginas HTML y fragmentos compartidos.
- `src/main/resources/static/css/styles.css`: estilos de la aplicación.
- `src/main/resources/static/img/`: fotografías y logos.
- `src/main/resources/application.properties`: configuración de ejecución.
- `HomeController.java`: rutas de las páginas.
- `DatosMaquetado.java`: información de ejemplo para las vistas.
