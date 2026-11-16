package cafegest.interfaz.grafica;

import static cafegest.interfaz.grafica.TemaCafe.BLANCO;
import static cafegest.interfaz.grafica.TemaCafe.TEXTO_SUAVE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Columna redondeada del tablero con los pedidos de un mismo estado. */
final class ColumnaEstado extends TarjetaRedondeada {
    private final JPanel lista = new JPanel();

    ColumnaEstado(String titulo, Color fondo, Color textoTitulo) {
        super(18, fondo);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JLabel encabezado = new JLabel(titulo);
        encabezado.setForeground(textoTitulo);
        encabezado.setFont(encabezado.getFont().deriveFont(Font.BOLD, 14f));
        add(encabezado, BorderLayout.NORTH);

        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setOpaque(false);
        add(lista, BorderLayout.CENTER);
    }

    void mostrar(List<String> lineas) {
        lista.removeAll();
        if (lineas.isEmpty()) {
            JLabel vacio = new JLabel("Sin pedidos");
            vacio.setForeground(TEXTO_SUAVE);
            vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            lista.add(vacio);
        }
        for (String linea : lineas) {
            var tarjeta = new TarjetaRedondeada(14, BLANCO);
            tarjeta.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            JLabel texto = new JLabel(linea);
            texto.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
            tarjeta.add(texto, BorderLayout.CENTER);
            tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);
            tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, tarjeta.getPreferredSize().height));
            lista.add(tarjeta);
            lista.add(Box.createVerticalStrut(8));
        }
        lista.revalidate();
        lista.repaint();
    }
}
