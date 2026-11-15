-- =====================================================================
-- Studio 21 · Datos mock para el panel de administración (Avance 3)

--   1. Crea el esquema con JPA en la base local de demostración elegida.
--   2. Detén la aplicación y revisa USE antes de ejecutar este archivo manualmente.
--   3. Reinicia la aplicación y abre http://localhost:8080/admin.
-- =====================================================================

USE studio21db;

-- Usuarios: los tres integrantes son ADMIN; el comprador es una cuenta separada.
-- Los correos identifican cuentas de demostración, no direcciones personales reales.
-- Las contraseñas se almacenan únicamente como hashes BCrypt.
INSERT INTO usuario (id, nombre, correo, contrasenia_hash, telefono, rol) VALUES
(1, 'Ori', 'ori@studio21.com', '$2a$10$bwCEY3DwYlQsalt2kA5XUOZMnaOg5MJ8s/ltVvM.OLUSIov02R77y', '000-000-0001', 'ADMIN'),
(2, 'Jos', 'jos@studio21.com', '$2a$10$bwCEY3DwYlQsalt2kA5XUOZMnaOg5MJ8s/ltVvM.OLUSIov02R77y', '000-000-0002', 'ADMIN'),
(3, 'Piña', 'pina@studio21.com', '$2a$10$bwCEY3DwYlQsalt2kA5XUOZMnaOg5MJ8s/ltVvM.OLUSIov02R77y', '000-000-0003', 'ADMIN'),
(4, 'Cliente de prueba', 'cliente@example.com', '$2a$10$Ep34T86if3V4l1WlhKAr2.9o2QaVqmi0Js9Ar45jP5HoUcX9N6AC.', '000-000-0004', 'CLIENTE');

-- Direcciones ficticias del cliente (ID 4), nunca de Piña (ID 3).
INSERT INTO direccion (id, usuario_id, destinatario, telefono, calle, numero_exterior, colonia, ciudad, estado_direccion, codigo_postal) VALUES
(1, 4, 'Cliente de prueba', '000-000-0004', 'Calle de Prueba', '123', 'Colonia Ficticia', 'Ciudad de Prueba', 'Estado de Prueba', '00000'),
(2, 4, 'Cliente de prueba', '000-000-0004', 'Avenida de Prueba', '456', 'Colonia Simulada', 'Ciudad de Prueba', 'Estado de Prueba', '00000');

-- Categorías
INSERT INTO categoria (id, nombre, descripcion, ropa) VALUES
(1, 'Playeras',   'Playeras de artistas y giras',  1),
(2, 'Sudaderas',  'Sudaderas y prendas de punto',  1),
(3, 'Vinilos',    'Discos de música',              0),
(4, 'Accesorios', 'Pósters y otros accesorios',    0);

-- Artistas
INSERT INTO artista (id, nombre, genero_musical) VALUES
(1, 'Taylor Swift', 'Pop'),
(2, 'Rosalía',      'Pop'),
(3, 'Charli XCX',   'Pop electrónico'),
(4, 'Daft Punk',    'Electrónica'),
(5, 'The Strokes',  'Rock');

-- Donantes y donaciones
INSERT INTO donante (id, nombre, telefono, correo) VALUES
(1, 'Donante de prueba 1', '000-000-0011', 'donante1@example.com'),
(2, 'Donante de prueba 2', '000-000-0012', 'donante2@example.com');

INSERT INTO donacion (id, donante_id, fecha_recepcion, recibido_por_id, observaciones) VALUES
(1, 1, '2026-10-18 10:15:00', 2, 'Dos prendas entregadas en mano. Donación voluntaria sin compensación económica.'),
(2, 2, '2026-10-24 12:30:00', 3, 'Pieza recibida por Piña para revisión de autenticidad.');

