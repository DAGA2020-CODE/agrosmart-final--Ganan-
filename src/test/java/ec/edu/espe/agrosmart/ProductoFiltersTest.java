package ec.edu.espe.agrosmart;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.domain.ProductoFilters;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProductoFiltersTest {

    @Test
    void isValid_conPrecioMayorACeroYCorreos_debeRetornarTrue() {
        Producto p = new Producto(1L, "Test", "Banano", new BigDecimal("1.0"), List.of("a@a.com"));
        assertTrue(ProductoFilters.IS_VALID.test(p));
    }

    @Test
    void isValid_conPrecioCero_debeRetornarFalse() {
        Producto p = new Producto(1L, "Test", "Banano", BigDecimal.ZERO, List.of("a@a.com"));
        assertFalse(ProductoFilters.IS_VALID.test(p));
    }

    @Test
    void isValid_conListaCorreosVacia_debeRetornarFalse() {
        Producto p = new Producto(1L, "Test", "Banano", new BigDecimal("1.0"), List.of());
        assertFalse(ProductoFilters.IS_VALID.test(p));
    }
}