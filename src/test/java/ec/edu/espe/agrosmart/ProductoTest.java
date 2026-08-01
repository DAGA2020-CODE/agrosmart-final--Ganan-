package ec.edu.espe.agrosmart;

import ec.edu.espe.agrosmart.domain.Producto;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void getCorreosNotificacion_alMutarLaListaOriginal_noDebeAfectarAlProducto() {
        // Arrange (Preparar)
        List<String> correos = new ArrayList<>();
        correos.add("ventas@agrosmart.ec");
        Producto producto = new Producto(1L, "Platano", "Banano", new BigDecimal("1.50"), correos);

        // Act (Actuar)
        correos.add("intruso@mail.com");

        // Assert (Verificar copia defensiva de entrada)
        assertEquals(1, producto.getCorreosNotificacion().size());
        assertNotSame(correos, producto.getCorreosNotificacion());
    }

    @Test
    void getCorreosNotificacion_alIntentarMutarLaListaDevuelta_debeLanzarExcepcion() {
        // Arrange
        Producto producto = new Producto(1L, "Platano", "Banano", new BigDecimal("1.50"), List.of("mail@test.com"));

        // Act & Assert (Verificar copia defensiva de salida)
        List<String> correosDelProducto = producto.getCorreosNotificacion();
        assertThrows(UnsupportedOperationException.class, () -> correosDelProducto.add("error@test.com"));
    }
}