-- Piezas (las fotos de ejemplo usan las imágenes que ya existen en static/img;
-- las que subas desde el panel se guardan en Cloudinary)
-- 1: verificada y publicada · 2: apartada por el pedido 1 · 3: pendiente de revisión
INSERT INTO producto (id, version, nombre, descripcion, precio_original, precio_reventa, condicion, detalle_condicion, talla,
                      estado_verificacion, estado_disponibilidad, activo, artista_id, categoria_id, donacion_id,
                      verificado_por_id, fecha_verificacion, evidencia_verificacion, pedido_reserva_id) VALUES
(1, 0, 'Playera Motomami', 'Playera clara de la gira Motomami World Tour 2022.', 60.00, 35.00, 'COMO_NUEVA', NULL, 'M',
 'VERIFICADA', 'DISPONIBLE', 1, 2, 1, 1, 1, '2026-10-19 09:00:00', 'Etiqueta oficial de la gira y estampado original.', NULL),
(2, 0, 'Playera Tour The Eras', 'Playera negra de la gira The Eras. Estampado frontal con fotografías de los álbumes.', 55.00, 28.00, 'USADA_CON_DETALLES',
 'Ligero desgaste en el estampado por lavado. Tela y costuras sin roturas.', 'M',
 'VERIFICADA', 'APARTADA', 1, 1, 1, 1, 3, '2026-10-19 09:20:00', 'Etiqueta y costuras de merch oficial.', NULL),
(3, 0, 'Playera de gira', 'Playera negra recibida por donación, pendiente de revisión.', 40.00, 25.00, 'USADA_CON_DETALLES',
 'Desgaste leve en estampado.', 'M',
 'PENDIENTE', 'DISPONIBLE', 0, 5, 1, 2, NULL, NULL, NULL, NULL);

INSERT INTO imagen_producto (id, url, producto_id) VALUES
(1, '/img/playera-motomami.jpg', 1),
(2, '/img/playera-eras.jpg',     2),
(3, '/img/playera-revision.jpg', 3);

-- Entrada física (+1) de cada pieza al recibirla
INSERT INTO movimiento_inventario (id, fecha, cambio_cantidad, motivo, producto_id, registrado_por_id) VALUES
(1, '2026-10-18 10:20:00', 1, 'Recepción DON-1', 1, 2),
(2, '2026-10-18 10:21:00', 1, 'Recepción DON-1', 2, 2),
(3, '2026-10-24 12:35:00', 1, 'Recepción DON-2', 3, 3);

-- Pedido confirmado y pendiente de pago (la pieza 2 queda apartada)
INSERT INTO pedido (id, version, numero_pedido, fecha_compra, total, estado, metodo_pago,
                    destinatario, telefono, calle, numero_exterior, colonia, ciudad, estado_direccion, codigo_postal,
                    fecha_envio, fecha_entrega, paqueteria, numero_guia, confirmado, usuario_id) VALUES
(1, 0, 'ORD-8492', '2026-10-25 18:40:00', 28.00, 'PENDIENTE', 'TRANSFERENCIA',
 'Cliente de prueba', '000-000-0004', 'Calle de Prueba', '123', 'Colonia Ficticia', 'Ciudad de Prueba', 'Estado de Prueba', '00000',
 NULL, NULL, NULL, NULL, 1, 4);

INSERT INTO detalle_pedido (id, pedido_id, producto_id, cantidad, precio_unitario_compra) VALUES
(1, 1, 2, 1, 28.00);

UPDATE producto SET pedido_reserva_id = 1 WHERE id = 2;

-- Persona 2 · Catálogo público: SOLO el bloque nuevo.
-- Ejecútalo en MySQL Workbench DESPUÉS de haber corrido el datos-mock.sql original.
-- No lo ejecutes dos veces (los ids se repetirían).

USE studio21db;
-- =====================================================================
-- Catálogo público (API). Piezas extra para probar filtros y estados.
-- 4-7: verificadas y publicadas · 8: vendida (sigue visible como "Vendida")
-- 9: rechazada y 10: retirada (NO deben aparecer en /api/productos)
-- =====================================================================
INSERT INTO producto (id, version, nombre, descripcion, precio_original, precio_reventa, condicion, detalle_condicion, talla,
                      estado_verificacion, estado_disponibilidad, activo, artista_id, categoria_id, donacion_id,
                      verificado_por_id, fecha_verificacion, evidencia_verificacion, pedido_reserva_id) VALUES
