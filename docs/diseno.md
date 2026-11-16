# Decisiones de diseño de CaféGest

Este documento describe el diseño de personalización, promociones y actualización de vistas, implementado en el código. Las pruebas unitarias y de flujo verifican las reglas descritas en cada sección.

## Personalización de productos con Decorator

**Requisitos relacionados:** RF-05, RF-06 y RNF-03.

**Problema:** una bebida puede tener varias adiciones compatibles. Crear una clase para cada combinación, como CaféConLeche, CaféConLecheYShot o CaféConShot, multiplica las clases y repite cálculos cuando aparecen nuevas combinaciones.

**Decisión:** representar la personalización mediante Decorator, de categoría estructural. `ProductoPedido` define `precio()` y `descripcion()`. `ProductoBase` representa la selección del menú. `AdicionDecorator` implementa el mismo contrato y guarda un `ProductoPedido`; las adiciones concretas, como `ShotExtra` y `LecheVegetal`, delegan y agregan su costo y descripción.

**Ejemplo ilustrativo:** una bebida de $5.000 con un shot de $1.500 y leche vegetal de $1.000 cuesta $7.500. Estos importes son ejemplos de diseño, no un menú definitivo. La cantidad de la línea multiplica el precio de la configuración completa.

**Alternativa considerada:** una lista de adiciones dentro de una sola clase también permite sumar precios con menos clases. Se prefiere Decorator para expresar cada adición como una responsabilidad independiente y reutilizable. Si todas las adiciones terminan siendo solo datos sin comportamiento propio, se reevaluará esta decisión para evitar complejidad innecesaria.

**Consecuencias:** permite combinar responsabilidades sin una subclase por combinación, pero introduce varios objetos y hace necesario recorrer la cadena para inspeccionarla. La compatibilidad con el producto base y las reglas sobre repeticiones se validarán antes de construir la cadena; no dependerán del orden accidental de los envoltorios.

**SOLID:** favorece responsabilidad única y abierto/cerrado. Todos los decoradores deben respetar el contrato del componente y devolver precios no negativos. Se usará `BigDecimal`, no `double`, para los importes.

## Promociones intercambiables con Strategy

**Requisitos relacionados:** RF-07, RF-08, RNF-03 y RNF-05.

**Problema:** un cálculo central con un `if` por promoción crece cada vez que se agrega una regla de día u horario. Mezclar vigencia, porcentaje y selección dificulta comprobar qué descuento corresponde.

**Decisión:** usar Strategy, de categoría de comportamiento. `Promocion` define `calcularDescuento(contexto)`. Una implementación devuelve cero cuando no está vigente. Cada estrategia encapsula su condición y cálculo; `CalculadorTotal` evalúa las promociones registradas y aplica únicamente el mayor descuento, limitado al intervalo entre cero y el subtotal.

El contexto incluirá subtotal y fecha/hora local. Se inyectará un `Clock` con zona explícita para obtener el momento de confirmación y poder fijarlo en las pruebas. Después de confirmar se conservarán los importes, aunque cambie la hora o el estado del pedido.

**Alternativa considerada:** un `switch` por tipo de promoción es suficiente para reglas muy pequeñas y fijas, pero obliga a modificar el cálculo central al incorporar una nueva variante. Strategy permite agregar la implementación y registrarla sin cambiar ese servicio.

**Consecuencias:** facilita pruebas por regla y extensión, pero requiere mantener un registro de estrategias y definir una política de selección. Las promociones no se acumulan. Si hay empate, se conserva la primera del registro para que la selección sea determinista; el importe final será el mismo.

**SOLID:** cada estrategia tiene una responsabilidad y el calculador depende del contrato `Promocion`. El descuento se redondeará a dos decimales con `HALF_UP`, antes de comparar importes aplicables.

## Actualización de vistas con Observer

**Requisitos relacionados:** RF-09, RF-10 y RNF-02.

**Problema:** si el servicio de pedidos llama directamente a `VistaCaja` y `VistaCocina`, conoce detalles de interfaz y debe modificarse al agregar otra vista. También se corre el riesgo de avisar desde una interfaz y olvidarlo desde la otra.

**Decisión:** usar Observer, de categoría de comportamiento. `ServicioPedidos` valida la transición y publica `PedidoCambioEstado`, un evento inmutable con identificador, estado anterior y nuevo estado. `PublicadorEventosPedido` mantiene observadores del contrato `ObservadorPedido`; caja y cocina se suscriben a él.

Las únicas transiciones admitidas serán recibido a en preparación y en preparación a entregado. Una transición inválida no cambia el pedido ni emite el evento. Tanto consola como interfaz gráfica invocarán el mismo servicio.

