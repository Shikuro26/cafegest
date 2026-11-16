package cafegest.interfaz.grafica;

import static cafegest.interfaz.grafica.TemaCafe.BOTON;
import static cafegest.interfaz.grafica.TemaCafe.BOTON_APAGADO;
import static cafegest.interfaz.grafica.TemaCafe.BOTON_APAGADO_TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.BOTON_HOVER;
import static cafegest.interfaz.grafica.TemaCafe.BOTON_PRESIONADO;
import static cafegest.interfaz.grafica.TemaCafe.BOTON_TEXTO;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * Botón plano con estado no disponible. Cuando no está disponible no ejecuta su acción y muestra
 * el motivo al pasar el cursor. Se pinta a mano para no depender del aspecto del sistema.
 */
final class BotonCafe extends JButton {
    private boolean disponible = true;

    BotonCafe(String texto, ActionListener accion) {
        super(texto);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setFont(getFont().deriveFont(Font.PLAIN, 14f));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        addActionListener(evento -> {
            if (disponible) {
                accion.actionPerformed(evento);
            }
        });
    }

    void configurar(boolean disponible, String motivo) {
        this.disponible = disponible;
        setToolTipText(disponible ? null : motivo);
        setCursor(Cursor.getPredefinedCursor(disponible ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.Color fondo;
        java.awt.Color texto;
        if (!disponible) {
            fondo = BOTON_APAGADO;
            texto = BOTON_APAGADO_TEXTO;
        } else if (getModel().isPressed()) {
            fondo = BOTON_PRESIONADO;
            texto = BOTON_TEXTO;
        } else if (getModel().isRollover()) {
            fondo = BOTON_HOVER;
            texto = BOTON_TEXTO;
        } else {
            fondo = BOTON;
            texto = BOTON_TEXTO;
        }
        g2.setColor(fondo);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
        g2.setColor(texto);
        g2.setFont(getFont());
        FontMetrics metricas = g2.getFontMetrics();
        int x = (getWidth() - metricas.stringWidth(getText())) / 2;
        int y = (getHeight() + metricas.getAscent() - metricas.getDescent()) / 2;
        g2.drawString(getText(), x, y);
        g2.dispose();
    }
}
