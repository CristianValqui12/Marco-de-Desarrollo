package pe.utp.restaurante;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import pe.utp.restaurante.model.Compra;
import pe.utp.restaurante.model.Pedido;
import pe.utp.restaurante.model.Producto;
import pe.utp.restaurante.model.Proveedor;
import pe.utp.restaurante.service.RestauranteService;

class RestauranteServiceTest {
    @Test
    void pedidoDescuentaStockYEliminarloLoDevuelve() {
        RestauranteService servicio = new RestauranteService();
        Producto producto = servicio.agregarProducto("Prueba", "Otros", new BigDecimal("10.00"), 5, 1);
        Pedido pedido = servicio.agregarPedido("Cliente", producto.id(), 2, "Recojo");
        assertEquals(3, servicio.consultarProducto(producto.id()).stock());
        assertEquals(new BigDecimal("20.00"), pedido.total());
        assertThrows(IllegalArgumentException.class, () -> servicio.agregarPedido("Otro", producto.id(), 4, "Salón"));
        servicio.eliminarPedido(pedido.id());
        assertEquals(5, servicio.consultarProducto(producto.id()).stock());
    }

    @Test
    void compraAumentaStockYBusquedaEncuentraRegistros() {
        RestauranteService servicio = new RestauranteService();
        Producto producto = servicio.agregarProducto("Salsa especial", "Otros", new BigDecimal("4.00"), 2, 1);
        Proveedor proveedor = servicio.agregarProveedor("Proveedor Test", "20999999999", "900000000", "test@example.com");
        Compra compra = servicio.agregarCompra(proveedor.id(), producto.id(), 7, new BigDecimal("2.50"));
        assertEquals(9, servicio.consultarProducto(producto.id()).stock());
        assertEquals(1, servicio.listarCompras("Proveedor Test").size());
        assertEquals(1, servicio.listarProductos("salsa").size());
        assertEquals(1, servicio.listarProveedores("20999999999").size());
        servicio.eliminarCompra(compra.id());
        assertEquals(2, servicio.consultarProducto(producto.id()).stock());
    }
}
