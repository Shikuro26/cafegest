package cafegest.aplicacion.puerto;

import cafegest.aplicacion.evento.PedidoCambioEstado;

/** Contrato de los receptores de avisos de pedidos (caja, cocina u otras vistas). */
public interface ObservadorPedido {
    void actualizar(PedidoCambioEstado evento);
}
