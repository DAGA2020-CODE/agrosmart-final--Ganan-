package ec.edu.espe.agrosmart.controller;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.service.ProductoService;
import ec.edu.espe.agrosmart.service.PublicidadService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class AgroSmartController {

    private final ProductoService productoService;
    private final PublicidadService publicidadService;

    public AgroSmartController(ProductoService productoService, PublicidadService publicidadService) {
        this.productoService = productoService;
        this.publicidadService = publicidadService;
    }

    // 1. Listar comerciales (Válidos)
    @GetMapping("/productos")
    public Flux<Producto> listarComerciales() {
        return productoService.obtenerProductosComercializables();
    }

    // 2. Buscar por ID
    @GetMapping("/productos/{id}")
    public Mono<Producto> buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id);
    }

    // 3. Generar publicidad vía IA
    @GetMapping("/agrosmart/publicidad")
    public Mono<String> generarPublicidad(
            @RequestParam String producto,
            @RequestParam String audiencia) {
        return publicidadService.obtenerPublicidadReactiva(producto, audiencia);
    }
}