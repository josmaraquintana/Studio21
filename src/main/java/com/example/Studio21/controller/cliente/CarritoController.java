package com.example.Studio21.controller.cliente;

import com.example.Studio21.dto.AgregarCarritoRequest;
import com.example.Studio21.dto.CarritoResponse;
import com.example.Studio21.service.CarritoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {
    private final CarritoService carrito;

    public CarritoController(CarritoService carrito) {
        this.carrito = carrito;
    }

    @GetMapping
    public CarritoResponse obtener() {
        return carrito.obtener();
    }

    @PostMapping("/items")
    public CarritoResponse agregar(@Valid @RequestBody AgregarCarritoRequest datos) {
        return carrito.agregar(datos.productoId());
    }

    @DeleteMapping("/items/{productoId}")
    public CarritoResponse quitar(@PathVariable @Positive Long productoId) {
        return carrito.quitar(productoId);
    }
}
