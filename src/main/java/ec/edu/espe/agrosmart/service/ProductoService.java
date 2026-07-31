package ec.edu.espe.agrosmart.service;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.domain.ProductoFilters;
import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.mapper.ProductoMapper;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    // Producto genérico solicitado en el Anexo D para defaultIfEmpty
    private static final Producto PRODUCTO_GENERICO = new Producto(
            0L, "GENERICO", "NINGUNA", BigDecimal.ZERO, new ArrayList<>()
    );

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public Flux<Producto> obtenerProductosComercializables() {
        // fromCallable: Envuelve la llamada bloqueante de JPA para diferir su ejecución
        return Mono.fromCallable(repository::findAll)
                // subscribeOn: Mueve el bloqueo fuera del bucle de eventos (Event Loop) hacia un pool elástico
                .subscribeOn(Schedulers.boundedElastic())
                // flatMapMany: Convierte el Mono<List> que devuelve el repo en un flujo Flux individual
                .flatMapMany(Flux::fromIterable)
                // map: Transforma la Entidad a Modelo de Dominio y aplica lógica de mayúsculas
                .map(ProductoMapper::toDominio)
                .map(ProductoFilters.A_MAYUSCULAS)
                // filter: Descarta productos según la regla de negocio (precio > 0 y correos no vacíos)
                .filter(ProductoFilters.IS_VALID)
                // doOnNext: Permite trazabilidad (logueo) sin alterar los datos del flujo
                .doOnNext(ProductoFilters.LOG_PRODUCTO)
                // defaultIfEmpty: Si el filtro descartó todo, emite el producto genérico
                .defaultIfEmpty(PRODUCTO_GENERICO);
    }

    public Mono<Producto> buscarPorId(Long id) {
        return Mono.fromCallable(() -> repository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty) // Convierte el Optional de JPA en un Mono vacío si no existe
                .map(ProductoMapper::toDominio)
                // switchIfEmpty: Lanza la excepción dentro del contexto reactivo si el Mono quedó vacío
                .switchIfEmpty(Mono.error(new ProductoNoEncontradoException(id)));
    }
}