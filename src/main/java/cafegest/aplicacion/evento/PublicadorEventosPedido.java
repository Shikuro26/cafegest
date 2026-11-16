package cafegest.aplicacion.evento;

import cafegest.aplicacion.puerto.ObservadorPedido;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Notifica cambios de estado a los observadores suscritos. Un observador que falla no impide
 * avisar a los demás: el error se entrega al manejador de errores.
 */
public final class PublicadorEventosPedido {
    private final List<ObservadorPedido> observadores = new ArrayList<>();
    private final Consumer<RuntimeException> manejadorErrores;

    public PublicadorEventosPedido(Consumer<RuntimeException> manejadorErrores) {
        this.manejadorErrores = manejadorErrores;
    }

    public void suscribir(ObservadorPedido observador) {
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public void retirar(ObservadorPedido observador) {
        observadores.remove(observador);
    }

    public void publicar(PedidoCambioEstado evento) {
        for (ObservadorPedido observador : List.copyOf(observadores)) {
            try {
                observador.actualizar(evento);
            } catch (RuntimeException error) {
                manejadorErrores.accept(error);
            }
        }
    }
}
