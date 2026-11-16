package cafegest.dominio.producto;

import java.math.BigDecimal;

/** Componente del patrón Decorator: una unidad de producto con su configuración. */
public interface ProductoPedido {
    BigDecimal precio();

    String descripcion();
}
