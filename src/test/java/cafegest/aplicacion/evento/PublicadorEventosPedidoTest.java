package cafegest.aplicacion.evento;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cafegest.aplicacion.puerto.ObservadorPedido;
import cafegest.dominio.pedido.EstadoPedido;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

class PublicadorEventosPedidoTest {

    private static final PedidoCambioEstado EVENTO =
            new PedidoCambioEstado("P-001", EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION);

    @Test
    void notificaATodosLosObservadoresSuscritos() {
        var recibidos = new ArrayList<PedidoCambioEstado>();
        var publicador = new PublicadorEventosPedido(e -> { });
        publicador.suscribir(recibidos::add);
        publicador.suscribir(recibidos::add);

        publicador.publicar(EVENTO);

        assertEquals(2, recibidos.size());
    }

    @Test
    void unObservadorFallidoNoImpideAvisarALosDemas() {
        var errores = new ArrayList<RuntimeException>();
        var recibidos = new ArrayList<PedidoCambioEstado>();
        var publicador = new PublicadorEventosPedido(errores::add);
        ObservadorPedido fallido = e -> {
            throw new IllegalStateException("vista caída");
        };
        publicador.suscribir(fallido);
        publicador.suscribir(recibidos::add);

        publicador.publicar(EVENTO);

        assertEquals(1, recibidos.size());
        assertEquals(1, errores.size());
    }

    @Test
    void noDuplicaSuscripcionesNiRecibeTrasRetirarse() {
        var recibidos = new ArrayList<PedidoCambioEstado>();
        ObservadorPedido observador = recibidos::add;
        var publicador = new PublicadorEventosPedido(e -> { });
        publicador.suscribir(observador);
        publicador.suscribir(observador);
        publicador.publicar(EVENTO);
        publicador.retirar(observador);
        publicador.publicar(EVENTO);

        assertEquals(1, recibidos.size());
        assertEquals(EstadoPedido.EN_PREPARACION, recibidos.get(0).nuevo());
    }
}
