package cafegest.dominio.pedido;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafegest.dominio.ReglaNegocioException;
import cafegest.dominio.catalogo.Categoria;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.dominio.producto.ProductoBase;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PedidoTest {

    private static ProductoBase cafe() {
        return new ProductoBase(new ProductoMenu("cafe", "Café americano", Categoria.BEBIDA, new BigDecimal("5000"), true));
    }

    private static ProductoBase torta() {
        return new ProductoBase(new ProductoMenu("torta", "Torta", Categoria.ALIMENTO, new BigDecimal("6000"), true));
    }

    @Test
    void nuevoPedidoEstaRecibidoYVacio() {
        var pedido = new Pedido("P-001");

        assertEquals(EstadoPedido.RECIBIDO, pedido.estado());
        assertEquals(new BigDecimal("0.00"), pedido.subtotal());
        assertTrue(pedido.lineas().isEmpty());
    }

    @Test
    void subtotalMultiplicaCantidadPorPrecio() {
        var pedido = new Pedido("P-001");
        pedido.agregarProducto(cafe(), 2);
        pedido.agregarProducto(torta(), 1);

        assertEquals(new BigDecimal("16000.00"), pedido.subtotal());
    }

    @Test
    void modificarYRetirarActualizanElSubtotal() {
        var pedido = new Pedido("P-001");
        int linea = pedido.agregarProducto(cafe(), 1);
        pedido.agregarProducto(torta(), 1);

        pedido.modificarCantidad(linea, 3);
        assertEquals(new BigDecimal("21000.00"), pedido.subtotal());

        pedido.retirarProducto(linea);
        assertEquals(new BigDecimal("6000.00"), pedido.subtotal());
    }

    @Test
    void rechazaCantidadesQueNoSonEnterosPositivos() {
        var pedido = new Pedido("P-001");

        var error = assertThrows(ReglaNegocioException.class, () -> pedido.agregarProducto(cafe(), 0));

        assertEquals("La cantidad debe ser un número entero mayor que cero.", error.getMessage());
        assertThrows(ReglaNegocioException.class, () -> pedido.agregarProducto(cafe(), -2));
        assertTrue(pedido.lineas().isEmpty());
    }

    @Test
    void rechazaLineaInexistente() {
        var pedido = new Pedido("P-001");

        assertThrows(ReglaNegocioException.class, () -> pedido.retirarProducto(9));
    }

    @Test
    void noPermiteConfirmarPedidoVacio() {
        var pedido = new Pedido("P-001");

        var error = assertThrows(ReglaNegocioException.class,
                () -> pedido.confirmar(new ResumenCobro(BigDecimal.ZERO, BigDecimal.ZERO)));

        assertTrue(error.getMessage().contains("está vacío"));
        assertTrue(!pedido.confirmado());
    }

    @Test
    void confirmadoNoAdmiteCambios() {
        var pedido = new Pedido("P-001");
        int linea = pedido.agregarProducto(cafe(), 1);
        pedido.confirmar(new ResumenCobro(new BigDecimal("5000"), BigDecimal.ZERO));

        assertThrows(ReglaNegocioException.class, () -> pedido.modificarCantidad(linea, 2));
        assertThrows(ReglaNegocioException.class, () -> pedido.retirarProducto(linea));
        assertThrows(ReglaNegocioException.class, () -> pedido.agregarProducto(torta(), 1));
        assertEquals(new BigDecimal("5000.00"), pedido.resumen().orElseThrow().total());
    }
}
