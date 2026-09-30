package pe.utp.restaurante.controller;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.utp.restaurante.service.RestauranteService;

@Controller
public class RestauranteController {
    private final RestauranteService servicio;

    public RestauranteController(RestauranteService servicio) { this.servicio = servicio; }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("productos", servicio.listarProductos("").size());
        model.addAttribute("proveedores", servicio.listarProveedores("").size());
        model.addAttribute("pedidos", servicio.listarPedidos("").size());
        model.addAttribute("compras", servicio.listarCompras("").size());
        model.addAttribute("ventasTotal", servicio.totalVentas());
        model.addAttribute("alertas", servicio.productosConStockBajo());
        return "inicio";
    }

    @GetMapping("/productos")
    public String productos(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("registros", servicio.listarProductos(q));
        model.addAttribute("q", q);
        return "productos";
    }

    @GetMapping("/productos/{id}")
    public String producto(@PathVariable long id, Model model) {
        model.addAttribute("registro", servicio.consultarProducto(id));
        return "producto-detalle";
    }

    @PostMapping("/productos")
    public String agregarProducto(@RequestParam String nombre, @RequestParam String categoria,
            @RequestParam BigDecimal precio, @RequestParam int stock, @RequestParam int stockMinimo,
            RedirectAttributes avisos) {
        try { servicio.agregarProducto(nombre, categoria, precio, stock, stockMinimo); avisos.addFlashAttribute("exito", "Producto registrado."); }
        catch (IllegalArgumentException e) { avisos.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/productos";
    }

    @PostMapping("/productos/{id}/eliminar")
    public String eliminarProducto(@PathVariable long id, RedirectAttributes avisos) {
        return ejecutar(() -> servicio.eliminarProducto(id), "/productos", avisos);
    }

    @GetMapping("/proveedores")
    public String proveedores(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("registros", servicio.listarProveedores(q));
        model.addAttribute("q", q);
        return "proveedores";
    }

    @GetMapping("/proveedores/{id}")
    public String proveedor(@PathVariable long id, Model model) {
        model.addAttribute("registro", servicio.consultarProveedor(id));
        return "proveedor-detalle";
    }

    @PostMapping("/proveedores")
    public String agregarProveedor(@RequestParam String razonSocial, @RequestParam String ruc,
            @RequestParam String telefono, @RequestParam String correo, RedirectAttributes avisos) {
        try { servicio.agregarProveedor(razonSocial, ruc, telefono, correo); avisos.addFlashAttribute("exito", "Proveedor registrado."); }
        catch (IllegalArgumentException e) { avisos.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/proveedores";
    }

    @PostMapping("/proveedores/{id}/eliminar")
    public String eliminarProveedor(@PathVariable long id, RedirectAttributes avisos) {
        return ejecutar(() -> servicio.eliminarProveedor(id), "/proveedores", avisos);
    }

    @GetMapping("/pedidos")
    public String pedidos(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("registros", servicio.listarPedidos(q));
        model.addAttribute("productos", servicio.listarProductos(""));
        model.addAttribute("q", q);
        return "pedidos";
    }

    @GetMapping("/pedidos/{id}")
    public String pedido(@PathVariable long id, Model model) {
        model.addAttribute("registro", servicio.consultarPedido(id));
        return "pedido-detalle";
    }

    @PostMapping("/pedidos")
    public String agregarPedido(@RequestParam String cliente, @RequestParam long productoId,
            @RequestParam int cantidad, @RequestParam String tipoAtencion, RedirectAttributes avisos) {
        try { servicio.agregarPedido(cliente, productoId, cantidad, tipoAtencion); avisos.addFlashAttribute("exito", "Pedido registrado y stock actualizado."); }
        catch (IllegalArgumentException | NoSuchElementException e) { avisos.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/pedidos";
    }

    @PostMapping("/pedidos/{id}/eliminar")
    public String eliminarPedido(@PathVariable long id, RedirectAttributes avisos) {
        return ejecutar(() -> servicio.eliminarPedido(id), "/pedidos", avisos);
    }

    @GetMapping("/compras")
    public String compras(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("registros", servicio.listarCompras(q));
        model.addAttribute("proveedores", servicio.listarProveedores(""));
        model.addAttribute("productos", servicio.listarProductos(""));
        model.addAttribute("q", q);
        return "compras";
    }

    @GetMapping("/compras/{id}")
    public String compra(@PathVariable long id, Model model) {
        model.addAttribute("registro", servicio.consultarCompra(id));
        return "compra-detalle";
    }

    @PostMapping("/compras")
    public String agregarCompra(@RequestParam long proveedorId, @RequestParam long productoId,
            @RequestParam int cantidad, @RequestParam BigDecimal costoUnitario, RedirectAttributes avisos) {
        try { servicio.agregarCompra(proveedorId, productoId, cantidad, costoUnitario); avisos.addFlashAttribute("exito", "Compra registrada y stock actualizado."); }
        catch (IllegalArgumentException | NoSuchElementException e) { avisos.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/compras";
    }

    @PostMapping("/compras/{id}/eliminar")
    public String eliminarCompra(@PathVariable long id, RedirectAttributes avisos) {
        return ejecutar(() -> servicio.eliminarCompra(id), "/compras", avisos);
    }

    @GetMapping("/graficos/ventas")
    public String graficosVentas(Model model) {
        model.addAttribute("unidades", servicio.unidadesVendidasPorProducto());
        model.addAttribute("ventasDiarias", servicio.ventasPorDia());
        model.addAttribute("total", servicio.totalVentas());
        return "graficos-ventas";
    }

    @GetMapping("/graficos/inventario")
    public String graficosInventario(Model model) {
        model.addAttribute("categorias", servicio.stockPorCategoria());
        model.addAttribute("stock", servicio.stockPorProducto());
        model.addAttribute("alertas", servicio.productosConStockBajo());
        return "graficos-inventario";
    }

    private String ejecutar(Runnable accion, String destino, RedirectAttributes avisos) {
        try { accion.run(); avisos.addFlashAttribute("exito", "Registro eliminado."); }
        catch (IllegalArgumentException | NoSuchElementException e) { avisos.addFlashAttribute("error", e.getMessage()); }
        return "redirect:" + destino;
    }
}
