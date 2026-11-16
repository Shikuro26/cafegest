package cafegest.aplicacion.puerto;

import cafegest.dominio.pedido.Pedido;
import java.util.List;
import java.util.Optional;

/** Contrato de almacenamiento de pedidos, independiente de la implementación. */
public interface RepositorioPedidos {
    void guardar(Pedido pedido);

    Optional<Pedido> buscar(String id);

    List<Pedido> listar();
}
