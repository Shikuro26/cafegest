package cafegest.dominio.catalogo;

import cafegest.dominio.ReglaNegocioException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Producto del menú. Es inmutable: un cambio de catálogo no altera pedidos ya confirmados. */
public record ProductoMenu(String id, String nombre, Categoria categoria, BigDecimal precioBase, boolean disponible) {

    public ProductoMenu {
        if (id == null || id.isBlank()) {
            throw new ReglaNegocioException("El producto debe tener un identificador.");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El producto debe tener un nombre.");
        }
        if (categoria == null) {
            throw new ReglaNegocioException("El producto debe tener una categoría.");
        }
        if (precioBase == null || precioBase.signum() < 0) {
            throw new ReglaNegocioException("El precio del producto no puede ser negativo.");
        }
        precioBase = precioBase.setScale(2, RoundingMode.HALF_UP);
    }

    public ProductoMenu conDisponibilidad(boolean nuevaDisponibilidad) {
        return new ProductoMenu(id, nombre, categoria, precioBase, nuevaDisponibilidad);
    }
}
