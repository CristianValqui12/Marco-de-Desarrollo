# Avance 2: sistema de gestión de restaurante

Proyecto Spring Boot basado en el primer avance «Sistema de Gestión de Compras y Pedidos para un Restaurante de Comida Rápida» y en la estructura del laboratorio Semana07.

## Requisitos

- Java 21.
- Conexión a internet al iniciar por primera vez para descargar dependencias Maven. Bootstrap y Chart.js también se cargan desde CDN en el navegador.

## Ejecución

En Windows, abre una terminal en esta carpeta y ejecuta:

```powershell
.\mvnw.cmd spring-boot:run
```

El ZIP también incluye un JAR ya compilado. Puedes abrir la aplicación directamente con `iniciar.cmd` o con:

```powershell
java -jar ejecutable\restaurante-avance2.jar
```

Después visita <http://localhost:8080/>. Para compilar y ejecutar pruebas: `.\mvnw.cmd test`.

## Funcionalidad

- Cuatro clases modelo: `Producto`, `Proveedor`, `Pedido` y `Compra`.
- `RestauranteService` permite agregar, listar, consultar por ID, buscar y eliminar registros de las cuatro clases.
- Formularios y listados Thymeleaf para productos, proveedores, pedidos y compras; páginas de detalle para consultar cada registro.
- Dos páginas de gráficos: ventas (barras y línea) e inventario (barras y circular).
- Menú responsivo con Bootstrap.
- Interfaz renovada con Bootstrap 5, Bootstrap Icons, tarjetas de indicadores, tablas y formularios adaptables.
- Datos de ejemplo al iniciar. Todo se conserva en memoria: se pierde al detener la aplicación.
- Los pedidos descuentan stock y las compras lo aumentan. Al eliminar un pedido se devuelve su stock; al eliminar una compra se retira su stock si aún está disponible.

## Organización

`src/main/java/pe/utp/restaurante/model`: modelos.

`src/main/java/pe/utp/restaurante/service`: operaciones y almacenamiento en memoria.

`src/main/java/pe/utp/restaurante/controller`: rutas web y formularios.

`src/main/resources/templates`: vistas Thymeleaf.
