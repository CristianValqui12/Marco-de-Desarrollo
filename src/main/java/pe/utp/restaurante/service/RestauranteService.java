package pe.utp.restaurante.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import pe.utp.restaurante.model.Compra;
import pe.utp.restaurante.model.Pedido;
import pe.utp.restaurante.model.Producto;
import pe.utp.restaurante.model.Proveedor;

/** Almacén en memoria. Todos los cambios de inventario se hacen en una operación sincronizada. */
@Service
public class RestauranteService {
    private final Map<Long, Producto> productos = new LinkedHashMap<>();
    private final Map<Long, Proveedor> proveedores = new LinkedHashMap<>();
    private final Map<Long, Pedido> pedidos = new LinkedHashMap<>();
    private final Map<Long, Compra> compras = new LinkedHashMap<>();
    private long siguienteProducto = 1, siguienteProveedor = 1, siguientePedido = 1, siguienteCompra = 1;

    public RestauranteService() {
        Producto hamburguesa = agregarProducto("Hamburguesa clásica", "Hamburguesas", new BigDecimal("18.90"), 45, 10);
        agregarProducto("Combo familiar", "Combos", new BigDecimal("49.90"), 18, 5);
        agregarProducto("Papas fritas", "Acompañamientos", new BigDecimal("8.50"), 36, 8);
        agregarProducto("Gaseosa personal", "Bebidas", new BigDecimal("5.00"), 50, 12);
        Proveedor proveedor = agregarProveedor("Distribuidora Lima Foods", "20601234567", "987654321", "ventas@limafoods.pe");
        agregarProveedor("Insumos del Sur", "20598765432", "912345678", "contacto@insumosdelsur.pe");
        agregarPedido("Ana Torres", hamburguesa.id(), 2, "Salón");
        agregarCompra(proveedor.id(), hamburguesa.id(), 12, new BigDecimal("11.50"));
    }

