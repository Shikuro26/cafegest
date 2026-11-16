package cafegest.dominio.promocion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafegest.dominio.ReglaNegocioException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class PromocionesTest {

    private static final BigDecimal DIEZ = new BigDecimal("10");
    private static final BigDecimal SUBTOTAL = new BigDecimal("10000.00");

    private static ContextoPromocion en(LocalDateTime momento) {
        return new ContextoPromocion(SUBTOTAL, momento);
    }

    @Test
    void porDiaAplicaSoloElDiaConfigurado() {
        var promocion = new PromocionPorDia("Martes", DayOfWeek.TUESDAY, DIEZ);

        // 2026-11-17 es martes; 2026-11-18 es miércoles.
        assertEquals(new BigDecimal("1000.00"),
                promocion.calcularDescuento(en(LocalDateTime.of(2026, 11, 17, 9, 0))));
        assertEquals(new BigDecimal("0.00"),
                promocion.calcularDescuento(en(LocalDateTime.of(2026, 11, 18, 9, 0))));
    }

    @Test
    void porHorarioIncluyeInicioYExcluyeFin() {
        var promocion = new PromocionPorHorario("Tarde", LocalTime.of(15, 0), LocalTime.of(17, 0), DIEZ);

        assertEquals(new BigDecimal("1000.00"),
                promocion.calcularDescuento(en(LocalDateTime.of(2026, 11, 17, 15, 0))));
        assertEquals(new BigDecimal("0.00"),
                promocion.calcularDescuento(en(LocalDateTime.of(2026, 11, 17, 17, 0))));
        assertEquals(new BigDecimal("0.00"),
                promocion.calcularDescuento(en(LocalDateTime.of(2026, 11, 17, 14, 59))));
    }

    @Test
    void rechazaPorcentajeFueraDeRango() {
        assertThrows(ReglaNegocioException.class,
                () -> new PromocionPorDia("X", DayOfWeek.MONDAY, BigDecimal.ZERO));
        assertThrows(ReglaNegocioException.class,
                () -> new PromocionPorDia("X", DayOfWeek.MONDAY, new BigDecimal("101")));
    }

    @Test
    void rechazaHorarioInvertido() {
        assertThrows(ReglaNegocioException.class,
                () -> new PromocionPorHorario("X", LocalTime.of(17, 0), LocalTime.of(15, 0), DIEZ));
    }
}
