package cafegest.interfaz.consola;

import cafegest.aplicacion.ServicioPedidos;
import cafegest.aplicacion.evento.PublicadorEventosPedido;
import cafegest.configuracion.ConfiguracionCafeteria;
import cafegest.dominio.ReglaNegocioException;
import cafegest.dominio.catalogo.CatalogoMenu;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.dominio.pedido.EstadoPedido;
import cafegest.dominio.pedido.LineaPedido;
import cafegest.dominio.pedido.Pedido;
import cafegest.dominio.pedido.ResumenCobro;
import cafegest.dominio.producto.Adicion;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Interfaz de consola. Recoge entradas y muestra resultados; las reglas están en los servicios.
 * Una entrada inválida muestra un aviso y vuelve al menú sin cambiar los pedidos.
 */
public final class MenuConsola {
    private final BufferedReader entrada;
    private final PrintWriter salida;
    private final CatalogoMenu catalogo;
    private final ServicioPedidos servicio;

    public MenuConsola(Reader entrada, Writer salida) {
        this.entrada = new BufferedReader(entrada);
        this.salida = new PrintWriter(salida);
        this.catalogo = ConfiguracionCafeteria.catalogoPorDefecto();
        var publicador = new PublicadorEventosPedido(error -> this.salida.println("[Aviso] " + error.getMessage()));
        publicador.suscribir(new VistaCaja(this.salida));
        publicador.suscribir(new VistaCocina(this.salida));
        this.servicio = ConfiguracionCafeteria.crearServicio(
                catalogo, ConfiguracionCafeteria.relojLocal(), publicador);
    }

    public void ejecutar() throws IOException {
        salida.println("CaféGest — Gestión de pedidos para cafeterías");
        salida.flush();
        try {
            while (true) {
                mostrarMenu();
                String opcion = leer("Seleccione una opción: ");
                if (opcion.equals("0")) {
                    break;
                }
                ejecutarOpcion(opcion);
            }
        } catch (FinDeEntrada fin) {
            // Se agotó la entrada: se cierra igual que con la opción 0.
        }
        salida.println("Hasta pronto.");
        salida.flush();
    }

    private void mostrarMenu() {
        salida.println();
        salida.println("1. Consultar menú");
        salida.println("2. Crear pedido");
        salida.println("3. Agregar producto a un pedido");
        salida.println("4. Modificar cantidad de una línea");
        salida.println("5. Retirar una línea de un pedido");
        salida.println("6. Confirmar pedido");
        salida.println("7. Consultar pedido");
        salida.println("8. Listar pedidos de la sesión");
        salida.println("9. Cambiar estado de un pedido");
        salida.println("0. Salir");
        salida.flush();
    }

    private void ejecutarOpcion(String opcion) throws IOException {
        try {
            switch (opcion) {
                case "1" -> mostrarMenuDeProductos();
                case "2" -> salida.println("Pedido " + servicio.crearPedido() + " creado en estado RECIBIDO.");
                case "3" -> agregarProducto();
                case "4" -> modificarCantidad();
                case "5" -> retirarLinea();
                case "6" -> confirmarPedido();
                case "7" -> mostrarPedido(servicio.consultarPedido(leer("Número de pedido: ")));
                case "8" -> listarPedidos();
                case "9" -> cambiarEstado();
                default -> salida.println("Opción inválida. Escriba un número del menú.");
            }
        } catch (ReglaNegocioException error) {
            salida.println("Error: " + error.getMessage());
        }
        salida.flush();
    }

    private void mostrarMenuDeProductos() {
        salida.println("Código    Producto                Categoría  Precio      Disponibilidad");
        for (ProductoMenu producto : catalogo.consultarMenu()) {
            salida.printf("%-9s %-23s %-10s $%-10s %s%n",
                    producto.id(), producto.nombre(), producto.categoria(), producto.precioBase().toPlainString(),
                    producto.disponible() ? "disponible" : "agotado");
        }
    }

    private void agregarProducto() throws IOException {
        String idPedido = leer("Número de pedido: ");
        String codigo = leer("Código del producto: ");
        List<Adicion> adiciones = leerAdiciones();
        int cantidad = leerEntero("Cantidad: ");
        int numero = servicio.agregarProducto(idPedido, codigo, adiciones, cantidad);
        salida.println("Línea " + numero + " agregada al pedido " + idPedido + ". Subtotal: "
                + dinero(servicio.consultarPedido(idPedido).subtotal()));
    }

