package cafegest.interfaz.grafica;

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
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/** Ventana de escritorio. Usa los mismos servicios que la consola; no calcula reglas por su cuenta. */
public final class VentanaPrincipal {
    private final CatalogoMenu catalogo;
    private final ServicioPedidos servicio;
    private final JFrame ventana = new JFrame("CaféGest — Gestión de pedidos");
    private final JTextField campoPedido = new JTextField(8);
    private final JComboBox<String> productos = new JComboBox<>();
    private final Map<Adicion, JCheckBox> casillasAdiciones = new EnumMap<>(Adicion.class);
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JTextArea detalle = new JTextArea(14, 42);

    public VentanaPrincipal() {
        this.catalogo = ConfiguracionCafeteria.catalogoPorDefecto();
        var caja = new VistaCaja();
        var cocina = new VistaCocina();
        var publicador = new PublicadorEventosPedido(error ->
                SwingUtilities.invokeLater(() -> mostrarError(error.getMessage())));
        publicador.suscribir(caja);
        publicador.suscribir(cocina);
        this.servicio = ConfiguracionCafeteria.crearServicio(
                catalogo, ConfiguracionCafeteria.relojLocal(), publicador);
        construir(caja, cocina);
    }

    public void mostrar() {
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    private void construir(VistaCaja caja, VistaCocina cocina) {
        detalle.setEditable(false);
        detalle.setLineWrap(true);

        for (ProductoMenu producto : catalogo.consultarMenu()) {
            String etiqueta = producto.id() + " - " + producto.nombre() + " ($" + producto.precioBase().toPlainString()
                    + ")" + (producto.disponible() ? "" : " [agotado]");
            productos.addItem(etiqueta);
        }
        for (Adicion adicion : Adicion.values()) {
            casillasAdiciones.put(adicion, new JCheckBox(adicion.nombre()));
        }

        var formulario = new JPanel(new GridLayout(0, 1, 4, 4));
        formulario.setBorder(BorderFactory.createTitledBorder("Pedido"));
        var fila = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila.add(new JLabel("Número de pedido:"));
        fila.add(campoPedido);
        formulario.add(fila);
        formulario.add(crearFila("Producto:", productos));
        var adicionesPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        casillasAdiciones.values().forEach(adicionesPanel::add);
        formulario.add(crearFila("Adiciones:", adicionesPanel));
        formulario.add(crearFila("Cantidad:", cantidad));

        var acciones = new JPanel(new GridLayout(0, 2, 4, 4));
        acciones.add(boton("Crear pedido", e -> accion(() -> {
            String id = servicio.crearPedido();
            campoPedido.setText(id);
            mostrarPedido(servicio.consultarPedido(id));
        })));
        acciones.add(boton("Agregar al pedido", e -> accion(this::agregarProducto)));
        acciones.add(boton("Consultar pedido", e -> accion(() -> mostrarPedido(servicio.consultarPedido(idPedido())))));
        acciones.add(boton("Confirmar pedido", e -> accion(this::confirmarPedido)));
        acciones.add(boton("Pasar a preparación", e -> accion(() -> cambiarEstado(EstadoPedido.EN_PREPARACION))));
        acciones.add(boton("Marcar entregado", e -> accion(() -> cambiarEstado(EstadoPedido.ENTREGADO))));

        var izquierda = new JPanel(new BorderLayout(8, 8));
        izquierda.add(formulario, BorderLayout.NORTH);
        izquierda.add(acciones, BorderLayout.CENTER);

        var vistas = new JPanel(new GridLayout(1, 2, 8, 8));
        vistas.add(caja);
        vistas.add(cocina);

        var raiz = new JPanel(new BorderLayout(8, 8));
        raiz.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        raiz.add(izquierda, BorderLayout.WEST);
        raiz.add(new JScrollPane(detalle), BorderLayout.CENTER);
        raiz.add(vistas, BorderLayout.SOUTH);
        ventana.setContentPane(raiz);
    }

    private void agregarProducto() {
        String codigo = seleccionado();
        List<Adicion> seleccion = new ArrayList<>();
        casillasAdiciones.forEach((adicion, casilla) -> {
            if (casilla.isSelected()) {
                seleccion.add(adicion);
            }
        });
        int numero = servicio.agregarProducto(idPedido(), codigo, seleccion, (Integer) cantidad.getValue());
        mostrarPedido(servicio.consultarPedido(idPedido()));
        mostrarInfo("Línea " + numero + " agregada.");
    }

    private void confirmarPedido() {
        ResumenCobro resumen = servicio.confirmarPedido(idPedido());
        mostrarPedido(servicio.consultarPedido(idPedido()));
        mostrarInfo("Pedido confirmado. Total: $" + resumen.total().toPlainString());
    }

    private void cambiarEstado(EstadoPedido destino) {
        servicio.cambiarEstado(idPedido(), destino);
        mostrarPedido(servicio.consultarPedido(idPedido()));
    }

    private String idPedido() {
        String id = campoPedido.getText().trim();
        if (id.isEmpty()) {
            throw new ReglaNegocioException("Indique el número de pedido o cree uno nuevo.");
        }
        return id;
    }

    private String seleccionado() {
        String etiqueta = (String) productos.getSelectedItem();
        return etiqueta.substring(0, etiqueta.indexOf(" - "));
    }

    private void mostrarPedido(Pedido pedido) {
        var texto = new StringBuilder();
        texto.append("Pedido ").append(pedido.id()).append(" — ").append(pedido.estado()).append('\n');
        if (pedido.lineas().isEmpty()) {
            texto.append("  (sin productos)\n");
        }
        for (LineaPedido linea : pedido.lineas()) {
            texto.append("  ").append(linea.numero()).append(". ").append(linea.cantidad()).append(" x ")
                    .append(linea.producto().descripcion()).append(" — $")
                    .append(linea.subtotal().toPlainString()).append('\n');
        }
        texto.append("Subtotal: $").append(pedido.subtotal().toPlainString()).append('\n');
        pedido.resumen().ifPresent(r -> texto
                .append("Descuento: $").append(r.descuento().toPlainString()).append('\n')
                .append("Total: $").append(r.total().toPlainString()).append('\n'));
        detalle.setText(texto.toString());
    }

    private void accion(Runnable operacion) {
        try {
            operacion.run();
        } catch (ReglaNegocioException error) {
            mostrarError(error.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(ventana, mensaje, "CaféGest", JOptionPane.WARNING_MESSAGE);
    }

    private void mostrarInfo(String mensaje) {
        JOptionPane.showMessageDialog(ventana, mensaje, "CaféGest", JOptionPane.INFORMATION_MESSAGE);
    }

    private static JPanel crearFila(String etiqueta, java.awt.Component componente) {
        var fila = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fila.add(new JLabel(etiqueta));
        fila.add(componente);
        return fila;
    }

    private static JButton boton(String texto, java.awt.event.ActionListener escucha) {
        var boton = new JButton(texto);
        boton.addActionListener(escucha);
        return boton;
    }
}
