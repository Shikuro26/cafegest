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

Los datos se mantendrán en memoria en una sola ejecución local, sin sincronización entre equipos. Menú, adiciones compatibles y promociones se definirán mediante configuración interna, sin pantalla de administración.

## Exclusiones y justificación

| Exclusión | Justificación |
| --- | --- |
| Pagos reales y facturación electrónica | Requieren servicios externos y requisitos adicionales. |
| Domicilios y GPS | Amplían el proyecto hacia logística y mapas. |
| Inventario de ingredientes y proveedores | Corresponden a un módulo diferente. |
| Web, móvil, cuentas y roles | La primera versión será local y de escritorio. |
| Base de datos y conservación entre sesiones | Se usará memoria para concentrarse en las reglas y su extensibilidad. |

## Áreas con variación frecuente

1. Productos: nuevas categorías de bebidas y alimentos.
2. Adiciones: distintas combinaciones, compatibilidad y precios.
3. Promociones: reglas por día u horario.
4. Receptores de avisos: futuras vistas adicionales a caja y cocina.

Estas áreas servirán para identificar problemas de diseño y justificar patrones en la siguiente etapa. Los patrones aún no se han seleccionado ni implementado.
