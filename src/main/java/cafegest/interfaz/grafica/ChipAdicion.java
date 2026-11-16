package cafegest.interfaz.grafica;

import static cafegest.interfaz.grafica.TemaCafe.BLANCO;
import static cafegest.interfaz.grafica.TemaCafe.BOTON;
import static cafegest.interfaz.grafica.TemaCafe.BOTON_APAGADO;
import static cafegest.interfaz.grafica.TemaCafe.BOTON_APAGADO_TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.BOTON_TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.LATTE;
import static cafegest.interfaz.grafica.TemaCafe.TEXTO;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JToggleButton;

/** Adición seleccionable en forma de píldora. Cuando no aplica queda apagada y no cambia de estado. */
final class ChipAdicion extends JToggleButton {
    private boolean disponible = true;

    ChipAdicion(String texto) {
        super(texto);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setFont(getFont().deriveFont(Font.PLAIN, 12f));
        setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
    }

    void configurar(boolean disponible, String motivo) {
        this.disponible = disponible;
        setToolTipText(disponible ? null : motivo);
        setCursor(Cursor.getPredefinedCursor(disponible ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }

    boolean disponible() {
        return disponible;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fondo;
        Color texto;
        Color borde = null;
        if (!disponible) {
            fondo = BOTON_APAGADO;
            texto = BOTON_APAGADO_TEXTO;
        } else if (isSelected()) {
            fondo = BOTON;
            texto = BOTON_TEXTO;
        } else {
            fondo = BLANCO;
            texto = TEXTO;
            borde = LATTE;
        }
        int arco = getHeight();
        var forma = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
        g2.setColor(fondo);
        g2.fill(forma);
        if (borde != null) {
            g2.setColor(borde);
            g2.draw(forma);
        }
        g2.setColor(texto);
        g2.setFont(getFont());
        var metricas = g2.getFontMetrics();
        int x = (getWidth() - metricas.stringWidth(getText())) / 2;
        int y = (getHeight() + metricas.getAscent() - metricas.getDescent()) / 2;
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
