-- =====================================================================
-- Studio 21 · Datos mock para el panel de administración (Avance 3)
--
-- Cómo usarlo:
--   1. Ejecuta la aplicación una vez para que JPA (ddl-auto=update) cree las tablas en studio21db.
--   2. Detén la aplicación y ejecuta este script en MySQL Workbench (o en la consola de MySQL).
--   3. Vuelve a iniciar la aplicación y entra a http://localhost:8080/admin
--
-- Accesos (las contraseñas se guardan cifradas con BCrypt, nunca en texto plano):
--   Administrador:  sofia.rivas@studio21.com    / admin123
--   Administrador:  carlos.mendoza@studio21.com / admin123
--   Cliente:        juan.perez@correo.com       / cliente123   (no puede entrar al panel)
-- =====================================================================

USE studio21db;

-- Usuarios
INSERT INTO usuario (id, nombre, correo, contrasenia_hash, telefono, rol) VALUES
(1, 'Sofía Rivas',    'sofia.rivas@studio21.com',    '$2a$10$bwCEY3DwYlQsalt2kA5XUOZMnaOg5MJ8s/ltVvM.OLUSIov02R77y', '+52 55 1000 2100', 'ADMIN'),
(2, 'Carlos Mendoza', 'carlos.mendoza@studio21.com', '$2a$10$bwCEY3DwYlQsalt2kA5XUOZMnaOg5MJ8s/ltVvM.OLUSIov02R77y', '+52 55 1000 2101', 'ADMIN'),
(3, 'Juan Pérez',     'juan.perez@correo.com',       '$2a$10$Ep34T86if3V4l1WlhKAr2.9o2QaVqmi0Js9Ar45jP5HoUcX9N6AC.', '+52 55 1234 5678', 'CLIENTE');

-- Dirección guardada del cliente
INSERT INTO direccion (id, usuario_id, destinatario, telefono, calle, numero_exterior, colonia, ciudad, estado_direccion, codigo_postal) VALUES
(1, 3, 'Juan Pérez', '+52 55 1234 5678', 'Av. Insurgentes Sur', '1450', 'Actipan', 'Ciudad de México', 'CDMX', '03230');

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
(1, 'Mariana López',  '+52 55 4192 8810', 'mariana.lopez@ejemplo.com'),
(2, 'Andrea Beltrán', '+52 55 3301 7720', NULL);

INSERT INTO donacion (id, donante_id, fecha_recepcion, recibido_por_id, observaciones) VALUES
(1, 1, '2026-10-18 10:15:00', 2, 'Dos prendas entregadas en mano. Donación voluntaria sin compensación económica.'),
(2, 2, '2026-10-24 12:30:00', 1, NULL);

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
 'VERIFICADA', 'APARTADA', 1, 1, 1, 1, 1, '2026-10-19 09:20:00', 'Etiqueta y costuras de merch oficial.', NULL),
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
(3, '2026-10-24 12:35:00', 1, 'Recepción DON-2', 3, 1);

-- Pedido confirmado y pendiente de pago (la pieza 2 queda apartada)
INSERT INTO pedido (id, version, numero_pedido, fecha_compra, total, estado, metodo_pago,
                    destinatario, telefono, calle, numero_exterior, colonia, ciudad, estado_direccion, codigo_postal,
                    fecha_envio, fecha_entrega, paqueteria, numero_guia, confirmado, usuario_id) VALUES
(1, 0, 'ORD-8492', '2026-10-25 18:40:00', 28.00, 'PENDIENTE', 'TRANSFERENCIA',
 'Juan Pérez', '+52 55 1234 5678', 'Av. Insurgentes Sur', '1450', 'Actipan', 'Ciudad de México', 'CDMX', '03230',
 NULL, NULL, NULL, NULL, 1, 3);

INSERT INTO detalle_pedido (id, pedido_id, producto_id, cantidad, precio_unitario_compra) VALUES
(1, 1, 2, 1, 28.00);

UPDATE producto SET pedido_reserva_id = 1 WHERE id = 2;