**Alternativa considerada:** llamar directamente a ambas vistas es más sencillo inicialmente, pero acopla el negocio a sus receptores. Observer permite agregar o retirar receptores sin cambiar las reglas de estado.

**Consecuencias:** los avisos se producirán dentro de la misma ejecución local, sin correo ni mensajería externa. Se evitarán suscripciones duplicadas y se retirarán observadores al cerrar las vistas. Un error de un observador deberá reportarse y no impedirá notificar a los demás ni revertirá un cambio de estado ya válido. La vista gráfica trasladará sus actualizaciones al hilo de interfaz correspondiente.

**SOLID:** separa reglas de pedido y presentación, y hace depender la publicación de una abstracción. El flujo de avisos es indirecto, por lo que deberá comprobarse explícitamente.

## Límites del diseño

- Decorator cubre adiciones; Strategy cubre promociones; Observer cubre avisos. Se incluyen las categorías estructural y de comportamiento.
- No se incorporará Composite para una lista plana de líneas de pedido: el alcance actual no requiere grupos anidados tratados como una sola línea.
- No se añadirá un patrón solo por su nombre. Si el código demuestra que una solución más sencilla satisface el problema, se documentará el cambio de decisión.
- El menú, las compatibilidades y las promociones seguirán siendo configuración interna. Los pedidos permanecerán en memoria durante una sesión.

## Roles de los patrones en el código

| Patrón | Rol | Clase en CaféGest |
| --- | --- | --- |
| Decorator | Componente | `ProductoPedido` |
| Decorator | Componente concreto | `ProductoBase` |
| Decorator | Decorador base | `AdicionDecorator` |
| Decorator | Decoradores concretos | `ShotExtra`, `LecheVegetal` |
| Strategy | Estrategia | `Promocion` |
| Strategy | Estrategias concretas | `PromocionPorDia`, `PromocionPorHorario` |
| Strategy | Contexto | `CalculadorTotal` |
| Observer | Sujeto | `PublicadorEventosPedido` |
| Observer | Observador | `ObservadorPedido` |
| Observer | Observadores concretos | `VistaCaja`, `VistaCocina` |

Los diagramas de clases por patrón y los de secuencia están en [diagramas](diagramas.md).

## Comprobaciones

Cada comprobación corresponde a pruebas que se ejecutan con `mvn clean verify`.

1. **Decorator:** precio, descripción y cantidad de una combinación de adiciones, y rechazo de adiciones incompatibles o repetidas antes de construir la configuración. Clases: `ConfiguradorProductoTest`, `PedidoTest`.
2. **Strategy:** promoción vigente y fuera de horario, selección sin acumulación, empate, redondeo HALF_UP, descuento limitado al subtotal, y valores conservados al confirmar. Clases: `PromocionesTest`, `CalculadorTotalTest`, `PedidoTest`.
3. **Observer:** cada suscriptor recibe el aviso una vez, un receptor fallido no bloquea a los demás, y una transición inválida no cambia el estado ni emite evento. Clases: `PublicadorEventosPedidoTest`, `ServicioPedidosEstadoTest`.
4. **Consola y gráfica sobre el mismo núcleo:** la consola se prueba de extremo a extremo en `MenuConsolaFlujoTest`. La ventana gráfica no tiene pruebas automáticas de interacción; sus reglas de habilitación se prueban en `EstadoAccionesTest` y `EdicionLineasTest`.

## Conclusiones

- **Qué aprendimos:** los tres patrones resuelven problemas distintos. Decorator evita una clase por cada combinación de adiciones, Strategy aísla cada regla de descuento, y Observer desacopla el negocio de las vistas. Ninguno se aplicó solo por su nombre.
- **Qué costó:** Decorator obliga a recorrer la cadena para inspeccionar un producto, y Observer hace menos evidente el flujo de avisos. Ambos requirieron pruebas explícitas para no depender del orden de ejecución.
- **Qué cambiaríamos:** las reglas de compatibilidad de adiciones quedaron en `ReglasAdiciones`, dentro del paquete del dominio de productos, y no en el de catálogo como se planeó al inicio. Lo dejamos así para evitar una dependencia cíclica entre paquetes. Sí cambiaríamos el menú y las promociones para cargarlos desde un archivo de configuración, en lugar de código, para no tener que recompilar al cambiar precios.
- **Limitación conocida:** la ventana gráfica se verificó construyéndola y revisando imágenes de sus componentes, pero no tiene pruebas automáticas de interacción.
