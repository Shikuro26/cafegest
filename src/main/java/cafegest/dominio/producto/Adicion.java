package cafegest.dominio.producto;

import cafegest.dominio.catalogo.Categoria;
import java.math.BigDecimal;
import java.util.Set;

/** Adiciones disponibles, con su precio y las categorías donde son compatibles. Configuración interna. */
public enum Adicion {
    SHOT_EXTRA("Shot extra", new BigDecimal("1500.00"), Set.of(Categoria.BEBIDA)),
    LECHE_VEGETAL("Leche vegetal", new BigDecimal("1000.00"), Set.of(Categoria.BEBIDA));

    private final String nombre;
    private final BigDecimal precio;
    private final Set<Categoria> categoriasCompatibles;

    Adicion(String nombre, BigDecimal precio, Set<Categoria> categoriasCompatibles) {
        this.nombre = nombre;
        this.precio = precio;
        this.categoriasCompatibles = categoriasCompatibles;
    }

    public String nombre() {
        return nombre;
    }

    public BigDecimal precio() {
        return precio;
    }

    public boolean esCompatibleCon(Categoria categoria) {
        return categoriasCompatibles.contains(categoria);
    }

    /** Envuelve el producto con esta adición. */
    public ProductoPedido aplicar(ProductoPedido producto) {
        return switch (this) {
            case SHOT_EXTRA -> new ShotExtra(producto);
            case LECHE_VEGETAL -> new LecheVegetal(producto);
        };
    }
}