    private void modificarCantidad() throws IOException {
        String idPedido = leer("Número de pedido: ");
        int linea = leerEntero("Número de línea: ");
        int cantidad = leerEntero("Nueva cantidad: ");
        servicio.modificarCantidad(idPedido, linea, cantidad);
        salida.println("Cantidad actualizada en la línea " + linea + " del pedido " + idPedido + ".");
    }

    private void retirarLinea() throws IOException {
        String idPedido = leer("Número de pedido: ");
        int linea = leerEntero("Número de línea: ");
        servicio.retirarProducto(idPedido, linea);
        salida.println("Línea " + linea + " retirada del pedido " + idPedido + ".");
    }

    private void confirmarPedido() throws IOException {
        String idPedido = leer("Número de pedido: ");
        ResumenCobro resumen = servicio.confirmarPedido(idPedido);
        salida.println("Pedido " + idPedido + " confirmado.");
        mostrarResumen(resumen);
    }

    private void cambiarEstado() throws IOException {
        String idPedido = leer("Número de pedido: ");
        salida.println("1. EN_PREPARACION");
        salida.println("2. ENTREGADO");
        EstadoPedido destino = switch (leerEntero("Nuevo estado: ")) {
            case 1 -> EstadoPedido.EN_PREPARACION;
            case 2 -> EstadoPedido.ENTREGADO;
            default -> throw new ReglaNegocioException("Estado no válido.");
        };
        servicio.cambiarEstado(idPedido, destino);
    }

    private void listarPedidos() {
        List<Pedido> pedidos = servicio.listarPedidos();
        if (pedidos.isEmpty()) {
            salida.println("No hay pedidos en esta sesión.");
            return;
        }
        for (Pedido pedido : pedidos) {
            salida.println(pedido.id() + "  " + pedido.estado() + "  Subtotal " + dinero(pedido.subtotal()));
        }
    }

    private void mostrarPedido(Pedido pedido) {
        salida.println("Pedido " + pedido.id() + " — " + pedido.estado());
        if (pedido.lineas().isEmpty()) {
            salida.println("  (sin productos)");
        }
        for (LineaPedido linea : pedido.lineas()) {
            salida.println("  " + linea.numero() + ". " + linea.cantidad() + " x " + linea.producto().descripcion()
                    + " — " + dinero(linea.subtotal()));
        }
        salida.println("  Subtotal: " + dinero(pedido.subtotal()));
        pedido.resumen().ifPresent(this::mostrarResumen);
    }

    private void mostrarResumen(ResumenCobro resumen) {
        salida.println("  Subtotal: " + dinero(resumen.subtotal()));
        salida.println("  Descuento: " + dinero(resumen.descuento()));
        salida.println("  Total: " + dinero(resumen.total()));
    }

    private List<Adicion> leerAdiciones() throws IOException {
        String texto = leer("Adiciones (1. Shot extra, 2. Leche vegetal; separadas por coma, Enter para ninguna): ");
        var adiciones = new ArrayList<Adicion>();
        if (texto.isEmpty()) {
            return adiciones;
        }
        Adicion[] disponibles = Adicion.values();
        for (String token : texto.split(",")) {
            int indice = parsearEntero(token.trim()) - 1;
            if (indice < 0 || indice >= disponibles.length) {
                throw new ReglaNegocioException("Adición no válida: " + token.trim() + ".");
            }
            adiciones.add(disponibles[indice]);
        }
        return adiciones;
    }

    private String leer(String mensaje) throws IOException {
        salida.print(mensaje);
        salida.flush();
        String linea = entrada.readLine();
        if (linea == null) {
            throw new FinDeEntrada();
        }
        return linea.trim();
    }

    private int leerEntero(String mensaje) throws IOException {
        return parsearEntero(leer(mensaje));
    }

    private static int parsearEntero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException error) {
            throw new ReglaNegocioException("Ingrese un número entero válido.");
        }
    }

    private static String dinero(BigDecimal valor) {
        return "$" + valor.toPlainString();
    }

    /** Señal interna para terminar el menú cuando se agota la entrada. */
    private static final class FinDeEntrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
