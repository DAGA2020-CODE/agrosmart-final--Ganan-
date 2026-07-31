package ec.edu.espe.agrosmart.config;

import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ProductoRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(crearProducto("Plátano de Exportación", "2.50", 100, "ventas@agro.com"));
                repository.save(crearProducto("Plátano Orgánico", "3.00", 50, "organicos@agro.com"));
                repository.save(crearProducto("Plátano Cavendish", "1.80", 200, "logistica@agro.com"));
                repository.save(crearProducto("Plátano de Segunda", "0.00", 500, "ofertas@agro.com"));
                repository.save(crearProducto("Plátano Madurado", "1.20", 80, ""));
                System.out.println("✅ Siembra de datos completada.");
            }
        };
    }

    private ProductoEntity crearProducto(String nombre, String precio, Integer stock, String correos) {
        ProductoEntity p = new ProductoEntity();
        p.setNombreProducto(nombre);
        p.setPrecioUsd(new BigDecimal(precio));
        p.setStockKg(stock);
        p.setCategoria("Banano");
        p.setCorreosNotificacion(correos);
        return p;
    }
}