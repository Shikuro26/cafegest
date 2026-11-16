package cafegest.interfaz.grafica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cafegest.dominio.catalogo.Categoria;
import cafegest.dominio.catalogo.ProductoMenu;
import cafegest.dominio.pedido.Pedido;
import cafegest.dominio.pedido.ResumenCobro;
import cafegest.dominio.producto.ProductoBase;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EstadoAccionesTest {

    private static Pedido pedidoConLinea() {
        var pedido = new Pedido("P-001");
        pedido.agregarProducto(new ProductoBase(
                new ProductoMenu("cafe", "Café", Categoria.BEBIDA, new BigDecimal("5000"), true)), 1);
        return pedido;
    }

    @Test
    void sinPedidoSoloSePuedeCrearUno() {
        assertTrue(EstadoAcciones.agregarProducto(Optional.empty()).motivo() != null);
        assertFalse(EstadoAcciones.confirmar(Optional.empty()).habilitado());
        assertEquals(1, EstadoAcciones.pasoActual(Optional.empty()));
    }

    @Test
    void confirmarExigeAlMenosUnaLinea() {
        var vacio = Optional.of(new Pedido("P-001"));

        var estado = EstadoAcciones.confirmar(vacio);

        assertFalse(estado.habilitado());
        assertEquals("Agregue al menos un producto", estado.motivo());
        assertTrue(EstadoAcciones.confirmar(Optional.of(pedidoConLinea())).habilitado());
    }

    @Test
    void pasarAPreparacionRequiereConfirmacion() {
        var pedido = pedidoConLinea();

        var antes = EstadoAcciones.pasarAPreparacion(Optional.of(pedido));
        assertFalse(antes.habilitado());
        assertEquals("Confirme el pedido primero", antes.motivo());

        pedido.confirmar(new ResumenCobro(new BigDecimal("5000"), BigDecimal.ZERO));
        assertTrue(EstadoAcciones.pasarAPreparacion(Optional.of(pedido)).habilitado());
    }

    @Test
    void noSePuedeEditarTrasConfirmar() {
        var pedido = pedidoConLinea();
        pedido.confirmar(new ResumenCobro(new BigDecimal("5000"), BigDecimal.ZERO));

        assertFalse(EstadoAcciones.agregarProducto(Optional.of(pedido)).habilitado());
        assertNull(EstadoAcciones.agregarProducto(Optional.of(new Pedido("P-002"))).motivo());
    }

    @Test
    void marcarEntregadoSoloEnPreparacion() {
        var pedido = pedidoConLinea();
        pedido.confirmar(new ResumenCobro(new BigDecimal("5000"), BigDecimal.ZERO));
        assertFalse(EstadoAcciones.marcarEntregado(Optional.of(pedido)).habilitado());

        pedido.cambiarEstado(cafegest.dominio.pedido.EstadoPedido.EN_PREPARACION);
        assertTrue(EstadoAcciones.marcarEntregado(Optional.of(pedido)).habilitado());
    }

    @Test
    void pasoActualAvanzaConElPedido() {
        var pedido = new Pedido("P-001");
        assertEquals(2, EstadoAcciones.pasoActual(Optional.of(pedido)));

        pedido.agregarProducto(new ProductoBase(
                new ProductoMenu("cafe", "Café", Categoria.BEBIDA, new BigDecimal("5000"), true)), 1);
        assertEquals(3, EstadoAcciones.pasoActual(Optional.of(pedido)));

        pedido.confirmar(new ResumenCobro(new BigDecimal("5000"), BigDecimal.ZERO));
        pedido.cambiarEstado(cafegest.dominio.pedido.EstadoPedido.EN_PREPARACION);
        pedido.cambiarEstado(cafegest.dominio.pedido.EstadoPedido.ENTREGADO);
        assertEquals(6, EstadoAcciones.pasoActual(Optional.of(pedido)));
    }
}
