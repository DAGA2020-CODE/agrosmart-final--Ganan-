package ec.edu.espe.agrosmart.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_productos_base_04")
public class ProductoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;
    private String nombreProducto;
    private BigDecimal precioUsd;
    private Integer stockKg;
    private String categoria;
    private String correosNotificacion;

    public ProductoEntity() {}
    // Getters y Setters rápidos
    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String n) { this.nombreProducto = n; }
    public BigDecimal getPrecioUsd() { return precioUsd; }
    public void setPrecioUsd(BigDecimal p) { this.precioUsd = p; }
    public Integer getStockKg() { return stockKg; }
    public void setStockKg(Integer s) { this.stockKg = s; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String c) { this.categoria = c; }
    public String getCorreosNotificacion() { return correosNotificacion; }
    public void setCorreosNotificacion(String cn) { this.correosNotificacion = cn; }
}