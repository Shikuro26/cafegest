package cafegest.dominio.pedido;

/** Estados del pedido. Solo se admite avanzar al estado siguiente. */
public enum EstadoPedido {
    RECIBIDO,
    EN_PREPARACION,
    ENTREGADO;

    public boolean puedePasarA(EstadoPedido destino) {
        return (this == RECIBIDO && destino == EN_PREPARACION)
                || (this == EN_PREPARACION && destino == ENTREGADO);
    }
}
