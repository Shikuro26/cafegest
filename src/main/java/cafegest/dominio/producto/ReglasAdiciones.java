package cafegest.dominio.producto;

import cafegest.dominio.ReglaNegocioException;
import cafegest.dominio.catalogo.ProductoMenu;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Valida compatibilidad y repeticiones de adiciones antes de construir la configuración. */
public final class ReglasAdiciones {
    private ReglasAdiciones() {
    }

    public static void verificar(ProductoMenu base, List<Adicion> adiciones) {
        Set<Adicion> vistas = new HashSet<>();
        for (Adicion adicion : adiciones) {
            if (!adicion.esCompatibleCon(base.categoria())) {
                throw new ReglaNegocioException(
                        adicion.nombre() + " no es compatible con " + base.nombre() + ".");
            }
            if (!vistas.add(adicion)) {
                throw new ReglaNegocioException("No se puede repetir la adición " + adicion.nombre() + ".");
            }
        }
    }
}
