package ec.edu.espe.agrosmart;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import ec.edu.espe.agrosmart.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;

class ProductoServiceTest {

    @Test
    void obtenerProductosComercializables_debeEmitirSoloLosValidos() {
        // Arrange
        ProductoRepository repo = Mockito.mock(ProductoRepository.class);

        ProductoEntity p1 = new ProductoEntity(); // Válido
        p1.setNombreProducto("Valido");
        p1.setPrecioUsd(new BigDecimal("10"));
        p1.setCorreosNotificacion("a@a.com");
        p1.setCategoria("Banano");

        ProductoEntity p2 = new ProductoEntity(); // Inválido (Precio 0)
        p2.setNombreProducto("Invalido");
        p2.setPrecioUsd(BigDecimal.ZERO);
        p2.setCorreosNotificacion("a@a.com");
        p2.setCategoria("Banano");

        when(repo.findAll()).thenReturn(List.of(p1, p2));
        ProductoService service = new ProductoService(repo);

        // Act
        Flux<Producto> resultado = service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(resultado)
                .expectNextMatches(p -> p.getNombre().equals("VALIDO")) // Verifica que aplicó A_MAYUSCULAS
                .verifyComplete();
    }
}