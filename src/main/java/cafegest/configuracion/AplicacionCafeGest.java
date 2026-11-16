package cafegest.configuracion;

import cafegest.interfaz.consola.MenuConsola;
import cafegest.interfaz.grafica.VentanaPrincipal;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.swing.SwingUtilities;

/** Punto de entrada: consola por defecto, o interfaz gráfica con el argumento {@code gui}. */
public final class AplicacionCafeGest {
    private AplicacionCafeGest() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && "gui".equalsIgnoreCase(args[0])) {
            SwingUtilities.invokeLater(() -> new VentanaPrincipal().mostrar());
            return;
        }
        var entrada = new InputStreamReader(System.in, StandardCharsets.UTF_8);
        var salida = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
        new MenuConsola(entrada, salida).ejecutar();
    }
}
