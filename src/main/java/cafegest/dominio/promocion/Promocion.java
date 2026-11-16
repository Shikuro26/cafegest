package cafegest.dominio.promocion;

import java.math.BigDecimal;

/** Estrategia de descuento. Devuelve cero cuando la promoción no está vigente para el contexto. */
public interface Promocion {
    String nombre();

    BigDecimal calcularDescuento(ContextoPromocion contexto);
}
