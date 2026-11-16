package cafegest.interfaz.grafica;

import cafegest.aplicacion.evento.PedidoCambioEstado;
import cafegest.dominio.pedido.EstadoPedido;

/** Vista gráfica de cocina: muestra los pedidos que entran en preparación o se entregan. */
public final class VistaCocina extends VistaPedidosSwing {

    public VistaCocina() {
        super("Cocina");
    }

    @Override
    protected String mensaje(PedidoCambioEstado evento) {
        if (evento.nuevo() == EstadoPedido.EN_PREPARACION) {
            return "Preparar pedido " + evento.idPedido();
        }
        if (evento.nuevo() == EstadoPedido.ENTREGADO) {
            return "Pedido " + evento.idPedido() + " entregado";
        }
        return null;
    }
}
