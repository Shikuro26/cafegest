package cafegest.interfaz.grafica;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSpinner;

/** Da forma de píldora a los campos de selección y de cantidad que FlatLaf no redondea por sí solo. */
final class EstiloControles {
    private static final String ARCO = "JComponent.arc";
    private static final int PILDORA = 999;

    private EstiloControles() {
    }

    static void redondear(JComboBox<?> selector) {
        selector.putClientProperty(ARCO, PILDORA);
    }

    static void redondear(JSpinner spinner) {
        spinner.putClientProperty(ARCO, PILDORA);
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            JComponent campo = editor.getTextField();
            campo.putClientProperty(ARCO, PILDORA);
        }
    }
}
