package cafegest.interfaz.consola;

import cafegest.aplicacion.evento.PedidoCambioEstado;
import cafegest.aplicacion.puerto.ObservadorPedido;
import java.io.PrintWriter;

/** Vista de caja en consola: muestra los cambios de estado de cada pedido. */
public final class VistaCaja implements ObservadorPedido {
    private final PrintWriter salida;

    public VistaCaja(PrintWriter salida) {
        this.salida = salida;
    }

    @Override
    public void actualizar(PedidoCambioEstado evento) {
        salida.println("[Caja] Pedido " + evento.idPedido() + ": " + evento.anterior() + " -> " + evento.nuevo());
        salida.flush();
    }
}
