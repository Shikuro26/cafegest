package cafegest.interfaz.grafica;

import cafegest.dominio.pedido.EstadoPedido;
import cafegest.dominio.pedido.Pedido;
import java.util.Optional;

/**
 * Decide qué acciones y pasos están disponibles según el estado del pedido seleccionado.
 * Solo presenta reglas de la interfaz; las validaciones definitivas siguen en los servicios.
 */
public final class EstadoAcciones {
    private static final int TOTAL_PASOS = 5;

    private EstadoAcciones() {
    }

    /** Si la acción puede usarse y, en caso contrario, el motivo que se muestra al pasar el cursor. */
    public record Estado(boolean habilitado, String motivo) {
        static Estado si() {
            return new Estado(true, null);
        }

        static Estado no(String motivo) {
            return new Estado(false, motivo);
        }
    }

    public static Estado agregarProducto(Optional<Pedido> seleccion) {
        if (seleccion.isEmpty()) {
            return Estado.no("Cree o seleccione un pedido");
        }
        Pedido pedido = seleccion.get();
        if (pedido.confirmado()) {
            return Estado.no("El pedido ya fue confirmado");
        }
        return Estado.si();
    }

    public static Estado confirmar(Optional<Pedido> seleccion) {
        if (seleccion.isEmpty()) {
            return Estado.no("Cree o seleccione un pedido");
        }
        Pedido pedido = seleccion.get();
        if (pedido.confirmado()) {
            return Estado.no("El pedido ya está confirmado");
        }
        if (pedido.lineas().isEmpty()) {
            return Estado.no("Agregue al menos un producto");
        }
        return Estado.si();
    }

    public static Estado pasarAPreparacion(Optional<Pedido> seleccion) {
        if (seleccion.isEmpty()) {
            return Estado.no("Cree o seleccione un pedido");
        }
        Pedido pedido = seleccion.get();
        if (!pedido.confirmado()) {
            return Estado.no("Confirme el pedido primero");
        }
        if (pedido.estado() != EstadoPedido.RECIBIDO) {
            return Estado.no("El pedido ya está en preparación");
        }
        return Estado.si();
    }

    public static Estado marcarEntregado(Optional<Pedido> seleccion) {
        if (seleccion.isEmpty()) {
            return Estado.no("Cree o seleccione un pedido");
        }
        Pedido pedido = seleccion.get();
        return switch (pedido.estado()) {
            case RECIBIDO -> Estado.no("Pase el pedido a preparación primero");
            case ENTREGADO -> Estado.no("El pedido ya fue entregado");
            case EN_PREPARACION -> Estado.si();
        };
    }

    /**
     * Número del primer paso pendiente, de 1 a 5 (crear, agregar, confirmar, preparar, entregar).
     * Devuelve 6 cuando todos los pasos están completos.
     */
    public static int pasoActual(Optional<Pedido> seleccion) {
        if (seleccion.isEmpty()) {
            return 1;
        }
        Pedido pedido = seleccion.get();
        boolean[] completados = {
            true,
            !pedido.lineas().isEmpty(),
            pedido.confirmado(),
            pedido.estado() != EstadoPedido.RECIBIDO,
            pedido.estado() == EstadoPedido.ENTREGADO
        };
        for (int i = 0; i < TOTAL_PASOS; i++) {
            if (!completados[i]) {
                return i + 1;
            }
        }
        return TOTAL_PASOS + 1;
    }
}
