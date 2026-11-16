package cafegest.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafegest.dominio.ReglaNegocioException;
import cafegest.dominio.catalogo.Categoria;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.dominio.pedido.Pedido;
import cafegest.dominio.producto.ProductoBase;
import cafegest.dominio.promocion.Promocion;
import cafegest.dominio.promocion.PromocionPorDia;
import cafegest.dominio.promocion.PromocionPorHorario;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalculadorTotalTest {

    // 2026-11-17 15:30 hora de Bogotá (martes).
    private static final Clock MARTES_TARDE = Clock.fixed(Instant.parse("2026-11-17T20:30:00Z"), ZoneId.of("America/Bogota"));

    private static Pedido pedidoDe(String precio) {
        var pedido = new Pedido("P-001");
        pedido.agregarProducto(new ProductoBase(
                new ProductoMenu("p", "Producto", Categoria.ALIMENTO, new BigDecimal(precio), true)), 1);
        return pedido;
    }

    @Test
    void sinPromocionesElDescuentoEsCero() {
        var resumen = new CalculadorTotal(List.of(), MARTES_TARDE).calcular(pedidoDe("10000"));

        assertEquals(new BigDecimal("0.00"), resumen.descuento());
        assertEquals(new BigDecimal("10000.00"), resumen.total());
    }

    @Test
    void aplicaSoloLaMayorPromocionSinAcumular() {
        List<Promocion> promociones = List.of(
                new PromocionPorDia("Martes", DayOfWeek.TUESDAY, new BigDecimal("10")),
                new PromocionPorHorario("Tarde", LocalTime.of(15, 0), LocalTime.of(18, 0), new BigDecimal("20")));

        var resumen = new CalculadorTotal(promociones, MARTES_TARDE).calcular(pedidoDe("10000"));

        assertEquals(new BigDecimal("2000.00"), resumen.descuento());
        assertEquals(new BigDecimal("8000.00"), resumen.total());
    }

    @Test
    void redondeaElDescuentoConHalfUp() {
        // 12,5 % de 1001 = 125,125 -> 125,13
        var resumen = new CalculadorTotal(
                List.of(new PromocionPorDia("Martes", DayOfWeek.TUESDAY, new BigDecimal("12.5"))),
                MARTES_TARDE).calcular(pedidoDe("1001"));

        assertEquals(new BigDecimal("125.13"), resumen.descuento());
        assertEquals(new BigDecimal("875.87"), resumen.total());
    }

    @Test
    void ningunDescuentoSuperaElSubtotal() {
        var resumen = new CalculadorTotal(
                List.of(new PromocionPorDia("Total", DayOfWeek.TUESDAY, new BigDecimal("100"))),
                MARTES_TARDE).calcular(pedidoDe("5000"));

        assertEquals(new BigDecimal("0.00"), resumen.total());
    }

    @Test
    void conservaLaPrimeraPromocionEnEmpate() {
        var resumen = new CalculadorTotal(List.of(
                new PromocionPorDia("A", DayOfWeek.TUESDAY, new BigDecimal("10")),
                new PromocionPorHorario("B", LocalTime.of(15, 0), LocalTime.of(18, 0), new BigDecimal("10"))),
                MARTES_TARDE).calcular(pedidoDe("10000"));

        assertEquals(new BigDecimal("1000.00"), resumen.descuento());
    }

    @Test
    void confirmarPedidoUsaElResumenCalculado() {
        var servicio = new ServicioPedidos(
                new ConfiguradorProducto(new cafegest.dominio.catalogo.CatalogoMenu(List.of())),
                new cafegest.infraestructura.memoria.RepositorioPedidosMemoria(),
                new CalculadorTotal(List.of(), MARTES_TARDE),
                new cafegest.aplicacion.evento.PublicadorEventosPedido(e -> { }));
        String id = servicio.crearPedido();

        assertThrows(ReglaNegocioException.class, () -> servicio.confirmarPedido(id));
    }
}
