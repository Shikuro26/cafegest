package cafegest.dominio.producto;

/** Decorator concreto: shot adicional de café. */
public final class ShotExtra extends AdicionDecorator {
    public ShotExtra(ProductoPedido envuelto) {
        super(envuelto, Adicion.SHOT_EXTRA);
    }
}
