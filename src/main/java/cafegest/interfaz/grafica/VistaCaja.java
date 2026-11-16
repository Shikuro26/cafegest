package cafegest.interfaz.grafica;

import cafegest.aplicacion.evento.PedidoCambioEstado;

/** Vista gráfica de caja: muestra todos los cambios de estado. */
public final class VistaCaja extends VistaPedidosSwing {

    public VistaCaja() {
        super("Caja");
    }

    @Override
    protected String mensaje(PedidoCambioEstado evento) {
        return "Pedido " + evento.idPedido() + ": " + evento.anterior() + " -> " + evento.nuevo();
    }
}
