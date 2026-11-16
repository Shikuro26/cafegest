package cafegest.interfaz.grafica;

import static cafegest.interfaz.grafica.TemaCafe.LATTE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JPanel;

/** Panel con esquinas redondeadas y fondo plano. Sirve de base para tarjetas y contenedores. */
class TarjetaRedondeada extends JPanel {
    private final int arco;
    private Color fondo;

    TarjetaRedondeada(int arco, Color fondo) {
        super(new BorderLayout());
        this.arco = arco;
        this.fondo = fondo;
        setOpaque(false);
    }

    void cambiarFondo(Color nuevoFondo) {
        this.fondo = nuevoFondo;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        var forma = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arco, arco);
        g2.setColor(fondo);
        g2.fill(forma);
        g2.setColor(LATTE);
        g2.draw(forma);
        g2.dispose();
    }
}
