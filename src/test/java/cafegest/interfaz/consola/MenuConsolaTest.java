package cafegest.interfaz.consola;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(2)
class MenuConsolaTest {
    @Test
    void permiteSalirDesdeLaConsola() {
        var salida = new StringWriter();
        assertDoesNotThrow(() -> new MenuConsola(new StringReader("0\n"), salida).ejecutar());
        assertTrue(salida.toString().contains("CaféGest"));
        assertTrue(salida.toString().contains("Hasta pronto."));
    }

    @Test
    void recuperaUnaEntradaInvalidaYPermiteSalir() {
        var salida = new StringWriter();
        assertDoesNotThrow(() -> new MenuConsola(new StringReader("no-es-una-opcion\n0\n"), salida).ejecutar());
        assertTrue(salida.toString().contains("Opción inválida."));
        assertTrue(salida.toString().contains("Hasta pronto."));
    }

    @Test
    void terminaAlAgotarseLaEntradaSinBucleInfinito() {
        var salida = new StringWriter();
        assertDoesNotThrow(() -> new MenuConsola(new StringReader(""), salida).ejecutar());
        assertTrue(salida.toString().contains("Hasta pronto."));
    }
}
