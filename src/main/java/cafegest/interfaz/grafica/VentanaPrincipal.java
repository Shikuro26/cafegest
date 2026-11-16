package cafegest.interfaz.grafica;

import static cafegest.interfaz.grafica.TemaCafe.ACTUAL;
import static cafegest.interfaz.grafica.TemaCafe.ARENA;
import static cafegest.interfaz.grafica.TemaCafe.BLANCO;
import static cafegest.interfaz.grafica.TemaCafe.BOTON;
import static cafegest.interfaz.grafica.TemaCafe.BOTON_APAGADO_TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.BOTON_TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.CREMA;
import static cafegest.interfaz.grafica.TemaCafe.ENTREGADOS;
import static cafegest.interfaz.grafica.TemaCafe.ENTREGADOS_TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.LATTE;
import static cafegest.interfaz.grafica.TemaCafe.PREPARACION;
import static cafegest.interfaz.grafica.TemaCafe.PREPARACION_TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.RECIBIDOS;
import static cafegest.interfaz.grafica.TemaCafe.RECIBIDOS_TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.TEXTO_SUAVE;

import cafegest.aplicacion.ServicioPedidos;
import cafegest.aplicacion.evento.PublicadorEventosPedido;
import cafegest.configuracion.ConfiguracionCafeteria;
import cafegest.dominio.ReglaNegocioException;
import cafegest.dominio.catalogo.CatalogoMenu;
import cafegest.dominio.pedido.EstadoPedido;
import cafegest.dominio.pedido.LineaPedido;
import cafegest.dominio.pedido.Pedido;
import cafegest.dominio.pedido.ResumenCobro;
import cafegest.interfaz.Dinero;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Ventana de escritorio: pasos del pedido, detalle, acciones y tablero por estado. */
public final class VentanaPrincipal {
    private static final String[] NOMBRES_PASOS = {
        "Crear pedido", "Agregar productos", "Confirmar", "Preparar", "Entregar"
    };

    static {
        // Debe correr antes de crear cualquier componente: los campos de instancia se crean antes del constructor.
        aplicarTema();
    }

    private final CatalogoMenu catalogo;
    private final ServicioPedidos servicio;
    private final JFrame ventana = new JFrame("CaféGest");
    private final JComboBox<String> selectorPedido = new JComboBox<>();
    private final JComboBox<String> selectorLinea = new JComboBox<>();
    private final JSpinner cantidadNueva = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final DetallePedido detalle = new DetallePedido();
    private final JLabel[] etiquetasPasos = new JLabel[NOMBRES_PASOS.length];
    private final TarjetaRedondeada[] contenedoresPasos = new TarjetaRedondeada[NOMBRES_PASOS.length];
    private final BotonCafe botonAgregar;
    private final BotonCafe botonConfirmar;
    private final BotonCafe botonPreparar;
    private final BotonCafe botonEntregar;
    private final BotonCafe botonCambiarCantidad;
    private final BotonCafe botonQuitarLinea;
    private final BotonCafe botonVerMenu;
    private final ColumnaEstado columnaRecibidos = new ColumnaEstado("Recibidos", RECIBIDOS, RECIBIDOS_TEXTO);
    private final ColumnaEstado columnaPreparacion = new ColumnaEstado("En preparación", PREPARACION, PREPARACION_TEXTO);
    private final ColumnaEstado columnaEntregados = new ColumnaEstado("Entregados", ENTREGADOS, ENTREGADOS_TEXTO);
    private boolean actualizandoSelector;

