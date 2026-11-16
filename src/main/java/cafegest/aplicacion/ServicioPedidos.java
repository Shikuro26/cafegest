package cafegest.aplicacion;

import cafegest.aplicacion.evento.PedidoCambioEstado;
import cafegest.aplicacion.evento.PublicadorEventosPedido;
import cafegest.aplicacion.puerto.RepositorioPedidos;
import cafegest.dominio.ReglaNegocioException;
import cafegest.dominio.pedido.EstadoPedido;
import cafegest.dominio.pedido.Pedido;
import cafegest.dominio.pedido.ResumenCobro;
import cafegest.dominio.producto.Adicion;
import cafegest.dominio.producto.ProductoPedido;
import java.util.List;

/** Casos de uso de pedidos. Es el único punto por el que consola e interfaz gráfica cambian pedidos. */
public final class ServicioPedidos {
    private final ConfiguradorProducto configurador;
    private final RepositorioPedidos repositorio;
    private final CalculadorTotal calculador;
    private final PublicadorEventosPedido publicador;
    private int secuencia;

    public ServicioPedidos(ConfiguradorProducto configurador, RepositorioPedidos repositorio,
            CalculadorTotal calculador, PublicadorEventosPedido publicador) {
        this.configurador = configurador;
        this.repositorio = repositorio;
        this.calculador = calculador;
        this.publicador = publicador;
    }

    /** Avanza el estado y, solo si la transición es válida, notifica a caja y cocina. */
    public void cambiarEstado(String idPedido, EstadoPedido destino) {
        Pedido pedido = obtener(idPedido);
        EstadoPedido anterior = pedido.estado();
        pedido.cambiarEstado(destino);
        repositorio.guardar(pedido);
        publicador.publicar(new PedidoCambioEstado(idPedido, anterior, destino));
    }

    /** Confirma el pedido, fija subtotal, descuento y total, y devuelve el resumen. */
    public ResumenCobro confirmarPedido(String idPedido) {
        Pedido pedido = obtener(idPedido);
        ResumenCobro resumen = calculador.calcular(pedido);
        pedido.confirmar(resumen);
        repositorio.guardar(pedido);
        return resumen;
    }

    public String crearPedido() {
        String id = String.format("P-%03d", ++secuencia);
        repositorio.guardar(new Pedido(id));
        return id;
    }

    public int agregarProducto(String idPedido, String productoId, List<Adicion> adiciones, int cantidad) {
        Pedido pedido = obtener(idPedido);
        ProductoPedido configurado = configurador.configurar(productoId, adiciones);
        int numero = pedido.agregarProducto(configurado, cantidad);
        repositorio.guardar(pedido);
        return numero;
    }

    public void modificarCantidad(String idPedido, int numeroLinea, int nuevaCantidad) {
        Pedido pedido = obtener(idPedido);
        pedido.modificarCantidad(numeroLinea, nuevaCantidad);
        repositorio.guardar(pedido);
    }

    public void retirarProducto(String idPedido, int numeroLinea) {
        Pedido pedido = obtener(idPedido);
        pedido.retirarProducto(numeroLinea);
        repositorio.guardar(pedido);
    }

    public Pedido consultarPedido(String idPedido) {
        return obtener(idPedido);
    }

    public List<Pedido> listarPedidos() {
        return repositorio.listar();
    }

    private Pedido obtener(String idPedido) {
        return repositorio.buscar(idPedido)
                .orElseThrow(() -> new ReglaNegocioException("No existe un pedido con el identificador " + idPedido + "."));
    }
}