    private static String obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Ingrese " + campo + ".");
        return valor.trim();
    }

    private static String buscarTexto(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase();
    }

    public synchronized Producto agregarProducto(String nombre, String categoria, BigDecimal precio, int stock, int minimo) {
        nombre = obligatorio(nombre, "el nombre del producto");
        categoria = obligatorio(categoria, "la categoría");
        if (precio == null || precio.signum() <= 0) throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        if (stock < 0 || minimo < 0) throw new IllegalArgumentException("El stock y el mínimo no pueden ser negativos.");
        Producto p = new Producto(siguienteProducto++, nombre, categoria, precio, stock, minimo);
        productos.put(p.id(), p);
        return p;
    }

    public synchronized List<Producto> listarProductos(String busqueda) {
        String q = buscarTexto(busqueda);
        return productos.values().stream().filter(p -> (p.nombre() + " " + p.categoria()).toLowerCase().contains(q)).toList();
    }

    public synchronized Producto consultarProducto(long id) {
        Producto p = productos.get(id);
        if (p == null) throw new NoSuchElementException("Producto no encontrado.");
        return p;
    }

    public synchronized void eliminarProducto(long id) {
        consultarProducto(id);
        if (pedidos.values().stream().anyMatch(p -> p.productoId() == id)
                || compras.values().stream().anyMatch(c -> c.productoId() == id)) {
            throw new IllegalArgumentException("El producto tiene pedidos o compras. Elimine esos registros primero.");
        }
        productos.remove(id);
    }

    public synchronized Proveedor agregarProveedor(String razonSocial, String ruc, String telefono, String correo) {
        razonSocial = obligatorio(razonSocial, "la razón social");
        String rucValidado = obligatorio(ruc, "el RUC");
        if (!rucValidado.matches("\\d{11}")) throw new IllegalArgumentException("El RUC debe tener 11 dígitos.");
        if (proveedores.values().stream().anyMatch(p -> p.ruc().equals(rucValidado))) throw new IllegalArgumentException("Ese RUC ya existe.");
        Proveedor p = new Proveedor(siguienteProveedor++, razonSocial, rucValidado,
                obligatorio(telefono, "el teléfono"), obligatorio(correo, "el correo"));
        proveedores.put(p.id(), p);
        return p;
    }

    public synchronized List<Proveedor> listarProveedores(String busqueda) {
        String q = buscarTexto(busqueda);
        return proveedores.values().stream().filter(p -> (p.razonSocial() + " " + p.ruc()).toLowerCase().contains(q)).toList();
    }

    public synchronized Proveedor consultarProveedor(long id) {
        Proveedor p = proveedores.get(id);
        if (p == null) throw new NoSuchElementException("Proveedor no encontrado.");
        return p;
    }

    public synchronized void eliminarProveedor(long id) {
        consultarProveedor(id);
        if (compras.values().stream().anyMatch(c -> c.proveedorId() == id))
            throw new IllegalArgumentException("El proveedor tiene compras. Elimine esas compras primero.");
        proveedores.remove(id);
    }

    public synchronized Pedido agregarPedido(String cliente, long productoId, int cantidad, String tipoAtencion) {
        cliente = obligatorio(cliente, "el cliente");
        tipoAtencion = obligatorio(tipoAtencion, "el tipo de atención");
        if (!List.of("Salón", "Recojo", "Delivery").contains(tipoAtencion))
            throw new IllegalArgumentException("Tipo de atención no válido.");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        Producto producto = consultarProducto(productoId);
        if (cantidad > producto.stock()) throw new IllegalArgumentException("No hay stock suficiente para el pedido.");
        Pedido pedido = new Pedido(siguientePedido++, cliente, productoId, producto.nombre(), cantidad,
                producto.precio(), producto.precio().multiply(BigDecimal.valueOf(cantidad)), tipoAtencion, LocalDateTime.now());
        pedidos.put(pedido.id(), pedido);
        cambiarStock(producto, -cantidad);
        return pedido;
    }

    public synchronized List<Pedido> listarPedidos(String busqueda) {
        String q = buscarTexto(busqueda);
        return pedidos.values().stream().filter(p -> (p.cliente() + " " + p.productoNombre() + " " + p.id()).toLowerCase().contains(q))
                .sorted(Comparator.comparing(Pedido::fecha).reversed()).toList();
    }

    public synchronized Pedido consultarPedido(long id) {
        Pedido p = pedidos.get(id);
        if (p == null) throw new NoSuchElementException("Pedido no encontrado.");
        return p;
    }

    public synchronized void eliminarPedido(long id) {
        Pedido p = consultarPedido(id);
        Producto producto = consultarProducto(p.productoId());
        cambiarStock(producto, p.cantidad());
        pedidos.remove(id);
    }

    public synchronized Compra agregarCompra(long proveedorId, long productoId, int cantidad, BigDecimal costoUnitario) {
        Proveedor proveedor = consultarProveedor(proveedorId);
        Producto producto = consultarProducto(productoId);
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        if (costoUnitario == null || costoUnitario.signum() <= 0) throw new IllegalArgumentException("El costo debe ser mayor que cero.");
        Compra compra = new Compra(siguienteCompra++, proveedorId, proveedor.razonSocial(), productoId,
                producto.nombre(), cantidad, costoUnitario, costoUnitario.multiply(BigDecimal.valueOf(cantidad)), LocalDateTime.now());
        compras.put(compra.id(), compra);
        cambiarStock(producto, cantidad);
        return compra;
    }

    public synchronized List<Compra> listarCompras(String busqueda) {
        String q = buscarTexto(busqueda);
        return compras.values().stream().filter(c -> (c.proveedorNombre() + " " + c.productoNombre() + " " + c.id()).toLowerCase().contains(q))
                .sorted(Comparator.comparing(Compra::fecha).reversed()).toList();
    }

    public synchronized Compra consultarCompra(long id) {
        Compra c = compras.get(id);
        if (c == null) throw new NoSuchElementException("Compra no encontrada.");
        return c;
    }

    public synchronized void eliminarCompra(long id) {
        Compra c = consultarCompra(id);
        Producto producto = consultarProducto(c.productoId());
        if (producto.stock() < c.cantidad())
            throw new IllegalArgumentException("No se puede eliminar la compra: parte de ese stock ya se vendió.");
        cambiarStock(producto, -c.cantidad());
        compras.remove(id);
    }

    private void cambiarStock(Producto p, int cambio) {
        productos.put(p.id(), new Producto(p.id(), p.nombre(), p.categoria(), p.precio(), p.stock() + cambio, p.stockMinimo()));
    }

    public synchronized BigDecimal totalVentas() {
        return pedidos.values().stream().map(Pedido::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public synchronized BigDecimal totalCompras() {
        return compras.values().stream().map(Compra::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public synchronized Map<String, Integer> unidadesVendidasPorProducto() {
        Map<String, Integer> datos = new LinkedHashMap<>();
        productos.values().forEach(p -> datos.put(p.nombre(), 0));
        pedidos.values().forEach(p -> datos.merge(p.productoNombre(), p.cantidad(), Integer::sum));
        return datos;
    }

    public synchronized Map<String, BigDecimal> ventasPorDia() {
        Map<String, BigDecimal> datos = new LinkedHashMap<>();
        pedidos.values().stream().sorted(Comparator.comparing(Pedido::fecha)).forEach(p ->
                datos.merge(p.fecha().toLocalDate().toString(), p.total(), BigDecimal::add));
        return datos;
    }

    public synchronized Map<String, Integer> stockPorCategoria() {
        return productos.values().stream().collect(Collectors.groupingBy(Producto::categoria,
                LinkedHashMap::new, Collectors.summingInt(Producto::stock)));
    }

    public synchronized Map<String, Integer> stockPorProducto() {
        Map<String, Integer> datos = new LinkedHashMap<>();
        productos.values().forEach(p -> datos.put(p.nombre(), p.stock()));
        return datos;
    }

    public synchronized long productosConStockBajo() {
        return productos.values().stream().filter(Producto::stockBajo).count();
    }
}