    public VentanaPrincipal() {
        this.catalogo = ConfiguracionCafeteria.catalogoPorDefecto();
        var publicador = new PublicadorEventosPedido(error ->
                SwingUtilities.invokeLater(() -> mostrarAviso(error.getMessage())));
        publicador.suscribir(evento -> SwingUtilities.invokeLater(() -> refrescar(null)));
        this.servicio = ConfiguracionCafeteria.crearServicio(
                catalogo, ConfiguracionCafeteria.relojLocal(), publicador);

        botonAgregar = new BotonCafe("Agregar producto", e -> accion(this::abrirAgregarProducto));
        botonConfirmar = new BotonCafe("Confirmar pedido", e -> accion(() -> {
            ResumenCobro resumen = servicio.confirmarPedido(idSeleccionado());
            refrescar(null);
            mostrarInfo("Pedido confirmado. Total: $" + Dinero.formatear(resumen.total()));
        }));
        botonPreparar = new BotonCafe("Pasar a preparación", e ->
                accion(() -> cambiarEstado(EstadoPedido.EN_PREPARACION)));
        botonEntregar = new BotonCafe("Marcar entregado", e ->
                accion(() -> cambiarEstado(EstadoPedido.ENTREGADO)));
        botonCambiarCantidad = new BotonCafe("Cambiar cantidad", e -> accion(() -> {
            servicio.modificarCantidad(idSeleccionado(), numeroLineaSeleccionada(),
                    (Integer) cantidadNueva.getValue());
            refrescar(null);
        }));
        botonQuitarLinea = new BotonCafe("Quitar línea", e -> accion(() -> {
            servicio.retirarProducto(idSeleccionado(), numeroLineaSeleccionada());
            refrescar(null);
        }));
        botonVerMenu = new BotonCafe("Ver menú", e -> new DialogoMenu(ventana, catalogo).setVisible(true));
        botonVerMenu.configurar(true, null);

        construir();
        EstiloControles.redondear(selectorPedido);
        EstiloControles.redondear(selectorLinea);
        EstiloControles.redondear(cantidadNueva);
        refrescar(null);
    }

    public void mostrar() {
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.pack();
        ventana.setMinimumSize(new Dimension(1000, 640));
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    /** Instala FlatLaf y lo tiñe con la paleta café: esquinas redondeadas y colores pastel. */
    private static void aplicarTema() {
        FlatLightLaf.setup();
        UIManager.put("Component.arc", 14);
        UIManager.put("Button.arc", 999);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.accentColor", BOTON);
        UIManager.put("Component.focusedBorderColor", BOTON);
        UIManager.put("Component.focusColor", BOTON);
        UIManager.put("TextComponent.arc", 999);
        UIManager.put("ComboBox.arc", 999);
        UIManager.put("Spinner.arc", 999);
        UIManager.put("TextComponent.borderColor", LATTE);
        UIManager.put("Spinner.borderColor", LATTE);
        UIManager.put("ComboBox.borderColor", LATTE);
        UIManager.put("Component.borderColor", LATTE);
        UIManager.put("Panel.background", CREMA);
        UIManager.put("OptionPane.background", CREMA);
        UIManager.put("OptionPane.messageForeground", TEXTO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("CheckBox.background", CREMA);
        UIManager.put("CheckBox.foreground", TEXTO);
        UIManager.put("CheckBox.disabledText", BOTON_APAGADO_TEXTO);
        UIManager.put("ComboBox.background", BLANCO);
        UIManager.put("ComboBox.foreground", TEXTO);
        UIManager.put("ComboBox.selectionBackground", ACTUAL);
        UIManager.put("ComboBox.selectionForeground", TEXTO);
        UIManager.put("Spinner.background", BLANCO);
        UIManager.put("TextField.background", BLANCO);
        UIManager.put("Table.background", BLANCO);
        UIManager.put("Table.selectionBackground", ACTUAL);
        UIManager.put("Table.selectionForeground", TEXTO);
        UIManager.put("TableHeader.background", ARENA);
        UIManager.put("TableHeader.foreground", TEXTO);
    }

    private void construir() {
        var sidebar = new TarjetaRedondeada(22, ARENA);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 18, 20, 18));
        sidebar.setPreferredSize(new Dimension(240, 0));

        JLabel titulo = new JLabel("CaféGest");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setForeground(TEXTO);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(titulo);
        sidebar.add(Box.createVerticalStrut(18));

        JLabel etiquetaSelector = new JLabel("Pedido");
        etiquetaSelector.setForeground(TEXTO_SUAVE);
        etiquetaSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(etiquetaSelector);
        sidebar.add(Box.createVerticalStrut(4));
        selectorPedido.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        selectorPedido.setAlignmentX(Component.LEFT_ALIGNMENT);
        selectorPedido.addActionListener(e -> {
            if (!actualizandoSelector) {
                refrescar(null);
            }
        });
        sidebar.add(selectorPedido);
        sidebar.add(Box.createVerticalStrut(10));

        var botonCrear = new BotonCafe("Crear pedido", e -> accion(() -> refrescar(servicio.crearPedido())));
        botonCrear.configurar(true, null);
        botonCrear.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(botonCrear);
        sidebar.add(Box.createVerticalStrut(22));

        for (int i = 0; i < NOMBRES_PASOS.length; i++) {
            JLabel paso = new JLabel(" ");
            paso.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
            var contenedor = new TarjetaRedondeada(16, ARENA);
            contenedor.add(paso, BorderLayout.CENTER);
            contenedor.setAlignmentX(Component.LEFT_ALIGNMENT);
            contenedor.setMaximumSize(new Dimension(Integer.MAX_VALUE, contenedor.getPreferredSize().height));
            etiquetasPasos[i] = paso;
            contenedoresPasos[i] = contenedor;
            sidebar.add(contenedor);
            sidebar.add(Box.createVerticalStrut(6));
        }

        var tarjeta = new TarjetaRedondeada(20, BLANCO);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        tarjeta.add(detalle, BorderLayout.CENTER);

        var acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        acciones.add(botonAgregar);
        acciones.add(botonConfirmar);
        acciones.add(botonPreparar);
        acciones.add(botonEntregar);
        acciones.add(botonVerMenu);

        selectorLinea.setPreferredSize(new Dimension(280, 32));
        cantidadNueva.setPreferredSize(new Dimension(72, 32));
        var edicion = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        edicion.setOpaque(false);
        edicion.add(new JLabel("Línea"));
        edicion.add(selectorLinea);
        edicion.add(new JLabel("Cantidad"));
        edicion.add(cantidadNueva);
        edicion.add(botonCambiarCantidad);
        edicion.add(botonQuitarLinea);

        var controles = new JPanel(new BorderLayout(0, 10));
        controles.setOpaque(false);
        controles.add(acciones, BorderLayout.NORTH);
        controles.add(edicion, BorderLayout.SOUTH);

        var tablero = new JPanel(new GridLayout(1, 3, 14, 0));
        tablero.setOpaque(false);
        tablero.add(columnaRecibidos);
        tablero.add(columnaPreparacion);
        tablero.add(columnaEntregados);

        var centro = new JPanel(new BorderLayout(0, 14));
        centro.setOpaque(false);
        centro.add(controles, BorderLayout.NORTH);
        centro.add(tarjeta, BorderLayout.CENTER);
        centro.add(tablero, BorderLayout.SOUTH);

        var raiz = new JPanel(new BorderLayout(18, 0));
        raiz.setBackground(CREMA);
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        raiz.add(sidebar, BorderLayout.WEST);
        raiz.add(centro, BorderLayout.CENTER);
        ventana.setContentPane(raiz);
    }

