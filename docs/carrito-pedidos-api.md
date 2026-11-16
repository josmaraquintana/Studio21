# API de carrito y pedidos de cliente

Implementación de backend para el Avance 4.1. Conserva las entidades, la autenticación JWT y la arquitectura Controller → Service → Repository. No conecta ni modifica las pantallas.

## Probar en Postman

Variables sugeridas: `baseUrl` = `http://localhost:8080`, `token`, `productoId`, `direccionId` y `pedidoId`.
Los IDs deben ser reales. `direccionId` debe identificar una dirección del cliente autenticado. Este módulo consulta la entidad `Direccion`; no crea una API para administrarla.

1. Iniciar sesión con una cuenta existente mediante `POST {{baseUrl}}/api/auth/login`, `Content-Type: application/json`:

   ```json
   {"correo":"cliente@ejemplo.com","contrasenia":"tu-contrasenia"}
   ```

2. Copiar `token` de la respuesta a la variable `token`. Opcionalmente, en Post-response:

   ```javascript
   pm.environment.set("token", pm.response.json().token);
   ```

3. Usar **Authorization → Bearer Token → `{{token}}`** en todas las peticiones siguientes. Para cuerpos JSON, usar `Content-Type: application/json`.

| Método y ruta | Cuerpo JSON de ejemplo | Respuesta correcta | Pantalla pendiente de conexión |
|---|---|---|---|
| `GET /api/carrito` | Sin cuerpo | `200`, `{ "items": [...], "total": 120.50 }` | Carrito y resumen de checkout |
| `POST /api/carrito/items` | `{ "productoId": 1 }` | `200`, carrito actualizado | Acción de agregar desde producto |
| `DELETE /api/carrito/items/1` | Sin cuerpo | `200`, carrito actualizado | Quitar pieza del carrito |
| `POST /api/pedidos` | `{ "direccionId": 1, "metodoPago": "TRANSFERENCIA" }` | `201`, detalle y cabecera `Location: /api/pedidos/{id}` | Confirmación de checkout |
| `GET /api/pedidos` | Sin cuerpo | `200`, lista propia del más reciente al más antiguo | Mis pedidos |
| `GET /api/pedidos/1` | Sin cuerpo | `200`, detalle propio | Detalle del pedido |
| `PATCH /api/pedidos/1/cancelacion` | Sin cuerpo | `200`, detalle cancelado | Confirmar cancelación |

Reemplazar los `1` por las variables correspondientes. Por ejemplo:

```http
POST {{baseUrl}}/api/carrito/items
Authorization: Bearer {{token}}
Content-Type: application/json

{"productoId": {{productoId}}}
```

```http
POST {{baseUrl}}/api/pedidos
Authorization: Bearer {{token}}
Content-Type: application/json

{"direccionId": {{direccionId}}, "metodoPago": "TRANSFERENCIA"}
```

El método de pago usa el enum existente: `TARJETA`, `TRANSFERENCIA` o `PAYPAL`. `EFECTIVO` no forma parte de ese contrato. Confirmar no ejecuta ningún cobro.

Carrito vacío, tanto si nunca se creó como si fue vaciado:

```json
{"items": [], "total": 0}
```

Cada item contiene `productoId`, `nombre`, `imagenUrl`, `cantidad` (siempre 1), `precioUnitario`, `subtotal` y `estadoDisponibilidad`. El carrito no reserva y puede contener piezas que otro cliente haya apartado después de agregarlas; checkout vuelve a validarlas.

El detalle de pedido devuelve `id`, `numeroPedido`, `fechaCompra`, `estado`, `confirmado`, `metodoPago`, `total`, `direccionEnvio`, `detalles`, `paqueteria`, `numeroGuia`, `fechaEnvio` y `fechaEntrega`. Cada detalle incluye `productoId`, `nombre`, `cantidad`, `precioUnitarioCompra` y `subtotal`. `direccionEnvio` es una copia con `destinatario`, `telefono`, `calle`, `numeroExterior`, `colonia`, `ciudad`, `estado` y `codigoPostal`.

La lista de pedidos devuelve resúmenes con `id`, `numeroPedido`, `fechaCompra`, `estado`, `metodoPago`, `total` y `piezas`. Ninguna respuesta expone entidades JPA ni datos de otros compradores.

No enviar `usuarioId`, precios, total o estado: el servidor determina esos valores. Los importes usan `BigDecimal` y el precio de cada detalle queda congelado al confirmar.

## Reglas, transacciones y errores

