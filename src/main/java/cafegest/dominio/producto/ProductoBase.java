package cafegest.dominio.producto;

import cafegest.dominio.catalogo.ProductoMenu;
import java.math.BigDecimal;
import java.util.Objects;

/** Componente concreto: la selección del menú sin adiciones. */
public final class ProductoBase implements ProductoPedido {
    private final ProductoMenu producto;

    public ProductoBase(ProductoMenu producto) {
        this.producto = Objects.requireNonNull(producto, "El producto base es obligatorio.");
    }

    public String idProducto() {
        return producto.id();
    }

    @Override
    public BigDecimal precio() {
        return producto.precioBase();
    }

    @Override
    public String descripcion() {
        return producto.nombre();
    }
}
