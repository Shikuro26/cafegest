package cafegest.interfaz.grafica;

import static cafegest.interfaz.grafica.TemaCafe.ARENA;
import static cafegest.interfaz.grafica.TemaCafe.ENTREGADOS;
import static cafegest.interfaz.grafica.TemaCafe.ENTREGADOS_TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.LATTE;
import static cafegest.interfaz.grafica.TemaCafe.PREPARACION;
import static cafegest.interfaz.grafica.TemaCafe.PREPARACION_TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.RECIBIDOS;
import static cafegest.interfaz.grafica.TemaCafe.RECIBIDOS_TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.TEXTO_SUAVE;

import cafegest.dominio.pedido.EstadoPedido;
import cafegest.dominio.pedido.LineaPedido;
import cafegest.dominio.pedido.Pedido;
import cafegest.interfaz.Dinero;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.util.Optional;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** Ficha del pedido seleccionado: encabezado con estado, productos en filas y totales. */
final class DetallePedido extends JPanel {
    private final JPanel encabezado = new JPanel(new BorderLayout());
    private final JPanel lineas = new JPanel();
    private final TarjetaRedondeada totales = new TarjetaRedondeada(16, ARENA);

    DetallePedido() {
        super(new BorderLayout(0, 14));
        setOpaque(false);
        encabezado.setOpaque(false);

        lineas.setLayout(new BoxLayout(lineas, BoxLayout.Y_AXIS));
        lineas.setOpaque(false);
        var arriba = new JPanel(new BorderLayout());
        arriba.setOpaque(false);
        arriba.add(lineas, BorderLayout.NORTH);
        var desplazable = new JScrollPane(arriba);
        desplazable.setBorder(BorderFactory.createEmptyBorder());
        desplazable.setOpaque(false);
        desplazable.getViewport().setOpaque(false);
        desplazable.getVerticalScrollBar().setUnitIncrement(12);

        totales.setLayout(new BoxLayout(totales, BoxLayout.Y_AXIS));
        totales.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        add(encabezado, BorderLayout.NORTH);
        add(desplazable, BorderLayout.CENTER);
        add(totales, BorderLayout.SOUTH);
    }

    void mostrar(Optional<Pedido> seleccion) {
        encabezado.removeAll();
        lineas.removeAll();
        totales.removeAll();

        if (seleccion.isEmpty()) {
            var vacio = new JLabel("Cree un pedido para comenzar.");
            vacio.setForeground(TEXTO_SUAVE);
            vacio.setFont(vacio.getFont().deriveFont(15f));
            encabezado.add(vacio, BorderLayout.WEST);
        } else {
            mostrarPedido(seleccion.get());
        }
        revalidate();
        repaint();
    }

    private void mostrarPedido(Pedido pedido) {
        var titulo = new JLabel("Pedido " + pedido.id());
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        titulo.setForeground(TEXTO);
        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(pastilla(pedido.estado()), BorderLayout.EAST);

        if (pedido.lineas().isEmpty()) {
            var vacio = new JLabel("Aún no hay productos. Use «Agregar producto».");
            vacio.setForeground(TEXTO_SUAVE);
            vacio.setFont(vacio.getFont().deriveFont(14f));
            vacio.setAlignmentX(Component.LEFT_ALIGNMENT);
            lineas.add(vacio);
        }
        boolean primera = true;
        for (LineaPedido linea : pedido.lineas()) {
            if (!primera) {
                lineas.add(separador());
            }
            lineas.add(fila(linea));
            primera = false;
        }

        fila(totales, "Subtotal", pedido.subtotal(), false);
        pedido.resumen().ifPresent(r -> {
            fila(totales, "Descuento", r.descuento(), false);
            fila(totales, "Total", r.total(), true);
        });
    }

    private record Colores(Color fondo, Color texto, String nombre) {
    }

    private static JLabel pastilla(EstadoPedido estado) {
        Colores colores = switch (estado) {
            case RECIBIDO -> new Colores(RECIBIDOS, RECIBIDOS_TEXTO, "Recibido");
            case EN_PREPARACION -> new Colores(PREPARACION, PREPARACION_TEXTO, "En preparación");
            case ENTREGADO -> new Colores(ENTREGADOS, ENTREGADOS_TEXTO, "Entregado");
        };
        var etiqueta = new JLabel(colores.nombre()) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                var g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colores.fondo());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        etiqueta.setOpaque(false);
        etiqueta.setForeground(colores.texto());
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 13f));
        etiqueta.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        return etiqueta;
    }

    private static JPanel fila(LineaPedido linea) {
        String descripcion = linea.producto().descripcion();
        int corte = descripcion.indexOf(" + ");
        String producto = corte < 0 ? descripcion : descripcion.substring(0, corte);
        String adiciones = corte < 0 ? null : descripcion.substring(corte + 3).replace(" + ", " · ");

        var nombre = new JLabel(linea.cantidad() + " × " + producto);
        nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 15f));
        nombre.setForeground(TEXTO);
        var bloque = new JPanel();
        bloque.setLayout(new BoxLayout(bloque, BoxLayout.Y_AXIS));
        bloque.setOpaque(false);
        bloque.add(nombre);
        if (adiciones != null) {
            var detalle = new JLabel("+ " + adiciones);
            detalle.setForeground(TEXTO_SUAVE);
            detalle.setFont(detalle.getFont().deriveFont(13f));
            bloque.add(detalle);
        }

        var precio = new JLabel("$" + Dinero.formatear(linea.subtotal()));
        precio.setFont(precio.getFont().deriveFont(Font.BOLD, 15f));
        precio.setForeground(TEXTO);

        var fila = new JPanel(new BorderLayout(12, 0));
        fila.setOpaque(false);
        fila.setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 4));
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.add(bloque, BorderLayout.CENTER);
        fila.add(precio, BorderLayout.EAST);
        return fila;
    }

    private static JPanel separador() {
        var linea = new JPanel();
        linea.setBackground(LATTE);
        linea.setPreferredSize(new Dimension(10, 1));
        linea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        linea.setAlignmentX(Component.LEFT_ALIGNMENT);
        return linea;
    }

    private void fila(TarjetaRedondeada destino, String etiqueta, BigDecimal valor, boolean destacada) {
        var fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.setAlignmentX(Component.LEFT_ALIGNMENT);
        fila.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        var nombre = new JLabel(etiqueta);
        var importe = new JLabel("$" + Dinero.formatear(valor));
        float tamano = destacada ? 17f : 14f;
        int estilo = destacada ? Font.BOLD : Font.PLAIN;
        nombre.setFont(nombre.getFont().deriveFont(estilo, tamano));
        importe.setFont(importe.getFont().deriveFont(estilo, tamano));
        nombre.setForeground(destacada ? TEXTO : TEXTO_SUAVE);
        importe.setForeground(TEXTO);
        fila.add(nombre, BorderLayout.WEST);
        fila.add(importe, BorderLayout.EAST);
        destino.add(fila);
    }
}
