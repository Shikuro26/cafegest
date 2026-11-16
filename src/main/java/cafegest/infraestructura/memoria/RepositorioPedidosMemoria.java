package cafegest.infraestructura.memoria;

import cafegest.aplicacion.puerto.RepositorioPedidos;
import cafegest.dominio.pedido.Pedido;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Almacenamiento en memoria durante una ejecución; no contiene reglas de negocio. */
public final class RepositorioPedidosMemoria implements RepositorioPedidos {
    private final Map<String, Pedido> pedidos = new LinkedHashMap<>();

    @Override
    public void guardar(Pedido pedido) {
        pedidos.put(pedido.id(), pedido);
    }

    @Override
    public Optional<Pedido> buscar(String id) {
        return Optional.ofNullable(pedidos.get(id));
    }

    @Override
    public List<Pedido> listar() {
        return new ArrayList<>(pedidos.values());
    }
}
