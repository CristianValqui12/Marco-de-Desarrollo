package pe.utp.restaurante.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Compra(long id, long proveedorId, String proveedorNombre, long productoId,
                     String productoNombre, int cantidad, BigDecimal costoUnitario,
                     BigDecimal total, LocalDateTime fecha) { }
