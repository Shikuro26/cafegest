package cafegest.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafegest.aplicacion.evento.PedidoCambioEstado;
import cafegest.aplicacion.evento.PublicadorEventosPedido;
import cafegest.dominio.ReglaNegocioException;
import cafegest.dominio.catalogo.CatalogoMenu;
import cafegest.dominio.catalogo.Categoria;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.dominio.pedido.EstadoPedido;
import cafegest.infraestructura.memoria.RepositorioPedidosMemoria;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServicioPedidosEstadoTest {

    private final ArrayList<PedidoCambioEstado> eventos = new ArrayList<>();
    private final ServicioPedidos servicio;

    ServicioPedidosEstadoTest() {
        var catalogo = new CatalogoMenu(List.of(
                new ProductoMenu("cafe", "Café americano", Categoria.BEBIDA, new BigDecimal("5000"), true)));
        var publicador = new PublicadorEventosPedido(e -> { });
        publicador.suscribir(eventos::add);
        servicio = new ServicioPedidos(new ConfiguradorProducto(catalogo), new RepositorioPedidosMemoria(),
                new CalculadorTotal(List.of(), Clock.systemDefaultZone()), publicador);
    }

    private String pedidoConfirmado() {
        String id = servicio.crearPedido();
        servicio.agregarProducto(id, "cafe", List.of(), 1);
        servicio.confirmarPedido(id);
        return id;
    }

    @Test
    void transicionValidaPublicaUnEvento() {
        String id = pedidoConfirmado();

        servicio.cambiarEstado(id, EstadoPedido.EN_PREPARACION);

        assertEquals(List.of(new PedidoCambioEstado(id, EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION)), eventos);
    }

    @Test
    void transicionInvalidaNoCambiaElPedidoNiPublica() {
        String id = pedidoConfirmado();

        assertThrows(ReglaNegocioException.class, () -> servicio.cambiarEstado(id, EstadoPedido.ENTREGADO));

        assertEquals(EstadoPedido.RECIBIDO, servicio.consultarPedido(id).estado());
        assertEquals(0, eventos.size());
    }

    @Test
    void noSePreparaUnPedidoSinConfirmar() {
        String id = servicio.crearPedido();
        servicio.agregarProducto(id, "cafe", List.of(), 1);

        assertThrows(ReglaNegocioException.class, () -> servicio.cambiarEstado(id, EstadoPedido.EN_PREPARACION));
        assertEquals(0, eventos.size());
    }

    @Test
    void noHayRetrocesos() {
        String id = pedidoConfirmado();
        servicio.cambiarEstado(id, EstadoPedido.EN_PREPARACION);

        assertThrows(ReglaNegocioException.class, () -> servicio.cambiarEstado(id, EstadoPedido.RECIBIDO));
        assertEquals(EstadoPedido.EN_PREPARACION, servicio.consultarPedido(id).estado());
    }
}
