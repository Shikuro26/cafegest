package cafegest.dominio.pedido;

/** Estados del pedido. Las transiciones válidas se controlan en {@link Pedido}. */
public enum EstadoPedido {
    RECIBIDO,
    EN_PREPARACION,
    ENTREGADO
}
