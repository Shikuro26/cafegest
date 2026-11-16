package cafegest.interfaz.grafica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafegest.dominio.catalogo.Categoria;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.dominio.pedido.Pedido;
import cafegest.dominio.pedido.ResumenCobro;
import cafegest.dominio.producto.ProductoBase;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EdicionLineasTest {

    private static Pedido pedidoConLinea() {
        var pedido = new Pedido("P-001");
        pedido.agregarProducto(new ProductoBase(
                new ProductoMenu("cafe", "Café", Categoria.BEBIDA, new BigDecimal("5000"), true)), 1);
        return pedido;
    }

    @Test
    void sinLineasNoSePuedeEditar() {
        var estado = EstadoAcciones.editarLineas(Optional.of(new Pedido("P-001")));

        assertFalse(estado.habilitado());
        assertEquals("No hay productos para editar", estado.motivo());
    }

    @Test
    void conLineaRecibidaSePuedeEditar() {
        assertTrue(EstadoAcciones.editarLineas(Optional.of(pedidoConLinea())).habilitado());
    }

    @Test
    void confirmadoNoSePuedeEditar() {
        var pedido = pedidoConLinea();
        pedido.confirmar(new ResumenCobro(new BigDecimal("5000"), BigDecimal.ZERO));

        var estado = EstadoAcciones.editarLineas(Optional.of(pedido));

        assertFalse(estado.habilitado());
        assertEquals("El pedido ya fue confirmado", estado.motivo());
    }
}
