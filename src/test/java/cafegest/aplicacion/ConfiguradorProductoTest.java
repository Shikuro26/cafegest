package cafegest.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cafegest.dominio.ReglaNegocioException;
import cafegest.dominio.catalogo.CatalogoMenu;
import cafegest.dominio.catalogo.Categoria;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.dominio.producto.Adicion;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfiguradorProductoTest {

    private final CatalogoMenu catalogo = new CatalogoMenu(List.of(
            new ProductoMenu("cafe", "Café americano", Categoria.BEBIDA, new BigDecimal("5000"), true),
            new ProductoMenu("torta", "Torta de chocolate", Categoria.ALIMENTO, new BigDecimal("6000"), true),
            new ProductoMenu("te", "Té", Categoria.BEBIDA, new BigDecimal("4000"), false)));

    private final ConfiguradorProducto configurador = new ConfiguradorProducto(catalogo);

    @Test
    void sumaVariasAdicionesCompatibles() {
        var producto = configurador.configurar("cafe", List.of(Adicion.SHOT_EXTRA, Adicion.LECHE_VEGETAL));

        assertEquals(new BigDecimal("7500.00"), producto.precio());
        assertEquals("Café americano + Shot extra + Leche vegetal", producto.descripcion());
    }

    @Test
    void sinAdicionesConservaElPrecioBase() {
        var producto = configurador.configurar("cafe", List.of());

        assertEquals(new BigDecimal("5000.00"), producto.precio());
        assertEquals("Café americano", producto.descripcion());
    }

    @Test
    void rechazaAdicionIncompatibleConLaCategoria() {
        var error = assertThrows(ReglaNegocioException.class,
                () -> configurador.configurar("torta", List.of(Adicion.SHOT_EXTRA)));

        assertEquals("Shot extra no es compatible con Torta de chocolate.", error.getMessage());
    }

    @Test
    void rechazaRepetirUnaAdicion() {
        assertThrows(ReglaNegocioException.class,
                () -> configurador.configurar("cafe", List.of(Adicion.SHOT_EXTRA, Adicion.SHOT_EXTRA)));
    }

    @Test
    void rechazaProductoAgotadoSinConstruirConfiguracion() {
        assertThrows(ReglaNegocioException.class, () -> configurador.configurar("te", List.of()));
    }
}
