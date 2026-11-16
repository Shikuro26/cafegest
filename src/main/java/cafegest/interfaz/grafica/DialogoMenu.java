package cafegest.interfaz.grafica;

import static cafegest.interfaz.grafica.TemaCafe.ARENA;
import static cafegest.interfaz.grafica.TemaCafe.BLANCO;
import static cafegest.interfaz.grafica.TemaCafe.CREMA;
import static cafegest.interfaz.grafica.TemaCafe.TEXTO;

import cafegest.dominio.catalogo.CatalogoMenu;
import cafegest.dominio.catalogo.Categoria;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.interfaz.Dinero;
import java.awt.BorderLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Muestra todo el menú con categoría, precio y disponibilidad, incluidos los productos agotados. */
final class DialogoMenu extends JDialog {

    DialogoMenu(Window dueno, CatalogoMenu catalogo) {
        super(dueno, "Menú", ModalityType.MODELESS);
        var modelo = new DefaultTableModel(
                new Object[] {"Código", "Producto", "Categoría", "Precio", "Disponibilidad"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        for (ProductoMenu producto : catalogo.consultarMenu()) {
            modelo.addRow(new Object[] {
                producto.id(),
                producto.nombre(),
                etiquetaCategoria(producto.categoria()),
                "$" + Dinero.formatear(producto.precioBase()),
                producto.disponible() ? "Disponible" : "Agotado"
            });
        }

        var tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setBackground(BLANCO);
        tabla.setForeground(TEXTO);
        tabla.getTableHeader().setBackground(ARENA);
        tabla.getTableHeader().setForeground(TEXTO);
        tabla.setFillsViewportHeight(true);

        var contenido = new JPanel(new BorderLayout());
        contenido.setBackground(CREMA);
        contenido.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        contenido.add(new JScrollPane(tabla), BorderLayout.CENTER);
        setContentPane(contenido);
        setSize(620, 320);
        setLocationRelativeTo(dueno);
    }

    private static String etiquetaCategoria(Categoria categoria) {
        return switch (categoria) {
            case BEBIDA -> "Bebida";
            case ALIMENTO -> "Alimento";
        };
    }
}
