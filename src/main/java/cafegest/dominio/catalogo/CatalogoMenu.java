package cafegest.dominio.catalogo;

import cafegest.dominio.ReglaNegocioException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Menú en memoria con productos por identificador, conservando el orden de registro. */
public final class CatalogoMenu {
    private final Map<String, ProductoMenu> productos = new LinkedHashMap<>();

    public CatalogoMenu(List<ProductoMenu> iniciales) {
        for (ProductoMenu producto : iniciales) {
            registrar(producto);
        }
    }

    public void registrar(ProductoMenu producto) {
        if (productos.containsKey(producto.id())) {
            throw new ReglaNegocioException("Ya existe un producto con el identificador " + producto.id() + ".");
        }
        productos.put(producto.id(), producto);
    }

    public List<ProductoMenu> consultarMenu() {
        return new ArrayList<>(productos.values());
    }

    public Optional<ProductoMenu> buscar(String id) {
        return Optional.ofNullable(productos.get(id));
    }

    /** Devuelve el producto solo si existe y está disponible para agregarlo a un pedido. */
    public ProductoMenu obtenerDisponible(String id) {
        ProductoMenu producto = buscar(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe un producto con el identificador " + id + "."));
        if (!producto.disponible()) {
            throw new ReglaNegocioException(producto.nombre() + " no está disponible en este momento.");
        }
        return producto;
    }

    public void cambiarDisponibilidad(String id, boolean disponible) {
        ProductoMenu actual = buscar(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe un producto con el identificador " + id + "."));
        productos.put(id, actual.conDisponibilidad(disponible));
    }
}
