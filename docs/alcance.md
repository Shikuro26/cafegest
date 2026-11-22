# Alcance de CaféGest

Integrante: Andrés Felipe Camacho Ángel.

## Objetivo

Gestionar pedidos de una cafetería en Java mediante consola e interfaz gráfica sencilla que compartan el mismo núcleo de negocio.

## Funcionalidades incluidas

- Menú de bebidas y alimentos con precios y disponibilidad.
- Creación y consulta de pedidos; modificación de cantidades y retiro de productos antes de iniciar su preparación.
- Adiciones compatibles que cambien la descripción y el precio del producto.
- Cálculo de subtotal, descuento y total.
- Promociones por día u horario, sin acumulación: se aplica la de mayor descuento.
- Estados recibido, en preparación y entregado.
- Actualización de las vistas de caja y cocina después de cada cambio de estado.
- Consulta de pedidos de la sesión actual.

Los datos se mantienen en memoria en una sola ejecución local, sin sincronización entre equipos. Menú, adiciones compatibles y promociones son configuración interna, sin pantalla de administración.

## Exclusiones y justificación

Se eligió la propuesta 3 (cafetería o restaurante) y se acotó a gestión de pedidos con productos personalizables, promociones y avisos de preparación.

| Exclusión | Justificación |
| --- | --- |
| Pagos reales y facturación electrónica | Requieren servicios externos y requisitos adicionales. |
| Domicilios y GPS | Amplían el proyecto hacia logística y mapas. |
| Inventario de ingredientes y proveedores | Corresponden a un módulo diferente. |
| Web, móvil, cuentas y roles | La primera versión será local y de escritorio. |
| Base de datos y conservación entre sesiones | Se usará memoria para concentrarse en las reglas y su extensibilidad. |
| Combos armados paso a paso | Duplican la personalización que ya resuelve Decorator; se priorizaron las adiciones sobre un producto. |
| Cierre de caja y reporte de ventas | Aportan poco a las áreas con variación y exigen un módulo de consultas aparte. |

## Áreas con variación frecuente

1. Productos: nuevas categorías de bebidas y alimentos.
2. Adiciones: distintas combinaciones, compatibilidad y precios.
3. Promociones: reglas por día u horario.
4. Receptores de avisos: futuras vistas adicionales a caja y cocina.

Estas áreas se usaron para identificar los problemas de diseño y justificar los patrones. La versión entregada implementa tres patrones, uno por cada una de las tres primeras áreas, y deja preparada la cuarta: Decorator para las adiciones, Strategy para las promociones y Observer para los receptores de avisos. El detalle está en [decisiones de diseño](diseno.md).
