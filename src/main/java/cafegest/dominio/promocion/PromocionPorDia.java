package cafegest.dominio.promocion;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.Objects;

/** Promoción vigente solo durante un día de la semana. */
public final class PromocionPorDia implements Promocion {
    private final String nombre;
    private final DayOfWeek dia;
    private final BigDecimal porcentaje;

    public PromocionPorDia(String nombre, DayOfWeek dia, BigDecimal porcentaje) {
        this.nombre = Objects.requireNonNull(nombre);
        this.dia = Objects.requireNonNull(dia);
        this.porcentaje = DescuentoPorcentual.validar(porcentaje);
    }

    @Override
    public String nombre() {
        return nombre;
    }

    @Override
    public BigDecimal calcularDescuento(ContextoPromocion contexto) {
        if (contexto.momento().getDayOfWeek() != dia) {
            return BigDecimal.ZERO.setScale(2);
        }
        return DescuentoPorcentual.sobre(contexto.subtotal(), porcentaje);
    }
}
