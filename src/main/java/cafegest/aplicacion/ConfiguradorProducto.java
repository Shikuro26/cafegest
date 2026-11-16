package cafegest.aplicacion;

import cafegest.dominio.catalogo.CatalogoMenu;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.dominio.producto.Adicion;
import cafegest.dominio.producto.ProductoBase;
import cafegest.dominio.producto.ProductoPedido;
import cafegest.dominio.producto.ReglasAdiciones;
import java.util.List;

/** Crea la configuración de una unidad de producto validando disponibilidad y adiciones antes de construirla. */
public final class ConfiguradorProducto {
    private final CatalogoMenu catalogo;

    public ConfiguradorProducto(CatalogoMenu catalogo) {
        this.catalogo = catalogo;
    }

    public ProductoPedido configurar(String productoId, List<Adicion> adiciones) {
        ProductoMenu base = catalogo.obtenerDisponible(productoId);
        ReglasAdiciones.verificar(base, adiciones);
        ProductoPedido configurado = new ProductoBase(base);
        for (Adicion adicion : adiciones) {
            configurado = adicion.aplicar(configurado);
        }
        return configurado;
    }
}
