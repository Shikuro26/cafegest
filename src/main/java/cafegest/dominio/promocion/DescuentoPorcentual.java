package cafegest.dominio.promocion;

import cafegest.dominio.ReglaNegocioException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Cálculo común de un porcentaje sobre el subtotal, redondeado a dos decimales con HALF_UP. */
final class DescuentoPorcentual {
    private static final BigDecimal CIEN = new BigDecimal("100");

    private DescuentoPorcentual() {
    }

    static BigDecimal validar(BigDecimal porcentaje) {
        if (porcentaje == null || porcentaje.signum() <= 0 || porcentaje.compareTo(CIEN) > 0) {
            throw new ReglaNegocioException("El porcentaje de descuento debe estar entre 0 y 100.");
        }
        return porcentaje;
    }

    static BigDecimal sobre(BigDecimal subtotal, BigDecimal porcentaje) {
        return subtotal.multiply(porcentaje)
                .divide(CIEN, 2, RoundingMode.HALF_UP);
    }
}
