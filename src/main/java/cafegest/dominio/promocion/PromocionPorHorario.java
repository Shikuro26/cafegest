package cafegest.dominio.promocion;

import cafegest.dominio.ReglaNegocioException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Objects;

/** Promoción vigente en un intervalo horario diario: inicio incluido, fin excluido. */
public final class PromocionPorHorario implements Promocion {
    private final String nombre;
    private final LocalTime inicio;
    private final LocalTime fin;
    private final BigDecimal porcentaje;

    public PromocionPorHorario(String nombre, LocalTime inicio, LocalTime fin, BigDecimal porcentaje) {
        this.nombre = Objects.requireNonNull(nombre);
        this.inicio = Objects.requireNonNull(inicio);
        this.fin = Objects.requireNonNull(fin);
        if (!inicio.isBefore(fin)) {
            throw new ReglaNegocioException("La hora de inicio debe ser anterior a la hora de fin.");
        }
        this.porcentaje = DescuentoPorcentual.validar(porcentaje);
    }

    @Override
    public String nombre() {
        return nombre;
    }

    @Override
    public BigDecimal calcularDescuento(ContextoPromocion contexto) {
        LocalTime hora = contexto.momento().toLocalTime();
        boolean vigente = !hora.isBefore(inicio) && hora.isBefore(fin);
        if (!vigente) {
            return BigDecimal.ZERO.setScale(2);
        }
        return DescuentoPorcentual.sobre(contexto.subtotal(), porcentaje);
    }
}
