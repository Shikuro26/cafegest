package cafegest.configuracion;

import cafegest.aplicacion.CalculadorTotal;
import cafegest.aplicacion.ConfiguradorProducto;
import cafegest.aplicacion.ServicioPedidos;
import cafegest.aplicacion.evento.PublicadorEventosPedido;
import cafegest.dominio.catalogo.CatalogoMenu;
import cafegest.dominio.catalogo.Categoria;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.dominio.promocion.Promocion;
import cafegest.dominio.promocion.PromocionPorDia;
import cafegest.dominio.promocion.PromocionPorHorario;
import cafegest.infraestructura.memoria.RepositorioPedidosMemoria;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/** Menú, promociones y reloj internos de la cafetería. Son configuración, no pantallas de administración. */
public final class ConfiguracionCafeteria {
    private static final ZoneId ZONA_LOCAL = ZoneId.of("America/Bogota");

    private ConfiguracionCafeteria() {
    }

    public static CatalogoMenu catalogoPorDefecto() {
        return new CatalogoMenu(List.of(
                new ProductoMenu("CAF-AME", "Café americano", Categoria.BEBIDA, new BigDecimal("5000"), true),
                new ProductoMenu("CAP", "Capuchino", Categoria.BEBIDA, new BigDecimal("6500"), true),
                new ProductoMenu("TE", "Té", Categoria.BEBIDA, new BigDecimal("4000"), false),
                new ProductoMenu("TOR-CHO", "Torta de chocolate", Categoria.ALIMENTO, new BigDecimal("6000"), true),
                new ProductoMenu("SAN-POL", "Sándwich de pollo", Categoria.ALIMENTO, new BigDecimal("8000"), true)));
    }

    public static List<Promocion> promocionesPorDefecto() {
        return List.of(
                new PromocionPorDia("Martes de café", DayOfWeek.TUESDAY, new BigDecimal("10")),
                new PromocionPorHorario("Hora feliz", LocalTime.of(15, 0), LocalTime.of(17, 0), new BigDecimal("15")));
    }

    public static Clock relojLocal() {
        return Clock.system(ZONA_LOCAL);
    }

    public static ServicioPedidos crearServicio(CatalogoMenu catalogo, Clock reloj, PublicadorEventosPedido publicador) {
        return new ServicioPedidos(
                new ConfiguradorProducto(catalogo),
                new RepositorioPedidosMemoria(),
                new CalculadorTotal(promocionesPorDefecto(), reloj),
                publicador);
    }
}