- Todas las rutas requieren JWT y reutilizan `UsuarioActual`. No se usa la sesión administrativa para autenticar la API.
- Agregar exige una pieza publicada, verificada, disponible y con saldo físico igual a uno. Los duplicados devuelven `409`. Agregar y quitar no generan movimientos ni reservas.
- Los cambios de carrito y checkout bloquean primero la fila del usuario. Esto también serializa la creación del primer carrito y evita dos pedidos desde el mismo contenido.
- Checkout lee los IDs de piezas y los bloquea por ID ascendente, antes de cargar sus estados. Reutiliza los bloqueos de producto y la comprobación física de administración, con aislamiento `READ_COMMITTED`.
- La confirmación, la reserva, el guardado de pedido/detalles y el vaciado del carrito pertenecen a una sola transacción. Ante fallo se revierte todo.
- Cancelación bloquea el pedido del dueño y después sus piezas por ID ascendente. Comparte el bloqueo de pedido con administración, reutiliza `Pedido.cancelar()` y solo admite `PENDIENTE`. Libera exclusivamente las reservas de ese pedido, sin entrada física `+1`.
- Se usa el formato central `ErrorResponse`: `400` para entradas inválidas; `401` sin JWT válido; `404` para recursos inexistentes o ajenos; `409` para conflictos de negocio, bloqueo u optimismo, incluidos los detectados al confirmar la transacción.
- Se corrigió el manejador central para reconocer la `NotFoundException` propia del proyecto, conservando el manejador anterior.

## Verificación realizada

- Compilación y siete pruebas existentes de seguridad mediante Maven con Java 21.
- Verificador temporal externo al repositorio, en `/tmp/studio21-carrito-verificacion`, con contexto real, MockMvc, filtros de seguridad, JWT obtenido por `/api/auth/login`, transacciones JPA y H2 en memoria. No usa puertos HTTP ni la base MySQL real. No se agregaron archivos de prueba al cambio final.
- Dieciocho grupos de comprobaciones aprobados:
  1. JWT obligatorio en las siete rutas y login real de API.
  2. Carrito inexistente con lista vacía y total cero.
  3. Validación de JSON, enum e IDs y errores centralizados `400`/`404`.
  4. Duplicado `409`, eliminación solo del carrito propio y saldo intacto.
  5. Dirección ajena `404`, conservando el carrito.
  6. Una pieza no disponible impide crear pedido, reservar o vaciar parcialmente.
  7. Confirmación reserva y vacía, calcula importes en servidor e ignora usuario/total/estado inyectados; pedidos ajenos e inexistentes `404`.
  8. Copia de dirección y precio histórico inmutables frente a cambios posteriores; cancelación sin `+1`; segunda cancelación `409`.
  9. Rechazo de saldo físico cero.
  10. Rechazo de piezas no publicadas/no verificadas.
  11. Dos clientes compiten por una pieza: un `201`, un `409`, un pedido y carrito perdedor intacto.
  12. Dos confirmaciones del mismo carrito: un `201`, un `409` y un solo pedido.
  13. Pedido pagado por administración no se cancela ni libera.
  14. Fallo posterior a `confirmar()` revierte pedido, reserva y vaciado.
  15. Conflicto optimista real al commit se traduce en `409` y revierte la escritura perdedora.
  16. Carritos con múltiples piezas agregadas en orden opuesto: un ganador, sin bloqueo circular.
  17. Listado propio por ID descendente.
  18. Pago administrativo concurrente con cancelación de cliente: un ganador, pedido/producto consistentes y saldo físico intacto.

Estas comprobaciones de concurrencia se ejecutaron en H2. Queda pendiente repetirlas en una instancia MySQL de pruebas independiente; no se consultó ni modificó la base real.

## Pendientes de integración visual

Ninguna pantalla consume todavía estas rutas. Se conservó el frontend existente y no se inventaron contratos de otros módulos.

- Autenticación: acordar el mecanismo frontend para obtener y enviar el JWT ya disponible en la API.
- Catálogo/producto: recibir el ID real del producto, en lugar del identificador visual de ejemplo.
- Direcciones: acordar cómo checkout lista y selecciona las direcciones del cliente para enviar su `direccionId`.
- Carrito, checkout, pedidos y detalle: conectar las acciones de la tabla anterior, representar errores y sustituir los datos de ejemplo por estas respuestas.
- El checkout visual incluye efectivo; coordinar su eliminación o un cambio acordado del contrato. Este backend conserva el enum vigente.

No se modificó la colección final de Postman, las entidades, las pantallas, login, catálogo, direcciones ni los servicios administrativos. Los cambios compartidos son métodos adicionales de repositorio y los manejadores centrales exigidos por estas rutas.

## Archivos del cambio

Nuevos:

- `controller/cliente/CarritoController.java`
- `controller/cliente/PedidoClienteController.java`
- `service/CarritoService.java`
- `service/PedidoClienteService.java`
- `dto/AgregarCarritoRequest.java`
- `dto/CrearPedidoRequest.java`
- `dto/CarritoResponse.java`
- `dto/PedidoResponse.java`
- `dto/PedidoResumenResponse.java`
- `repository/DireccionRepository.java`
- `docs/carrito-pedidos-api.md`

Modificados:

- `repository/CarritoRepository.java`
- `repository/PedidoRepository.java`
- `repository/UsuarioRepository.java`
- `exception/GlobalExceptionHandler.java`

Las rutas Java de esta lista son relativas a `src/main/java/com/example/Studio21/`.
