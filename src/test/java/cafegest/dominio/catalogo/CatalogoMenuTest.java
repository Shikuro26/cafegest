package cafegest.dominio.catalogo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafegest.dominio.ReglaNegocioException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CatalogoMenuTest {

    private static ProductoMenu cafe(boolean disponible) {
        return new ProductoMenu("cafe", "Café americano", Categoria.BEBIDA, new BigDecimal("5000"), disponible);
    }

    @Test
    void consultaLosProductosConPrecioDosDecimales() {
        var catalogo = new CatalogoMenu(List.of(cafe(true)));

        var menu = catalogo.consultarMenu();

        assertEquals(1, menu.size());
        assertEquals(new BigDecimal("5000.00"), menu.get(0).precioBase());
    }

    @Test
    void rechazaIdentificadoresRepetidos() {
        var catalogo = new CatalogoMenu(List.of(cafe(true)));

        assertThrows(ReglaNegocioException.class, () -> catalogo.registrar(cafe(true)));
    }

    @Test
    void obtenerDisponibleRechazaProductoAgotado() {
        var catalogo = new CatalogoMenu(List.of(cafe(false)));

        var error = assertThrows(ReglaNegocioException.class, () -> catalogo.obtenerDisponible("cafe"));

        assertTrue(error.getMessage().contains("no está disponible"));
    }

    @Test
    void obtenerDisponibleRechazaProductoInexistente() {
        var catalogo = new CatalogoMenu(List.of());

        assertThrows(ReglaNegocioException.class, () -> catalogo.obtenerDisponible("te"));
    }

    @Test
    void cambiarDisponibilidadActualizaElProducto() {
        var catalogo = new CatalogoMenu(List.of(cafe(true)));

        catalogo.cambiarDisponibilidad("cafe", false);

        assertFalse(catalogo.buscar("cafe").orElseThrow().disponible());
    }

    @Test
    void rechazaPrecioNegativo() {
        assertThrows(ReglaNegocioException.class,
                () -> new ProductoMenu("x", "X", Categoria.ALIMENTO, new BigDecimal("-1"), true));
    }
}
