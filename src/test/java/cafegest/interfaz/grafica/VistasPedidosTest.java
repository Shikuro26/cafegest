package cafegest.interfaz.grafica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cafegest.aplicacion.evento.PedidoCambioEstado;
import cafegest.dominio.pedido.EstadoPedido;
import org.junit.jupiter.api.Test;

class VistasPedidosTest {

    @Test
    void cajaMuestraCualquierCambioDeEstado() {
        var evento = new PedidoCambioEstado("P-001", EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION);

        assertEquals("Pedido P-001: RECIBIDO -> EN_PREPARACION", new VistaCaja().mensaje(evento));
    }

    @Test
    void cocinaSoloMuestraPreparacionYEntrega() {
        var cocina = new VistaCocina();

        assertEquals("Preparar pedido P-001",
                cocina.mensaje(new PedidoCambioEstado("P-001", EstadoPedido.RECIBIDO, EstadoPedido.EN_PREPARACION)));
        assertEquals("Pedido P-001 entregado",
                cocina.mensaje(new PedidoCambioEstado("P-001", EstadoPedido.EN_PREPARACION, EstadoPedido.ENTREGADO)));
    }
}
