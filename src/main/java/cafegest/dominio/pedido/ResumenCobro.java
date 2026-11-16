package cafegest.dominio.pedido;

import cafegest.dominio.ReglaNegocioException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Importes definitivos de un pedido confirmado: subtotal, descuento y total no negativo. */
public record ResumenCobro(BigDecimal subtotal, BigDecimal descuento) {

    public ResumenCobro {
        if (subtotal == null || descuento == null) {
            throw new ReglaNegocioException("El resumen de cobro requiere subtotal y descuento.");
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        descuento = descuento.setScale(2, RoundingMode.HALF_UP);
        if (subtotal.signum() < 0 || descuento.signum() < 0) {
            throw new ReglaNegocioException("Los importes del cobro no pueden ser negativos.");
        }
        if (descuento.compareTo(subtotal) > 0) {
            throw new ReglaNegocioException("El descuento no puede superar el subtotal.");
        }
    }

    public BigDecimal total() {
        return subtotal.subtract(descuento);
    }
}
