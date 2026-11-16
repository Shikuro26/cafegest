package cafegest.dominio.pedido;

import cafegest.dominio.producto.ProductoPedido;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Línea de un pedido: una configuración de producto con una cantidad entera positiva. */
public record LineaPedido(int numero, ProductoPedido producto, int cantidad) {

    public LineaPedido {
        Validaciones.cantidad(cantidad);
    }

    public LineaPedido conCantidad(int nuevaCantidad) {
        return new LineaPedido(numero, producto, nuevaCantidad);
    }

    public BigDecimal subtotal() {
        return producto.precio()
                .multiply(BigDecimal.valueOf(cantidad))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
