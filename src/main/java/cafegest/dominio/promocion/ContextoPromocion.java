package cafegest.dominio.promocion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Datos que una promoción necesita para decidir: subtotal y momento local de confirmación. */
public record ContextoPromocion(BigDecimal subtotal, LocalDateTime momento) {
}
