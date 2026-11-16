package cafegest.interfaz.grafica;

import static cafegest.interfaz.grafica.TemaCafe.ACTUAL;
import static cafegest.interfaz.grafica.TemaCafe.BLANCO;
import static cafegest.interfaz.grafica.TemaCafe.CREMA;
import static cafegest.interfaz.grafica.TemaCafe.TEXTO;
import static cafegest.interfaz.grafica.TemaCafe.TEXTO_SUAVE;

import cafegest.aplicacion.ServicioPedidos;
import cafegest.dominio.ReglaNegocioException;
import cafegest.dominio.catalogo.CatalogoMenu;
import cafegest.dominio.catalogo.Categoria;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.dominio.producto.Adicion;
import cafegest.dominio.producto.ReglasAdiciones;
import cafegest.interfaz.Dinero;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Menú para armar la selección del pedido: se toca un producto para sumarlo, se eligen sus adiciones
 * y al final se acepta. Las adiciones se aplican a todas las unidades de ese producto.
 */
final class DialogoAgregarProducto extends JDialog {
    private static final int COLUMNAS = 3;

    private final CatalogoMenu catalogo;
    private final ServicioPedidos servicio;
    private final String idPedido;
    private final Runnable alAgregar;
    private final Map<String, Integer> cantidades = new LinkedHashMap<>();
    private final Map<String, Set<Adicion>> adiciones = new LinkedHashMap<>();
    private final Map<String, TarjetaProducto> tarjetas = new LinkedHashMap<>();
    private final JLabel resumen = new JLabel();
    private final JLabel aviso = new JLabel(" ");
    private final BotonCafe botonAceptar;

    DialogoAgregarProducto(Window dueno, CatalogoMenu catalogo, ServicioPedidos servicio,
            String idPedido, Runnable alAgregar) {
        super(dueno, "Agregar productos", ModalityType.APPLICATION_MODAL);
        this.catalogo = catalogo;
        this.servicio = servicio;
        this.idPedido = idPedido;
        this.alAgregar = alAgregar;

        var grilla = new JPanel(new GridLayout(0, COLUMNAS, 14, 14));
        grilla.setBackground(CREMA);
        for (ProductoMenu producto : catalogo.consultarMenu()) {
            if (producto.disponible()) {
                var tarjeta = new TarjetaProducto(producto);
                tarjetas.put(producto.id(), tarjeta);
                grilla.add(tarjeta);
            }
        }
        var desplazable = new JScrollPane(grilla);
        desplazable.setBorder(BorderFactory.createEmptyBorder());
        desplazable.getViewport().setBackground(CREMA);
        desplazable.getVerticalScrollBar().setUnitIncrement(16);

        var encabezado = new JLabel("Toque un producto para sumarlo al pedido");
        encabezado.setForeground(TEXTO_SUAVE);
        encabezado.setFont(encabezado.getFont().deriveFont(Font.PLAIN, 14f));

        resumen.setForeground(TEXTO);
        resumen.setFont(resumen.getFont().deriveFont(Font.BOLD, 14f));
        aviso.setForeground(TEXTO_SUAVE);

        var cancelar = new BotonCafe("Cancelar", e -> dispose());
        cancelar.configurar(true, null);
        botonAceptar = new BotonCafe("Aceptar", e -> aceptar());

        var botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.add(cancelar);
        botones.add(botonAceptar);

        var pie = new JPanel(new BorderLayout(0, 6));
        pie.setOpaque(false);
        pie.add(aviso, BorderLayout.NORTH);
        var barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        barra.add(resumen, BorderLayout.WEST);
        barra.add(botones, BorderLayout.EAST);
        pie.add(barra, BorderLayout.SOUTH);

        var contenido = new JPanel(new BorderLayout(0, 12));
        contenido.setBackground(CREMA);
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        contenido.add(encabezado, BorderLayout.NORTH);
        contenido.add(desplazable, BorderLayout.CENTER);
        contenido.add(pie, BorderLayout.SOUTH);
        setContentPane(contenido);
        setSize(900, 640);
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(dueno);
        actualizar();
    }

    private void sumar(String codigo) {
        cantidades.merge(codigo, 1, Integer::sum);
        aviso.setText(" ");
        actualizar();
    }

    private void restar(String codigo) {
        cantidades.computeIfPresent(codigo, (k, v) -> v > 1 ? v - 1 : null);
        aviso.setText(" ");
        actualizar();
    }

    private void alternarAdicion(String codigo, Adicion adicion, ChipAdicion chip) {
        if (!chip.disponible()) {
            chip.setSelected(!chip.isSelected());
            return;
        }
        Set<Adicion> elegidas = adiciones.computeIfAbsent(codigo, k -> EnumSet.noneOf(Adicion.class));
        if (chip.isSelected()) {
            elegidas.add(adicion);
        } else {
            elegidas.remove(adicion);
        }
        aviso.setText(" ");
        actualizar();
    }

