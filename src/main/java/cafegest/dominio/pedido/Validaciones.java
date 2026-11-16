package cafegest.dominio.pedido;

import cafegest.dominio.ReglaNegocioException;

/** Validaciones compartidas por las entidades del pedido. */
final class Validaciones {
    private Validaciones() {
    }

    static void cantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser un número entero mayor que cero.");
        }
    }
}