    /** Vuelve a leer el estado del sistema y actualiza todos los paneles. */
    private void refrescar(String idSeleccionar) {
        String anterior = (String) selectorPedido.getSelectedItem();
        String seleccion = idSeleccionar != null ? idSeleccionar : anterior;
        List<Pedido> pedidos = servicio.listarPedidos();

        actualizandoSelector = true;
        selectorPedido.removeAllItems();
        for (Pedido pedido : pedidos) {
            selectorPedido.addItem(pedido.id());
        }
        if (seleccion != null && pedidos.stream().anyMatch(p -> p.id().equals(seleccion))) {
            selectorPedido.setSelectedItem(seleccion);
        } else if (!pedidos.isEmpty()) {
            selectorPedido.setSelectedIndex(pedidos.size() - 1);
        }
        actualizandoSelector = false;

        Optional<Pedido> pedido = pedidoSeleccionado();
        mostrarDetalle(pedido);
        actualizarLineas(pedido);
        actualizarPasos(pedido);
        actualizarBoton(botonAgregar, EstadoAcciones.agregarProducto(pedido));
        actualizarBoton(botonConfirmar, EstadoAcciones.confirmar(pedido));
        actualizarBoton(botonPreparar, EstadoAcciones.pasarAPreparacion(pedido));
        actualizarBoton(botonEntregar, EstadoAcciones.marcarEntregado(pedido));
        actualizarBoton(botonCambiarCantidad, EstadoAcciones.editarLineas(pedido));
        actualizarBoton(botonQuitarLinea, EstadoAcciones.editarLineas(pedido));
        actualizarTablero(pedidos);
    }

