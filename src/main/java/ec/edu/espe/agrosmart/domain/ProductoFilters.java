package ec.edu.espe.agrosmart.domain;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ProductoFilters {

    // Predicate: precioUsd > 0 Y correos no vacíos
    public static final Predicate<Producto> IS_VALID = p ->
            p.getPrecioUsd().compareTo(java.math.BigDecimal.ZERO) > 0 && !p.getCorreosNotificacion().isEmpty();

    // Consumer: Imprime log
    public static final Consumer<Producto> LOG_PRODUCTO = p ->
            System.out.println("Procesando Producto: ID=" + p.getId() + " - Nombre=" + p.getNombre());

    // Function: Nombre a MAYÚSCULAS (Crea una instancia nueva, no muta la anterior)
    public static final Function<Producto, Producto> A_MAYUSCULAS = p ->
            new Producto(p.getId(), p.getNombre().toUpperCase(), p.getCategoria(), p.getPrecioUsd(), p.getCorreosNotificacion());
}