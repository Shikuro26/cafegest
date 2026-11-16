package cafegest.aplicacion.evento;

import cafegest.dominio.pedido.EstadoPedido;

/** Evento inmutable publicado después de una transición válida de estado. */
public record PedidoCambioEstado(String idPedido, EstadoPedido anterior, EstadoPedido nuevo) {
}
