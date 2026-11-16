package cafegest.interfaz;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Formato de importes para pantalla: sin decimales cuando son enteros, con dos cuando no lo son. */
public final class Dinero {
    private Dinero() {
    }

    public static String formatear(BigDecimal valor) {
        BigDecimal limpio = valor.stripTrailingZeros();
        if (limpio.scale() <= 0) {
            return limpio.toPlainString();
        }
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
