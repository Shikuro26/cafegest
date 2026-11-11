# Decisiones de diseño de CaféGest

Este documento define el diseño previsto para personalización, promociones y actualización de vistas. Las clases y pruebas descritas todavía no están implementadas. Las decisiones se revisarán al desarrollar los flujos reales.

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

## Comprobaciones previstas

1. Producto base y combinación de dos adiciones: precio, descripción y cantidad correctos; rechazo de adiciones incompatibles antes de construir la configuración.
2. Promoción vigente y fuera de horario, varias promociones sin acumulación, empate, redondeo y límites del descuento; valores conservados después de confirmar.
3. Transición válida: caja y cocina reciben un aviso cada una. Transición inválida: estado intacto y ningún aviso. Un receptor fallido no bloquea al otro.
4. La consola y la interfaz gráfica obtienen los mismos resultados al usar los mismos servicios de negocio.

Estas comprobaciones son criterios para la implementación posterior; no constituyen resultados de pruebas ejecutadas.
