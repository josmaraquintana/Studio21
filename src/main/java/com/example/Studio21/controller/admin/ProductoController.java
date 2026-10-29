package com.example.Studio21.controller.admin;

import com.example.Studio21.enums.CondicionProducto;
import com.example.Studio21.enums.EstadoDisponibilidad;
import com.example.Studio21.enums.EstadoVerificacion;
import com.example.Studio21.model.Producto;
import com.example.Studio21.service.ArtistaService;
import com.example.Studio21.service.CategoriaService;
import com.example.Studio21.service.DonacionService;
import com.example.Studio21.service.ProductoService;
import com.example.Studio21.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

// Catálogo administrativo: alta, edición con fotografía, eliminación y publicación de piezas.
@Controller
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ArtistaService artistaService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private DonacionService donacionService;

    @Autowired
    private UsuarioService usuarioService;

    @ModelAttribute
    public void datosComunes(Model model) {
        model.addAttribute("admin", true);
        model.addAttribute("seccion", "productos");
    }

    // Búsqueda por GET: los filtros viajan en la URL y son opcionales.
    @GetMapping("/admin/productos")
    public String listar(@RequestParam(name = "buscar", required = false) String buscar,
                         @RequestParam(name = "verificacion", required = false) EstadoVerificacion verificacion,
                         @RequestParam(name = "disponibilidad", required = false) EstadoDisponibilidad disponibilidad,
                         Model model) {
        return mostrarListado(model, buscar, verificacion, disponibilidad);
    }

    @GetMapping("/admin/productos/nuevo")
    public String nuevo(@RequestParam(name = "donacionId", required = false) Long donacionId, Model model) {
        model.addAttribute("producto", new Producto()); // objeto vacío para que la vista no truene con null
        model.addAttribute("donacionSeleccionada", donacionId);
        return mostrarFormulario(model);
    }

    @PostMapping("/admin/productos/nuevo")
    public String guardar(@RequestParam("nombre") String nombre,
                          @RequestParam("descripcion") String descripcion,
                          @RequestParam(name = "artistaId", required = false) Long artistaId,
                          @RequestParam(name = "categoriaId", required = false) Long categoriaId,
                          @RequestParam(name = "donacionId", required = false) Long donacionId,
                          @RequestParam(name = "condicion", required = false) CondicionProducto condicion,
                          @RequestParam(name = "detalleCondicion", required = false) String detalleCondicion,
                          @RequestParam(name = "talla", required = false) String talla,
                          @RequestParam(name = "precioOriginal", required = false) BigDecimal precioOriginal,
                          @RequestParam(name = "precioReventa", required = false) BigDecimal precioReventa,
                          @RequestParam(name = "foto", required = false) MultipartFile foto,
                          Model model) {
        try {
            // El responsable de la recepción se toma de la sesión, nunca del formulario.
            productoService.crear(nombre, descripcion, artistaId, categoriaId, donacionId, condicion,
                    detalleCondicion, talla, precioOriginal, precioReventa, foto, usuarioService.obtenerAdminLogueado());
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("producto", new Producto());
            model.addAttribute("donacionSeleccionada", donacionId);
            return mostrarFormulario(model);
        }
        return "redirect:/admin/productos?guardado"; // PRG: recargar no duplica la pieza
    }

    @GetMapping("/admin/productos/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        Producto producto = productoService.buscarPorId(id);
        if (producto == null) {
            return "redirect:/admin/productos";
        }
        model.addAttribute("producto", producto); // mismo formulario para crear y editar
        return mostrarFormulario(model);
    }

    @PostMapping("/admin/productos/editar/{id}")
    public String actualizar(@PathVariable Long id,
                             @RequestParam("nombre") String nombre,
                             @RequestParam("descripcion") String descripcion,
                             @RequestParam(name = "artistaId", required = false) Long artistaId,
                             @RequestParam(name = "categoriaId", required = false) Long categoriaId,
                             @RequestParam(name = "condicion", required = false) CondicionProducto condicion,
                             @RequestParam(name = "detalleCondicion", required = false) String detalleCondicion,
                             @RequestParam(name = "talla", required = false) String talla,
                             @RequestParam(name = "precioOriginal", required = false) BigDecimal precioOriginal,
                             @RequestParam(name = "precioReventa", required = false) BigDecimal precioReventa,
                             @RequestParam(name = "foto", required = false) MultipartFile foto,
                             Model model) {
        try {
            productoService.actualizar(id, nombre, descripcion, artistaId, categoriaId, condicion,
                    detalleCondicion, talla, precioOriginal, precioReventa, foto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Producto producto = productoService.buscarPorId(id);
            if (producto == null) {
                return "redirect:/admin/productos";
            }
            model.addAttribute("error", e.getMessage());
            model.addAttribute("producto", producto);
            return mostrarFormulario(model);
        }
        return "redirect:/admin/productos?guardado";
    }

    @PostMapping("/admin/productos/eliminar/{id}")
    public String eliminar(@PathVariable Long id, Model model) {
        try {
            productoService.eliminar(id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarListado(model, null, null, null);
        }
        return "redirect:/admin/productos?eliminado";
    }

    @PostMapping("/admin/productos/publicar/{id}")
    public String publicar(@PathVariable Long id, Model model) {
        try {
            productoService.publicar(id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarListado(model, null, null, null);
        }
        return "redirect:/admin/productos?publicado";
    }

    @PostMapping("/admin/productos/retirar/{id}")
    public String retirar(@PathVariable Long id, Model model) {
        try {
            productoService.retirarPublicacion(id);
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return mostrarListado(model, null, null, null);
        }
        return "redirect:/admin/productos?retirado";
    }

    private String mostrarListado(Model model, String buscar, EstadoVerificacion verificacion,
                                  EstadoDisponibilidad disponibilidad) {
        model.addAttribute("productos", productoService.buscar(buscar, verificacion, disponibilidad));
        model.addAttribute("buscar", buscar);
        model.addAttribute("verificacion", verificacion);
        model.addAttribute("disponibilidad", disponibilidad);
        model.addAttribute("estadosVerificacion", EstadoVerificacion.values());
        model.addAttribute("estadosDisponibilidad", EstadoDisponibilidad.values());
        return "admin/productos";
    }

    // Listas para los select del formulario.
    private String mostrarFormulario(Model model) {
        model.addAttribute("artistas", artistaService.listar());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("donaciones", donacionService.listar());
        model.addAttribute("condiciones", CondicionProducto.values());
        return "admin/producto-form";
    }
}
