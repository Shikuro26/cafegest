package cafegest.interfaz.grafica;

import cafegest.interfaz.Dinero;
import static cafegest.interfaz.grafica.TemaCafe.BLANCO;
import static cafegest.interfaz.grafica.TemaCafe.BOTON_APAGADO_TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.CREMA;
import static cafegest.interfaz.grafica.TemaCafe.TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.TEXTO_SUAVE;

import cafegest.aplicacion.ServicioPedidos;
import cafegest.dominio.ReglaNegocioException;
import cafegest.dominio.catalogo.CatalogoMenu;
import cafegest.dominio.catalogo.Categoria;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.dominio.producto.Adicion;
import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/** Ventana para elegir un producto disponible, sus adiciones compatibles y la cantidad. */
final class DialogoAgregarProducto extends JDialog {

    private record Opcion(String codigo, String texto, Categoria categoria) {
        @Override
        public String toString() {
            return texto;
        }
    }

    private final JComboBox<Opcion> productos = new JComboBox<>();
    private final Map<Adicion, JCheckBox> casillas = new EnumMap<>(Adicion.class);
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
    private final JLabel aviso = new JLabel(" ");
    private final ServicioPedidos servicio;
    private final String idPedido;
    private final Runnable alAgregar;

    DialogoAgregarProducto(Window dueno, CatalogoMenu catalogo, ServicioPedidos servicio,
            String idPedido, Runnable alAgregar) {
        super(dueno, "Agregar producto", ModalityType.APPLICATION_MODAL);
        this.servicio = servicio;
        this.idPedido = idPedido;
        this.alAgregar = alAgregar;

        for (ProductoMenu producto : catalogo.consultarMenu()) {
            if (producto.disponible()) {
                productos.addItem(new Opcion(producto.id(),
                        producto.nombre() + " · $" + Dinero.formatear(producto.precioBase()), producto.categoria()));
            }
        }
        productos.addActionListener(e -> actualizarAdiciones());
        EstiloControles.redondear(productos);
        EstiloControles.redondear(cantidad);
        for (Adicion adicion : Adicion.values()) {
            JCheckBox casilla = new JCheckBox(adicion.nombre() + " (+$" + Dinero.formatear(adicion.precio()) + ")");
            casilla.setBackground(CREMA);
            casillas.put(adicion, casilla);
        }
        aviso.setForeground(TEXTO_SUAVE);

        var contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(CREMA);
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        contenido.add(fila("Producto", productos));
        var panelAdiciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelAdiciones.setOpaque(false);
        casillas.values().forEach(panelAdiciones::add);
        contenido.add(fila("Adiciones", panelAdiciones));
        contenido.add(fila("Cantidad", cantidad));
        contenido.add(aviso);

        var botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.setOpaque(false);
        var agregar = new BotonCafe("Agregar", e -> agregar());
        agregar.configurar(true, null);
        var cancelar = new BotonCafe("Cancelar", e -> dispose());
        cancelar.configurar(true, null);
        botones.add(cancelar);
        botones.add(agregar);
        contenido.add(botones);

        setContentPane(contenido);
        actualizarAdiciones();
        pack();
        setMinimumSize(new Dimension(480, getHeight()));
        setLocationRelativeTo(dueno);
    }

    /** Solo permite las adiciones compatibles con la categoría del producto elegido. */
    private void actualizarAdiciones() {
        Opcion elegido = (Opcion) productos.getSelectedItem();
        for (Map.Entry<Adicion, JCheckBox> entrada : casillas.entrySet()) {
            boolean compatible = elegido != null && entrada.getKey().esCompatibleCon(elegido.categoria());
            JCheckBox casilla = entrada.getValue();
            casilla.setEnabled(compatible);
            if (!compatible) {
                casilla.setSelected(false);
            }
            casilla.setToolTipText(compatible ? null : "No aplica a este producto");
        }
    }

    private void agregar() {
        Opcion elegido = (Opcion) productos.getSelectedItem();
        if (elegido == null) {
            aviso.setText("No hay productos disponibles en este momento.");
            return;
        }
        List<Adicion> seleccion = new ArrayList<>();
        casillas.forEach((adicion, casilla) -> {
            if (casilla.isSelected()) {
                seleccion.add(adicion);
            }
        });
        try {
            servicio.agregarProducto(idPedido, elegido.codigo(), seleccion, (Integer) cantidad.getValue());
            alAgregar.run();
            dispose();
        } catch (ReglaNegocioException error) {
            aviso.setText(error.getMessage());
        }
    }

    private static JPanel fila(String etiqueta, java.awt.Component componente) {
        var panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        JLabel texto = new JLabel(etiqueta);
        texto.setForeground(TEXTO);
        texto.setPreferredSize(new Dimension(90, texto.getPreferredSize().height));
        panel.add(texto, BorderLayout.WEST);
        panel.add(componente, BorderLayout.CENTER);
        panel.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        return panel;
    }
}