    private void actualizarBoton(BotonCafe boton, EstadoAcciones.Estado estado) {
        boton.configurar(estado.habilitado(), estado.motivo());
    }

    private void actualizarLineas(Optional<Pedido> pedido) {
        selectorLinea.removeAllItems();
        pedido.ifPresent(p -> p.lineas().forEach(linea -> selectorLinea.addItem(
                linea.numero() + " · " + linea.cantidad() + " × " + linea.producto().descripcion())));
        if (selectorLinea.getItemCount() > 0) {
            selectorLinea.setSelectedIndex(0);
        }
    }

    private int numeroLineaSeleccionada() {
        String texto = (String) selectorLinea.getSelectedItem();
        if (texto == null) {
            throw new ReglaNegocioException("Seleccione una línea del pedido.");
        }
        return Integer.parseInt(texto.substring(0, texto.indexOf(' ')));
    }

    private void actualizarPasos(Optional<Pedido> pedido) {
        int actual = EstadoAcciones.pasoActual(pedido);
        for (int i = 0; i < NOMBRES_PASOS.length; i++) {
            JLabel paso = etiquetasPasos[i];
            int numero = i + 1;
            if (numero < actual) {
                paso.setText("✓  " + NOMBRES_PASOS[i]);
                paso.setForeground(TEXTO_SUAVE);
                contenedoresPasos[i].cambiarFondo(ARENA);
                paso.setFont(paso.getFont().deriveFont(Font.PLAIN));
            } else if (numero == actual) {
                paso.setText("›  " + NOMBRES_PASOS[i]);
                paso.setForeground(TEXTO);
                contenedoresPasos[i].cambiarFondo(ACTUAL);
                paso.setFont(paso.getFont().deriveFont(Font.BOLD));
            } else {
                paso.setText("   " + NOMBRES_PASOS[i]);
                paso.setForeground(BOTON_APAGADO_TEXTO);
                contenedoresPasos[i].cambiarFondo(ARENA);
                paso.setFont(paso.getFont().deriveFont(Font.PLAIN));
            }
        }
    }

    private void mostrarDetalle(Optional<Pedido> seleccion) {
        detalle.mostrar(seleccion);
    }

    private void actualizarTablero(List<Pedido> pedidos) {
        columnaRecibidos.mostrar(lineasDe(pedidos, EstadoPedido.RECIBIDO));
        columnaPreparacion.mostrar(lineasDe(pedidos, EstadoPedido.EN_PREPARACION));
        columnaEntregados.mostrar(lineasDe(pedidos, EstadoPedido.ENTREGADO));
    }

    private static List<String> lineasDe(List<Pedido> pedidos, EstadoPedido estado) {
        var lineas = new ArrayList<String>();
        for (Pedido pedido : pedidos) {
            if (pedido.estado() == estado) {
                lineas.add(pedido.id() + "   $" + Dinero.formatear(pedido.subtotal()));
            }
        }
        return lineas;
    }

    private static String etiquetaEstado(EstadoPedido estado) {
        return switch (estado) {
            case RECIBIDO -> "recibido";
            case EN_PREPARACION -> "en preparación";
            case ENTREGADO -> "entregado";
        };
    }

    private void abrirAgregarProducto() {
        new DialogoAgregarProducto(ventana, catalogo, servicio, idSeleccionado(),
                () -> refrescar(idSeleccionado())).setVisible(true);
    }

    private void cambiarEstado(EstadoPedido destino) {
        servicio.cambiarEstado(idSeleccionado(), destino);
        refrescar(null);
    }

    private Optional<Pedido> pedidoSeleccionado() {
        String id = (String) selectorPedido.getSelectedItem();
        if (id == null) {
            return Optional.empty();
        }
        return servicio.listarPedidos().stream().filter(p -> p.id().equals(id)).findFirst();
    }

    private String idSeleccionado() {
        return pedidoSeleccionado()
                .map(Pedido::id)
                .orElseThrow(() -> new ReglaNegocioException("Cree o seleccione un pedido."));
    }

    private void accion(Runnable operacion) {
        try {
            operacion.run();
        } catch (ReglaNegocioException error) {
            mostrarAviso(error.getMessage());
        }
    }

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(ventana, mensaje, "CaféGest", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(ventana, mensaje, "CaféGest", JOptionPane.INFORMATION_MESSAGE);
    }
}
