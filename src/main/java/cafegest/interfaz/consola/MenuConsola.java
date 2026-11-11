package cafegest.interfaz.consola;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

/** Interfaz inicial; las operaciones de pedidos se incorporarán con sus servicios. */
public final class MenuConsola {
    private final BufferedReader entrada;
    private final PrintWriter salida;

    public MenuConsola(Reader entrada, Writer salida) {
        this.entrada = new BufferedReader(entrada);
        this.salida = new PrintWriter(salida);
    }

    public void ejecutar() throws IOException {
        salida.println("CaféGest — Gestión de pedidos para cafeterías");
        salida.println("Versión inicial: las operaciones de pedidos están en desarrollo.");
        while (true) {
            salida.println("0. Salir");
            salida.print("Seleccione una opción: ");
            salida.flush();
            String opcion = entrada.readLine();
            if (opcion == null || opcion.trim().equals("0")) {
                salida.println("Hasta pronto.");
                salida.flush();
                return;
            }
            salida.println("Opción inválida. Escriba 0 para salir.");
        }
    }
}
