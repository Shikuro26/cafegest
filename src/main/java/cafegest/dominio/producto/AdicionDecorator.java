package cafegest.dominio.producto;

import java.math.BigDecimal;
import java.util.Objects;

/** Decorator base: envuelve un {@link ProductoPedido} y agrega el costo y la descripción de una adición. */
public abstract class AdicionDecorator implements ProductoPedido {
    private final ProductoPedido envuelto;
    private final Adicion adicion;

    protected AdicionDecorator(ProductoPedido envuelto, Adicion adicion) {
        this.envuelto = Objects.requireNonNull(envuelto, "El producto envuelto es obligatorio.");
        this.adicion = Objects.requireNonNull(adicion, "La adición es obligatoria.");
    }

    @Override
    public BigDecimal precio() {
        return envuelto.precio().add(adicion.precio());
    }

    @Override
    public String descripcion() {
        return envuelto.descripcion() + " + " + adicion.nombre();
    }

    public Adicion adicion() {
        return adicion;
    }
}
