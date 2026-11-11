package cafegest.configuracion;

import cafegest.interfaz.consola.MenuConsola;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/** Punto de entrada de la versión inicial por consola. */
public final class AplicacionCafeGest {
    private AplicacionCafeGest() {
    }

    public static void main(String[] args) throws IOException {
        var entrada = new InputStreamReader(System.in, StandardCharsets.UTF_8);
        var salida = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
        new MenuConsola(entrada, salida).ejecutar();
    }
}
