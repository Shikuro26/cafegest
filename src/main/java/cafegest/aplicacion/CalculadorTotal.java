package cafegest.aplicacion;

import cafegest.dominio.pedido.Pedido;
import cafegest.dominio.pedido.ResumenCobro;
import cafegest.dominio.promocion.ContextoPromocion;
import cafegest.dominio.promocion.Promocion;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Calcula el resumen de cobro. Evalúa las promociones registradas y aplica solo la de mayor descuento;
 * en caso de empate conserva la primera del registro. El descuento queda entre cero y el subtotal.
 */
public final class CalculadorTotal {
    private final List<Promocion> promociones;
    private final Clock reloj;

    public CalculadorTotal(List<Promocion> promociones, Clock reloj) {
        this.promociones = List.copyOf(promociones);
        this.reloj = reloj;
    }

    public ResumenCobro calcular(Pedido pedido) {
        BigDecimal subtotal = pedido.subtotal();
        ContextoPromocion contexto = new ContextoPromocion(subtotal, LocalDateTime.now(reloj));

        BigDecimal mejorDescuento = BigDecimal.ZERO;
        for (Promocion promocion : promociones) {
            BigDecimal descuento = promocion.calcularDescuento(contexto);
            if (descuento.compareTo(mejorDescuento) > 0) {
                mejorDescuento = descuento;
            }
        }
        if (mejorDescuento.compareTo(subtotal) > 0) {
            mejorDescuento = subtotal;
        }
        return new ResumenCobro(subtotal, mejorDescuento);
    }
}
