package cafegest.interfaz.grafica;

import cafegest.aplicacion.evento.PedidoCambioEstado;
import cafegest.aplicacion.puerto.ObservadorPedido;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/** Panel base de caja y cocina: recibe avisos y los agrega al registro visible en el hilo de la interfaz. */
public abstract class VistaPedidosSwing extends JPanel implements ObservadorPedido {
    private final JTextArea registro = new JTextArea(10, 30);

    protected VistaPedidosSwing(String titulo) {
        super(new BorderLayout());
        registro.setEditable(false);
        setBorder(BorderFactory.createTitledBorder(titulo));
        add(new JScrollPane(registro), BorderLayout.CENTER);
    }

    @Override
    public final void actualizar(PedidoCambioEstado evento) {
        if (SwingUtilities.isEventDispatchThread()) {
            registrar(evento);
        } else {
            SwingUtilities.invokeLater(() -> registrar(evento));
        }
    }

    /** Texto del aviso para esta vista, o {@code null} si la vista no muestra ese cambio. */
    protected abstract String mensaje(PedidoCambioEstado evento);

    private void registrar(PedidoCambioEstado evento) {
        String texto = mensaje(evento);
        if (texto != null) {
            registro.append(texto + "\n");
        }
    }
}
