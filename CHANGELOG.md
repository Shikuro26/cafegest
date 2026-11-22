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
- Maven Wrapper (`mvnw`, `mvnw.cmd`): el proyecto compila sin Maven instalado.

### Cambiado
- La ventana gráfica reemplaza las vistas de registro por un tablero por estado.
- Las acciones no disponibles quedan apagadas y muestran el motivo al pasar el cursor.
- El alcance, la arquitectura y el diseño describen el estado entregado: patrones implementados, FlatLaf como biblioteca gráfica, `ReglasAdiciones` en `dominio.producto` y las 56 pruebas actuales.
- El diagrama de casos de uso se muestra desde el SVG del repositorio y el diagrama de clases general incluye `ConfiguracionCafeteria`, `Dinero` y `EstadoAcciones`.
- Las exclusiones del alcance explican también los combos paso a paso y el cierre de caja.

### Corregido
- Al consultar un pedido confirmado, el subtotal ya no aparece repetido.
- El documento de diseño incluye la descripción de cada patrón con palabras propias y su análisis de ventajas y desventajas, como pide el enunciado.
- `dependency-reduced-pom.xml` queda fuera del control de versiones por ser un residuo de la compilación.
