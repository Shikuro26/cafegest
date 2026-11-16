# Historial de cambios

Todas las versiones notables de CaféGest se documentan en este archivo.

## [0.1.0-SNAPSHOT] — sin publicar

### Agregado
- Catálogo de menú con categoría, precio en `BigDecimal` y disponibilidad.
- Adiciones compatibles (shot extra y leche vegetal) con patrón Decorator; no se repiten dentro de un producto.
- Pedidos con identificador de sesión, líneas, cantidades enteras positivas, subtotal y confirmación.
- Promociones por día y por horario con patrón Strategy; se aplica solo la de mayor descuento, con redondeo HALF_UP.
- Estados recibido, en preparación y entregado, con transiciones válidas únicamente.
- Avisos a caja y cocina mediante patrón Observer; un observador fallido no impide avisar a los demás.
- Consola con todas las operaciones del alcance.
- Interfaz gráfica Swing con pasos del pedido, detalle, tablero por estado, menú completo y edición de líneas.
- Formato de importes sin decimales cuando son enteros.

### Cambiado
- La ventana gráfica reemplaza las vistas de registro por un tablero por estado.
- Las acciones no disponibles quedan apagadas y muestran el motivo al pasar el cursor.

### Corregido
- Al consultar un pedido confirmado, el subtotal ya no aparece repetido.
