package cafegest.dominio.pedido;

import cafegest.dominio.ReglaNegocioException;
import cafegest.dominio.producto.ProductoPedido;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Agregado del pedido: protege sus líneas, su estado y el resumen confirmado. */
public final class Pedido {
    private final String id;
    private final List<LineaPedido> lineas = new ArrayList<>();
    private int ultimoNumeroLinea;
    private EstadoPedido estado = EstadoPedido.RECIBIDO;
    private ResumenCobro resumen;

    public Pedido(String id) {
        this.id = Objects.requireNonNull(id, "El identificador del pedido es obligatorio.");
    }

    public String id() {
        return id;
    }

    public EstadoPedido estado() {
        return estado;
    }

    /** Vista de solo lectura de las líneas. */
    public List<LineaPedido> lineas() {
        return List.copyOf(lineas);
    }

    public Optional<ResumenCobro> resumen() {
        return Optional.ofNullable(resumen);
    }

    public boolean confirmado() {
        return resumen != null;
    }

    public BigDecimal subtotal() {
        return lineas.stream()
                .map(LineaPedido::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Agrega una unidad configurada y devuelve el número de línea asignado. */
    public int agregarProducto(ProductoPedido producto, int cantidad) {
        exigirEditable();
        Validaciones.cantidad(cantidad);
        int numero = ++ultimoNumeroLinea;
        lineas.add(new LineaPedido(numero, producto, cantidad));
        return numero;
    }

    public void modificarCantidad(int numeroLinea, int nuevaCantidad) {
        exigirEditable();
        Validaciones.cantidad(nuevaCantidad);
        int indice = indiceDe(numeroLinea);
        lineas.set(indice, lineas.get(indice).conCantidad(nuevaCantidad));
    }

    public void retirarProducto(int numeroLinea) {
        exigirEditable();
        lineas.remove(indiceDe(numeroLinea));
    }

    /** Fija los importes definitivos. Rechaza pedidos vacíos y confirmaciones repetidas. */
    public void confirmar(ResumenCobro nuevoResumen) {
        Objects.requireNonNull(nuevoResumen, "El resumen de cobro es obligatorio.");
        if (estado != EstadoPedido.RECIBIDO) {
            throw new ReglaNegocioException("Solo se pueden confirmar pedidos en estado recibido.");
        }
        if (confirmado()) {
            throw new ReglaNegocioException("El pedido " + id + " ya fue confirmado.");
        }
        if (lineas.isEmpty()) {
            throw new ReglaNegocioException(
                    "El pedido está vacío. Agregue al menos un producto antes de confirmarlo.");
        }
        if (nuevoResumen.subtotal().compareTo(subtotal()) != 0) {
            throw new ReglaNegocioException("El resumen de cobro no corresponde a los productos del pedido.");
        }
        this.resumen = nuevoResumen;
    }

    private void exigirEditable() {
        if (estado != EstadoPedido.RECIBIDO) {
            throw new ReglaNegocioException("Solo se pueden modificar pedidos en estado recibido.");
        }
        if (confirmado()) {
            throw new ReglaNegocioException("El pedido " + id + " ya fue confirmado y no admite cambios.");
        }
    }

    private int indiceDe(int numeroLinea) {
        for (int i = 0; i < lineas.size(); i++) {
            if (lineas.get(i).numero() == numeroLinea) {
                return i;
            }
        }
        throw new ReglaNegocioException("El pedido no tiene una línea número " + numeroLinea + ".");
    }
}
