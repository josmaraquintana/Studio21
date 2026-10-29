package com.example.Studio21.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.Studio21.enums.CondicionProducto;
import com.example.Studio21.enums.EstadoDisponibilidad;
import com.example.Studio21.enums.EstadoVerificacion;
import com.example.Studio21.model.Artista;
import com.example.Studio21.model.Categoria;
import com.example.Studio21.model.Donacion;
import com.example.Studio21.model.ImagenProducto;
import com.example.Studio21.model.MovimientoInventario;
import com.example.Studio21.model.Producto;
import com.example.Studio21.model.Usuario;
import com.example.Studio21.repository.ArtistaRepository;
import com.example.Studio21.repository.CarritoRepository;
import com.example.Studio21.repository.CategoriaRepository;
import com.example.Studio21.repository.DonacionRepository;
import com.example.Studio21.repository.MovimientoInventarioRepository;
import com.example.Studio21.repository.PedidoRepository;
import com.example.Studio21.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Lógica de negocio de las piezas: alta, edición, fotografías, autenticidad y publicación.
@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ArtistaRepository artistaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private DonacionRepository donacionRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    // required = false: la app arranca aunque Cloudinary no esté configurado.
    @Autowired(required = false)
    private Cloudinary cloudinary;

    // Carpeta para la alternativa local; Spring la sirve en la URL /uploads/** (ver WebConfig).
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public List<Producto> buscar(String texto, EstadoVerificacion verificacion, EstadoDisponibilidad disponibilidad) {
        String filtro = (texto == null || texto.isBlank()) ? null : texto.trim();
        return productoRepository.buscar(filtro, verificacion, disponibilidad);
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAllByOrderByNombreAsc();
    }

    public List<Producto> listarPorVerificacion(EstadoVerificacion estado) {
        return productoRepository.findByEstadoVerificacionOrderByIdAsc(estado);
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    // Contadores del resumen.
    public long contarPendientesDeRevision() {
        return productoRepository.countByEstadoVerificacion(EstadoVerificacion.PENDIENTE);
    }

    public long contarPublicadasDisponibles() {
        return productoRepository.countByActivoAndEstadoDisponibilidad(true, EstadoDisponibilidad.DISPONIBLE);
    }

    public long contarPorDisponibilidad(EstadoDisponibilidad estado) {
        return productoRepository.countByEstadoDisponibilidad(estado);
    }

    // Alta de una pieza: queda pendiente de revisión y se registra su entrada física (+1) al inventario.
    public Producto crear(String nombre, String descripcion, Long artistaId, Long categoriaId, Long donacionId,
                          CondicionProducto condicion, String detalleCondicion, String talla,
                          BigDecimal precioOriginal, BigDecimal precioReventa,
                          MultipartFile foto, Usuario admin) {
        if (admin == null) {
            throw new IllegalStateException("No hay un administrador en sesión.");
        }
        Artista artista = obtenerArtista(artistaId);
        Categoria categoria = obtenerCategoria(categoriaId);
        if (donacionId == null) {
            throw new IllegalArgumentException("Selecciona la donación de origen.");
        }
        Donacion donacion = donacionRepository.findById(donacionId)
                .orElseThrow(() -> new IllegalArgumentException("La donación seleccionada no existe."));

        validarDatos(nombre, descripcion, categoria, condicion, detalleCondicion, talla, precioOriginal, precioReventa);

        // La foto se sube antes de crear la pieza para no dejar nada a medias si falla.
        String urlFoto = null;
        if (foto != null && !foto.isEmpty()) {
            urlFoto = guardarImagen(foto);
        }

        Producto producto = new Producto(nombre.trim(), descripcion.trim(), precioOriginal, precioReventa,
                condicion, limpiar(detalleCondicion), limpiar(talla), artista, categoria, donacion);

        if (urlFoto != null) {
            new ImagenProducto(producto, urlFoto); // se agrega sola a la lista de imágenes
        }

        producto = productoRepository.save(producto);

        movimientoRepository.save(new MovimientoInventario(producto, admin, 1,
                "Recepción DON-" + donacion.getId(), LocalDateTime.now()));
        return producto;
    }

    // Edición: solo se permite con la pieza disponible y sin publicar.
    public void actualizar(Long id, String nombre, String descripcion, Long artistaId, Long categoriaId,
                           CondicionProducto condicion, String detalleCondicion, String talla,
                           BigDecimal precioOriginal, BigDecimal precioReventa, MultipartFile foto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pieza no encontrada."));

        // Se revisa antes de cambiar cualquier dato para no dejar la pieza a medias.
        if (producto.getEstadoDisponibilidad() != EstadoDisponibilidad.DISPONIBLE) {
            throw new IllegalStateException("La pieza está apartada o vendida; ya no se puede editar.");
        }
        if (producto.isActivo()) {
            throw new IllegalStateException("Retira la publicación antes de editar la pieza.");
        }

        Artista artista = obtenerArtista(artistaId);
        Categoria categoria = obtenerCategoria(categoriaId);
        validarDatos(nombre, descripcion, categoria, condicion, detalleCondicion, talla, precioOriginal, precioReventa);

        // Si no se sube una foto nueva, se conservan las anteriores.
        String urlFoto = null;
        if (foto != null && !foto.isEmpty()) {
            urlFoto = guardarImagen(foto);
        }

        producto.setNombre(nombre.trim());
        producto.setDescripcion(descripcion.trim());
        producto.setArtista(artista);
        producto.setCategoria(categoria);
        producto.setCondicion(condicion);
        producto.setDetalleCondicion(limpiar(detalleCondicion));
        producto.setTalla(limpiar(talla));
        producto.setPrecioOriginal(precioOriginal);
        producto.setPrecioReventa(precioReventa);

        if (urlFoto != null) {
            new ImagenProducto(producto, urlFoto);
        }

        productoRepository.save(producto);
    }

    // Una pieza que ya forma parte de un pedido o de un carrito no se elimina, para no perder el historial.
    public void eliminar(Long id) {
        if (pedidoRepository.existsByDetallesProductoId(id)) {
            throw new IllegalStateException("No se puede eliminar: la pieza forma parte de un pedido.");
        }
        if (carritoRepository.existsByDetallesProductoId(id)) {
            throw new IllegalStateException("No se puede eliminar: la pieza está en el carrito de un cliente.");
        }
        // Los movimientos de inventario apuntan a la pieza; se eliminan junto con ella.
        movimientoRepository.deleteAll(movimientoRepository.findByProductoId(id));
        productoRepository.deleteById(id);
    }

    public void publicar(Long id) {
        Producto producto = obtenerProducto(id);
        producto.publicar(); // la entidad exige que esté verificada y disponible
        productoRepository.save(producto);
    }

    public void retirarPublicacion(Long id) {
        Producto producto = obtenerProducto(id);
        producto.retirarPublicacion();
        productoRepository.save(producto);
    }

    // Verificar habilita la publicación, pero no publica automáticamente.
    public void verificarAutenticidad(Long id, Usuario admin, String evidencia) {
        Producto producto = obtenerProducto(id);
        if (admin == null) {
            throw new IllegalStateException("No hay un administrador en sesión.");
        }
        producto.verificarAutenticidad(admin, evidencia);
        productoRepository.save(producto);
    }

    public void rechazarAutenticidad(Long id, Usuario admin, String motivo) {
        Producto producto = obtenerProducto(id);
        if (admin == null) {
            throw new IllegalStateException("No hay un administrador en sesión.");
        }
        producto.rechazarAutenticidad(admin, motivo);
        productoRepository.save(producto);
    }

    private Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pieza no encontrada."));
    }

    private Artista obtenerArtista(Long artistaId) {
        if (artistaId == null) {
            throw new IllegalArgumentException("Selecciona un artista.");
        }
        return artistaRepository.findById(artistaId)
                .orElseThrow(() -> new IllegalArgumentException("El artista seleccionado no existe."));
    }

    private Categoria obtenerCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw new IllegalArgumentException("Selecciona una categoría.");
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("La categoría seleccionada no existe."));
    }

    // Reglas de negocio de la ficha de una pieza.
    private void validarDatos(String nombre, String descripcion, Categoria categoria, CondicionProducto condicion,
                              String detalleCondicion, String talla,
                              BigDecimal precioOriginal, BigDecimal precioReventa) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (nombre.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre admite máximo 100 caracteres.");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        if (descripcion.trim().length() > 500) {
            throw new IllegalArgumentException("La descripción admite máximo 500 caracteres.");
        }
        if (condicion == null) {
            throw new IllegalArgumentException("Selecciona la condición de la pieza.");
        }
        if (condicion == CondicionProducto.USADA_CON_DETALLES && limpiar(detalleCondicion) == null) {
            throw new IllegalArgumentException("Describe el desgaste de una pieza usada con detalles.");
        }
        if (categoria.isRopa() && limpiar(talla) == null) {
            throw new IllegalArgumentException("La ropa debe tener talla.");
        }
        if (precioReventa == null || precioReventa.signum() <= 0) {
            throw new IllegalArgumentException("El precio de reventa debe ser mayor que cero.");
        }
        if (precioOriginal != null && precioOriginal.signum() < 0) {
            throw new IllegalArgumentException("El precio original no puede ser negativo.");
        }
    }

    // Convierte los textos vacíos en null.
    private String limpiar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }

    // Valida que sea imagen y la sube. Para guardar en disco cambia a: return guardarImagenLocal(foto);
    private String guardarImagen(MultipartFile foto) {
        // accept="image/*" solo filtra en el navegador; aquí se valida en el servidor.
        if (foto.getContentType() == null || !foto.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("La fotografía debe ser una imagen JPG o PNG.");
        }
        return guardarImagenCloudinary(foto);
    }

    // Sube la imagen a Cloudinary y regresa su URL https, que se guarda en ImagenProducto.
    private String guardarImagenCloudinary(MultipartFile foto) {
        if (cloudinary == null) {
            throw new IllegalStateException("Cloudinary no está configurado.");
        }
        try {
            Map uploadResult = cloudinary.uploader().upload(foto.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("secure_url").toString();
        } catch (Exception e) {
            System.err.println("Error al subir a Cloudinary. Revisa tus credenciales.");
            e.printStackTrace();
            throw new IllegalStateException("No se pudo subir la fotografía a Cloudinary. Revisa las credenciales en application.properties.");
        }
    }

    // Alternativa sin nube: guarda la imagen en static/uploads con un nombre único (UUID) y regresa la URL local.
    private String guardarImagenLocal(MultipartFile foto) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String original = foto.getOriginalFilename() == null ? "foto" : foto.getOriginalFilename();
            // Se quitan caracteres raros para que el nombre no pueda salirse de la carpeta.
            String nombreArchivo = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
            Files.copy(foto.getInputStream(), uploadPath.resolve(nombreArchivo));

            return "/uploads/" + nombreArchivo;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la fotografía.");
        }
    }
}
