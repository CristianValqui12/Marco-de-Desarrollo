package pe.utp.restaurante.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Pedido(long id, String cliente, long productoId, String productoNombre,
                     int cantidad, BigDecimal precioUnitario, BigDecimal total,
                     String tipoAtencion, LocalDateTime fecha) { }