(4, 0, 'Sudadera Brat', 'Sudadera verde lima con el logotipo del álbum Brat.', 70.00, 42.00, 'NUEVA', NULL, 'L',
 'VERIFICADA', 'DISPONIBLE', 1, 3, 2, 2, 1, '2026-10-26 10:00:00', 'Etiqueta oficial de la tienda del artista.', NULL),
(5, 0, 'Vinilo Random Access Memories', 'Edición doble en vinilo. Carátula en buen estado.', 45.00, 38.00, 'COMO_NUEVA',
 'Carátula con una marca mínima en una esquina.', NULL,
 'VERIFICADA', 'DISPONIBLE', 1, 4, 3, 2, 2, '2026-10-26 10:30:00', 'Sello y numeración de la edición oficial.', NULL),
(6, 0, 'Póster The Strokes', 'Póster de gira, impresión original.', 25.00, 18.00, 'COMO_NUEVA', NULL, NULL,
 'VERIFICADA', 'DISPONIBLE', 1, 5, 4, 2, 1, '2026-10-26 11:00:00', 'Impresión oficial con sello de la gira.', NULL),
(7, 0, 'Cárdigan Folklore', 'Cárdigan gris de la colección Folklore.', 90.00, 55.00, 'USADA_CON_DETALLES',
 'Bolitas de tela leves en los codos.', 'S',
 'VERIFICADA', 'DISPONIBLE', 1, 1, 2, 2, 1, '2026-10-26 11:30:00', 'Etiqueta oficial y costura original.', NULL),
(8, 0, 'Playera Eras (vendida)', 'Playera blanca de The Eras Tour, ya vendida.', 50.00, 30.00, 'COMO_NUEVA', NULL, 'S',
 'VERIFICADA', 'VENDIDA', 0, 1, 1, 2, 1, '2026-10-20 10:00:00', 'Etiqueta oficial.', NULL),
(9, 0, 'Playera sospechosa', 'Pieza rechazada por autenticidad.', 40.00, 20.00, 'USADA_CON_DETALLES', 'Estampado dudoso.', 'M',
 'RECHAZADA', 'DISPONIBLE', 0, 2, 1, 2, 1, '2026-10-26 12:00:00', 'No coincide con el merch oficial.', NULL),
(10, 0, 'Vinilo retirado', 'Pieza verificada pero retirada de la venta.', 40.00, 30.00, 'NUEVA', NULL, NULL,
 'VERIFICADA', 'DISPONIBLE', 0, 4, 3, 2, 1, '2026-10-26 12:30:00', 'Sello oficial.', NULL);

INSERT INTO imagen_producto (id, url, producto_id) VALUES
(4, '/img/sudadera-brat.jpg',      4),
(5, '/img/vinilo-daft-punk.jpg',   5),
(6, '/img/poster-strokes.jpg',     6),
(7, '/img/cardigan.jpg',           7),
(8, '/img/playera-eras.jpg',       8),
(9, '/img/playera-revision.jpg',   9),
(10, '/img/vinilo-daft-punk.jpg', 10);

INSERT INTO movimiento_inventario (id, fecha, cambio_cantidad, motivo, producto_id, registrado_por_id) VALUES
(4, '2026-10-25 10:00:00', 1, 'Recepción DON-1', 4, 2),
(5, '2026-10-25 10:01:00', 1, 'Recepción DON-1', 5, 2),
(6, '2026-10-25 10:02:00', 1, 'Recepción DON-1', 6, 2),
(7, '2026-10-25 10:03:00', 1, 'Recepción DON-1', 7, 2),
(8, '2026-10-20 10:00:00', 1, 'Recepción DON-1', 8, 2),
(9, '2026-10-25 10:04:00', 1, 'Recepción DON-1', 9, 2),
(10, '2026-10-25 10:05:00', 1, 'Recepción DON-1', 10, 2);