    private void actualizar() {
        int total = 0;
        for (Map.Entry<String, TarjetaProducto> entrada : tarjetas.entrySet()) {
            String codigo = entrada.getKey();
            int cantidad = cantidades.getOrDefault(codigo, 0);
            total += cantidad;
            entrada.getValue().refrescar(cantidad, adiciones.getOrDefault(codigo, EnumSet.noneOf(Adicion.class)));
        }
        resumen.setText(total == 1 ? "1 artículo" : total + " artículos");
        botonAceptar.configurar(total > 0, total > 0 ? null : "Toque un producto para sumarlo");
    }

    private void aceptar() {
        try {
            var lineas = new ArrayList<Runnable>();
            for (Map.Entry<String, Integer> entrada : cantidades.entrySet()) {
                String codigo = entrada.getKey();
                int cantidad = entrada.getValue();
                List<Adicion> elegidas = List.copyOf(adiciones.getOrDefault(codigo, EnumSet.noneOf(Adicion.class)));
                ProductoMenu base = catalogo.obtenerDisponible(codigo);
                ReglasAdiciones.verificar(base, elegidas);
                lineas.add(() -> servicio.agregarProducto(idPedido, codigo, elegidas, cantidad));
            }
            lineas.forEach(Runnable::run);
            alAgregar.run();
            dispose();
        } catch (ReglaNegocioException error) {
            aviso.setText(error.getMessage());
        }
    }

    /** Tarjeta de un producto: imagen, precio, adiciones compatibles y contador de unidades. */
    private final class TarjetaProducto extends TarjetaRedondeada {
        private final String codigo;
        private final JLabel contador = new JLabel("0", javax.swing.SwingConstants.CENTER);
        private final BotonCafe menos;
        private final Map<Adicion, ChipAdicion> chips = new LinkedHashMap<>();

        TarjetaProducto(ProductoMenu producto) {
            super(18, BLANCO);
            this.codigo = producto.id();
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            var cuerpo = new JPanel();
            cuerpo.setLayout(new BoxLayout(cuerpo, BoxLayout.Y_AXIS));
            cuerpo.setOpaque(false);

            var imagen = new ImagenProducto(producto.id(), producto.categoria());
            imagen.setAlignmentX(Component.LEFT_ALIGNMENT);
            cuerpo.add(imagen);
            cuerpo.add(javax.swing.Box.createVerticalStrut(10));

            var nombre = new JLabel(producto.nombre());
            nombre.setFont(nombre.getFont().deriveFont(Font.BOLD, 15f));
            nombre.setAlignmentX(Component.LEFT_ALIGNMENT);
            cuerpo.add(nombre);
            var precio = new JLabel("$" + Dinero.formatear(producto.precioBase()));
            precio.setForeground(TEXTO_SUAVE);
            precio.setAlignmentX(Component.LEFT_ALIGNMENT);
            cuerpo.add(precio);
            cuerpo.add(javax.swing.Box.createVerticalStrut(8));

            var fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            fila.setOpaque(false);
            fila.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (Adicion adicion : Adicion.values()) {
                var chip = new ChipAdicion(adicion.nombre());
                boolean aplica = adicion.esCompatibleCon(producto.categoria());
                chip.configurar(aplica, "No aplica a este producto");
                chip.addActionListener(e -> alternarAdicion(codigo, adicion, chip));
                chips.put(adicion, chip);
                fila.add(chip);
            }
            cuerpo.add(fila);
            cuerpo.add(javax.swing.Box.createVerticalStrut(8));

            menos = new BotonCafe("−", e -> restar(codigo));
            var mas = new BotonCafe("+", e -> sumar(codigo));
            mas.configurar(true, null);
            contador.setFont(contador.getFont().deriveFont(Font.BOLD, 15f));
            contador.setPreferredSize(new Dimension(44, 30));
            var control = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
            control.setOpaque(false);
            control.setAlignmentX(Component.LEFT_ALIGNMENT);
            control.add(menos);
            control.add(contador);
            control.add(mas);
            cuerpo.add(control);

            add(cuerpo, BorderLayout.CENTER);
            // Tocar la tarjeta suma una unidad; los botones y chips tienen su propia acción.
            conectarClic(cuerpo, () -> sumar(codigo));
        }

        void refrescar(int cantidad, Set<Adicion> elegidas) {
            contador.setText(String.valueOf(cantidad));
            menos.configurar(cantidad > 0, "No hay unidades para quitar");
            cambiarFondo(cantidad > 0 ? ACTUAL : BLANCO);
            chips.forEach((adicion, chip) -> chip.setSelected(elegidas.contains(adicion)));
        }

        private void conectarClic(Component componente, Runnable accion) {
            if (componente instanceof AbstractButton) {
                return;
            }
            componente.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evento) {
                    accion.run();
                }
            });
            if (componente instanceof java.awt.Container contenedor) {
                for (Component hijo : contenedor.getComponents()) {
                    conectarClic(hijo, accion);
                }
            }
        }
    }
}
