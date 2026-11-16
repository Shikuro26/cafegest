package cafegest.interfaz.grafica;

import static cafegest.interfaz.grafica.TemaCafe.ARENA;
import static cafegest.interfaz.grafica.TemaCafe.BLANCO;
import static cafegest.interfaz.grafica.TemaCafe.LATTE;
import static cafegest.interfaz.grafica.TemaCafe.PREPARACION;
import static cafegest.interfaz.grafica.TemaCafe.TEXTO_SUAVE;

import cafegest.dominio.catalogo.Categoria;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Image;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JComponent;

/**
 * Imagen de un producto. Si existe el archivo {@code /imagenes/productos/CODIGO.png} en los recursos
 * lo muestra; si no, dibuja un ícono sencillo según la categoría.
 */
final class ImagenProducto extends JComponent {
    static final int ANCHO = 170;
    static final int ALTO = 104;
    private static final int ARCO = 16;

    private final String codigo;
    private final Categoria categoria;
    private final BufferedImage archivo;

    ImagenProducto(String codigo, Categoria categoria) {
        this.codigo = codigo;
        this.categoria = categoria;
        this.archivo = cargar(codigo);
        Dimension tamano = new Dimension(ANCHO, ALTO);
        setPreferredSize(tamano);
        setMinimumSize(tamano);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, ALTO));
    }

    private static BufferedImage cargar(String codigo) {
        try (InputStream entrada = ImagenProducto.class.getResourceAsStream("/imagenes/productos/" + codigo + ".png")) {
            return entrada == null ? null : ImageIO.read(entrada);
        } catch (IOException error) {
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        var fondo = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, ARCO, ARCO);
        g2.setColor(categoria == Categoria.BEBIDA ? ARENA : PREPARACION);
        g2.fill(fondo);
        g2.clip(fondo);
        if (archivo != null) {
            dibujarArchivo(g2);
        } else if (categoria == Categoria.BEBIDA) {
            dibujarTaza(g2);
        } else {
            dibujarPlato(g2);
        }
        g2.dispose();
    }

    private void dibujarArchivo(Graphics2D g2) {
        double escala = Math.min((double) getWidth() / archivo.getWidth(), (double) getHeight() / archivo.getHeight());
        int ancho = (int) (archivo.getWidth() * escala);
        int alto = (int) (archivo.getHeight() * escala);
        Image imagen = archivo.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        g2.drawImage(imagen, (getWidth() - ancho) / 2, (getHeight() - alto) / 2, null);
    }

    private void dibujarTaza(Graphics2D g2) {
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        Path2D cuerpo = new Path2D.Double();
        cuerpo.moveTo(cx - 30, cy - 22);
        cuerpo.lineTo(cx + 30, cy - 22);
        cuerpo.lineTo(cx + 22, cy + 20);
        cuerpo.lineTo(cx - 22, cy + 20);
        cuerpo.closePath();
        g2.setColor(new Color(0xB8957A));
        g2.fill(cuerpo);
        g2.setColor(new Color(0xB8957A));
        g2.setStroke(new java.awt.BasicStroke(5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g2.draw(new Arc2D.Double(cx + 22, cy - 12, 16, 22, -90, 180, Arc2D.OPEN));
        g2.setColor(new Color(0xD9C2A7));
        g2.fill(new RoundRectangle2D.Double(cx - 38, cy + 22, 76, 6, 6, 6));
    }

    private void dibujarPlato(Graphics2D g2) {
        double cx = getWidth() / 2.0;
        double cy = getHeight() / 2.0;
        g2.setColor(BLANCO);
        g2.fill(new Ellipse2D.Double(cx - 44, cy - 2, 88, 34));
        g2.setColor(LATTE);
        g2.setStroke(new java.awt.BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Double(cx - 44, cy - 2, 88, 34));
        Path2D porcion = new Path2D.Double();
        porcion.moveTo(cx - 16, cy + 14);
        porcion.lineTo(cx + 24, cy - 12);
        porcion.lineTo(cx + 24, cy + 14);
        porcion.closePath();
        g2.setColor(new Color(0xC98B6E));
        g2.fill(porcion);
    }
}
