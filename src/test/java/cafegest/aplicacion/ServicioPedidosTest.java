package cafegest.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafegest.dominio.ReglaNegocioException;
import cafegest.dominio.catalogo.CatalogoMenu;
import cafegest.dominio.catalogo.Categoria;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.dominio.producto.Adicion;
import cafegest.infraestructura.memoria.RepositorioPedidosMemoria;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServicioPedidosTest {

    private final CatalogoMenu catalogo = new CatalogoMenu(List.of(
            new ProductoMenu("cafe", "Café americano", Categoria.BEBIDA, new BigDecimal("5000"), true),
            new ProductoMenu("te", "Té", Categoria.BEBIDA, new BigDecimal("4000"), false)));

    private final ServicioPedidos servicio = new ServicioPedidos(
            new ConfiguradorProducto(catalogo),
            new RepositorioPedidosMemoria(),
            new CalculadorTotal(List.of(), Clock.systemDefaultZone()),
            new cafegest.aplicacion.evento.PublicadorEventosPedido(e -> { }));

    @Test
    void asignaIdentificadoresUnicosEnLaSesion() {
        assertEquals("P-001", servicio.crearPedido());
        assertEquals("P-002", servicio.crearPedido());
        assertEquals(2, servicio.listarPedidos().size());
    }

    @Test
    void agregaYConsultaLineasConAdiciones() {
        String id = servicio.crearPedido();
        servicio.agregarProducto(id, "cafe", List.of(Adicion.SHOT_EXTRA), 2);

        var pedido = servicio.consultarPedido(id);

        assertEquals(1, pedido.lineas().size());
        assertEquals("Café americano + Shot extra", pedido.lineas().get(0).producto().descripcion());
        assertEquals(new BigDecimal("13000.00"), pedido.subtotal());
    }

    @Test
    void productoAgotadoNoAlteraElPedido() {
        String id = servicio.crearPedido();

        assertThrows(ReglaNegocioException.class, () -> servicio.agregarProducto(id, "te", List.of(), 1));

        assertEquals(0, servicio.consultarPedido(id).lineas().size());
    }

    @Test
    void pedidoInexistenteLanzaError() {
        assertThrows(ReglaNegocioException.class, () -> servicio.consultarPedido("P-999"));
    }

    @Test
    void modificaYRetiraProductos() {
        String id = servicio.crearPedido();
        int linea = servicio.agregarProducto(id, "cafe", List.of(), 1);

        servicio.modificarCantidad(id, linea, 4);
        assertEquals(new BigDecimal("20000.00"), servicio.consultarPedido(id).subtotal());

        servicio.retirarProducto(id, linea);
        assertEquals(0, servicio.consultarPedido(id).lineas().size());
    }
}
