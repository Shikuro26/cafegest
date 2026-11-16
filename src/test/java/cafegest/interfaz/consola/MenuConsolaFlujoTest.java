package cafegest.interfaz.consola;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(5)
class MenuConsolaFlujoTest {

    private static String ejecutar(String entrada) {
        var salida = new StringWriter();
        assertDoesNotThrow(() -> new MenuConsola(new StringReader(entrada), salida).ejecutar());
        return salida.toString();
    }

    @Test
    void flujoCompletoDeUnPedido() {
        String salida = ejecutar("""
                2
                3
                P-001
                CAF-AME
                1
                2
                6
                P-001
                9
                P-001
                1
                7
                P-001
                0
                """);

        assertTrue(salida.contains("Pedido P-001 creado en estado RECIBIDO."));
        assertTrue(salida.contains("Subtotal: $13000"));
        assertTrue(salida.contains("Pedido P-001 confirmado."));
        assertTrue(salida.contains("[Caja] Pedido P-001: RECIBIDO -> EN_PREPARACION"));
        assertTrue(salida.contains("[Cocina] Preparar pedido P-001"));
        assertTrue(salida.contains("Pedido P-001 — EN_PREPARACION"));
        assertTrue(salida.contains("Hasta pronto."));
    }

    @Test
    void entradaInvalidaNoPierdeElPedido() {
        String salida = ejecutar("""
                2
                3
                P-001
                CAF-AME

                abc
                7
                P-001
                0
                """);

        assertTrue(salida.contains("Ingrese un número entero válido."));
        assertTrue(salida.contains("(sin productos)"));
        assertTrue(salida.contains("Hasta pronto."));
    }

    @Test
    void productoAgotadoSeRechazaConMensajeClaro() {
        String salida = ejecutar("""
                2
                3
                P-001
                TE

                1
                0
                """);

        assertTrue(salida.contains("Té no está disponible en este momento."));
    }

    @Test
    void transicionInvalidaSeInformaSinCerrarElPrograma() {
        String salida = ejecutar("""
                2
                3
                P-001
                CAF-AME

                1
                6
                P-001
                9
                P-001
                2
                0
                """);

        assertTrue(salida.contains("No se permite pasar el pedido P-001 de RECIBIDO a ENTREGADO."));
        assertTrue(salida.contains("Hasta pronto."));
    }
}
