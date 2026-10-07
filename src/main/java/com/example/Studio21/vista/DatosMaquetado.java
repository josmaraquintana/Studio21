package com.example.Studio21.vista;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Datos de muestra para las vistas del avance. No consulta ni modifica la base de datos. */
@Component
public class DatosMaquetado {
    private final List<Map<String, Object>> productos = new ArrayList<>();

    public DatosMaquetado() {
        productos.add(pieza("eras", "Playera Tour The Eras", "Taylor Swift", "Playeras", "M", "Usada con detalles", 28, 55, "playera-eras.jpg", "Disponible", "available", "S21-001", "Playera negra de la gira The Eras. Estampado frontal con fotografías de los álbumes.", "Ligero desgaste en el estampado por lavado. Tela y costuras sin roturas.", "DON-104", true));
        productos.add(pieza("motomami", "Playera Motomami", "Rosalía", "Playeras", "M", "Como nueva", 35, 60, "playera-motomami.jpg", "Disponible", "available", "S21-002", "Playera clara de la gira Motomami World Tour 2022. Una pieza para seguir escuchando tus canciones favoritas.", "Sin manchas ni roturas visibles.", "DON-105", true));
        productos.add(pieza("cardigan", "Cárdigan bordado", "Taylor Swift", "Sudaderas", "S", "Como nueva", 42, 70, "cardigan.jpg", "Disponible", "available", "S21-003", "Cárdigan de punto claro con bordados y botones frontales.", "Tejido conservado y botones completos.", "DON-105", true));
        productos.add(pieza("vinilo", "Vinilo One More Time", "Daft Punk", "Vinilos", "No aplica", "Usada con detalles", 30, 40, "vinilo-daft-punk.jpg", "Apartada", "reserved", "S21-004", "Disco de vinilo con funda. Se entrega la única pieza fotografiada.", "Marcas leves en la funda. Disco revisado visualmente.", "DON-106", true));
        productos.add(pieza("brat", "Sudadera Brat", "Charli XCX", "Sudaderas", "L", "Como nueva", 48, 85, "sudadera-brat.jpg", "Vendida", "sold", "S21-005", "Sudadera negra con capucha y estampado verde de Brat. La imagen muestra el frente y reverso de una sola prenda.", "Sin manchas ni roturas visibles.", "DON-104", true));
        productos.add(pieza("poster", "Póster First Impressions", "The Strokes", "Accesorios", "No aplica", "Usada con detalles", 18, 25, "poster-strokes.jpg", "Vendida", "sold", "S21-006", "Póster de colección First Impressions of Earth. Marco de la fotografía no incluido.", "Marcas leves en los bordes del papel.", "DON-103", true));
        productos.add(pieza("revision", "Playera de gira", "The Strokes", "Playeras", "M", "Usada con detalles", 25, 40, "playera-revision.jpg", "Disponible", "available", "S21-007", "Playera negra recibida por donación, pendiente de revisión.", "Desgaste leve en estampado.", "DON-107", false));

    }

    private Map<String, Object> pieza(String slug, String nombre, String artista, String categoria,
            String talla, String condicion, double precio, double original, String imagen,
            String estado, String color, String sku, String descripcion, String desgaste,
            String donacion, boolean verificado) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("slug", slug); p.put("name", nombre); p.put("artist", artista); p.put("cat", categoria);
        p.put("size", talla); p.put("condition", condicion); p.put("price", precio); p.put("original", original);
        p.put("priceText", String.format(Locale.US, "$%.2f", precio));
        p.put("originalText", String.format(Locale.US, "$%.2f", original));
        p.put("img", imagen); p.put("status", estado); p.put("kind", color); p.put("sku", sku);
        p.put("desc", descripcion); p.put("wear", desgaste); p.put("don", donacion);
        p.put("verificado", verificado);
        p.put("adminStatus", slug.equals("eras") ? "Apartada" : estado);
        return p;
    }

    public List<Map<String, Object>> catalogo() {
        List<Map<String, Object>> visibles = new ArrayList<>();
        for (Map<String, Object> p : productos) {
            if (Boolean.TRUE.equals(p.get("verificado"))) visibles.add(p);
        }
        return visibles;
    }

    public List<Map<String, Object>> todos() { return productos; }

    public Map<String, Object> producto(String id) {
        for (Map<String, Object> p : productos) {
            if (p.get("slug").equals(id)) return p;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pieza no encontrada");
    }

    public List<String> pasos() {
        return List.of("Pendiente", "Pagado", "Procesando", "Enviado", "Entregado");
    }

    public List<Map<String, Object>> pedidos() {
        return List.of(pedido("8492", ""), pedido("8410", ""), pedido("8201", ""), pedido("8115", ""));
    }

    // Las variantes se eligen por parámetros para reutilizar una sola plantilla pedido.html.
    public Map<String, Object> pedido(String id, String variante) {
        String pieza;
        String fecha;
        String estado;
        String envio = "";
        String fechaEnvio = "";
        String entrega = "Pendiente";
        switch (id) {
            case "8492": pieza = "eras"; fecha = "24 oct 2024"; estado = "Pendiente"; break;
            case "8493": pieza = "motomami"; fecha = "24 oct 2024"; estado = "Pendiente"; break;
            case "8494": pieza = "cardigan"; fecha = "24 oct 2024"; estado = "Pendiente"; break;
            case "8410": pieza = "brat"; fecha = "18 oct 2024"; estado = "Enviado";
                envio = "DHL Express · Guía MX-994827103"; fechaEnvio = "20 oct 2024"; break;
            case "8201": pieza = "poster"; fecha = "10 oct 2024"; estado = "Entregado";
                envio = "Estafeta · Guía EST-8201-MX"; fechaEnvio = "11 oct 2024"; entrega = "14 oct 2024"; break;
            case "8115": pieza = "cardigan"; fecha = "8 oct 2024"; estado = "Cancelado"; break;
            default: throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado");
        }
        if (!variante.isBlank() && id.startsWith("849")) {
            if (!pasos().contains(variante) && !variante.equals("Cancelado")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado no válido");
            }
            estado = variante;
            if (estado.equals("Enviado") || estado.equals("Entregado")) {
                envio = "DHL Express · Guía MX-994827492"; fechaEnvio = "25 oct 2024";
                if (estado.equals("Entregado")) entrega = "28 oct 2024";
            }
        }
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("id", id); p.put("fecha", fecha); p.put("estado", estado); p.put("producto", producto(pieza));
        p.put("indice", pasos().indexOf(estado));
        p.put("disponibilidad", estado.equals("Pendiente") ? "Apartada" : estado.equals("Cancelado") ? "Disponible" : "Vendida");
        p.put("kind", switch (estado) {
            case "Pendiente" -> "pending"; case "Pagado" -> "paid"; case "Procesando" -> "processing";
            case "Enviado" -> "sent"; case "Entregado" -> "delivered"; default -> "cancelled";
        });
        p.put("envio", envio); p.put("fechaEnvio", fechaEnvio); p.put("fechaEntrega", entrega);
        return p;
    }
}
