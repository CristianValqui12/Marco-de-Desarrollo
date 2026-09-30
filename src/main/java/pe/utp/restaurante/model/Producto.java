package pe.utp.restaurante.model;

import java.math.BigDecimal;

public record Producto(long id, String nombre, String categoria, BigDecimal precio,
                       int stock, int stockMinimo) {
    public boolean stockBajo() { return stock <= stockMinimo; }
}
