package cafegest.dominio.producto;

/** Decorator concreto: cambio a leche vegetal. */
public final class LecheVegetal extends AdicionDecorator {
    public LecheVegetal(ProductoPedido envuelto) {
        super(envuelto, Adicion.LECHE_VEGETAL);
    }
}
