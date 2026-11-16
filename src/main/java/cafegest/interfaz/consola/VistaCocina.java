package cafegest.interfaz.consola;

import cafegest.aplicacion.evento.PedidoCambioEstado;
import cafegest.aplicacion.puerto.ObservadorPedido;
import cafegest.dominio.pedido.EstadoPedido;
import java.io.PrintWriter;

/** Vista de cocina en consola: avisa cuando un pedido entra en preparación o queda listo para entregar. */
public final class VistaCocina implements ObservadorPedido {
    private final PrintWriter salida;

    public VistaCocina(PrintWriter salida) {
        this.salida = salida;
    }

    @Override
    public void actualizar(PedidoCambioEstado evento) {
        if (evento.nuevo() == EstadoPedido.EN_PREPARACION) {
            salida.println("[Cocina] Preparar pedido " + evento.idPedido());
        } else if (evento.nuevo() == EstadoPedido.ENTREGADO) {
            salida.println("[Cocina] Pedido " + evento.idPedido() + " entregado");
        }
        salida.flush();
    }
}
