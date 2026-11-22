# Decisiones de diseño de CaféGest

Este documento describe el diseño de personalización, promociones y actualización de vistas, implementado en el código. Las pruebas unitarias y de flujo verifican las reglas descritas en cada sección.

## Personalización de productos con Decorator

**Requisitos relacionados:** RF-05, RF-06 y RNF-03.

**Problema:** una bebida puede tener varias adiciones compatibles. Crear una clase para cada combinación, como CaféConLeche, CaféConLecheYShot o CaféConShot, multiplica las clases y repite cálculos cuando aparecen nuevas combinaciones.

**Decisión:** representar la personalización mediante Decorator, de categoría estructural. `ProductoPedido` define `precio()` y `descripcion()`. `ProductoBase` representa la selección del menú. `AdicionDecorator` implementa el mismo contrato y guarda un `ProductoPedido`; las adiciones concretas, como `ShotExtra` y `LecheVegetal`, delegan y agregan su costo y descripción.

**El patrón en palabras propias:** Decorator permite agregar comportamiento a un objeto envolviéndolo en otro objeto del mismo tipo. El envoltorio recibe la llamada, hace su aporte y delega en el objeto envuelto. Como todos comparten el mismo contrato, quien usa el producto no distingue si está hablando con la base o con tres capas de adiciones. Aplicado aquí: `ShotExtra` envuelve el café y suma su precio, luego `LecheVegetal` envuelve ese resultado y suma otra descripción. La cadena se construye una sola vez en `ConfiguradorProducto.configurar` y no cambia después.

**Ejemplo ilustrativo:** una bebida de $5.000 con un shot de $1.500 y leche vegetal de $1.000 cuesta $7.500. Estos importes son ejemplos de diseño, no un menú definitivo. La cantidad de la línea multiplica el precio de la configuración completa.

**Alternativa considerada:** una lista de adiciones dentro de una sola clase también permite sumar precios con menos clases. Se prefiere Decorator para expresar cada adición como una responsabilidad independiente y reutilizable. Si todas las adiciones terminan siendo solo datos sin comportamiento propio, se reevaluará esta decisión para evitar complejidad innecesaria.

**Consecuencias:** permite combinar responsabilidades sin una subclase por combinación, pero introduce varios objetos y hace necesario recorrer la cadena para inspeccionarla. La compatibilidad con el producto base y las reglas sobre repeticiones se validarán antes de construir la cadena; no dependerán del orden accidental de los envoltorios.

**Ventajas y desventajas:** entre las ventajas, una adición nueva es una clase nueva más una entrada en el enum `Adicion`, sin tocar `Pedido`, `LineaPedido` ni las interfaces; las combinaciones son ilimitadas y la cantidad de código crece de forma lineal, no exponencial; cada adición se prueba por separado. Entre las desventajas, el objeto final es una cadena y para obtener el detalle de qué adiciones tiene hay que recorrerla o exponer `adicion()` como hace `AdicionDecorator`; además, cualquier adición con reglas de orden o de exclusión mutua compleja exigiría validaciones externas, que aquí están en `ReglasAdiciones`.

**SOLID:** favorece responsabilidad única y abierto/cerrado. Todos los decoradores deben respetar el contrato del componente y devolver precios no negativos. Se usará `BigDecimal`, no `double`, para los importes.

## Promociones intercambiables con Strategy

**Requisitos relacionados:** RF-07, RF-08, RNF-03 y RNF-05.

**Problema:** un cálculo central con un `if` por promoción crece cada vez que se agrega una regla de día u horario. Mezclar vigencia, porcentaje y selección dificulta comprobar qué descuento corresponde.

**Decisión:** usar Strategy, de categoría de comportamiento. `Promocion` define `calcularDescuento(contexto)`. Una implementación devuelve cero cuando no está vigente. Cada estrategia encapsula su condición y cálculo; `CalculadorTotal` evalúa las promociones registradas y aplica únicamente el mayor descuento, limitado al intervalo entre cero y el subtotal.

**El patrón en palabras propias:** Strategy separa una familia de algoritmos intercambiables detrás de un contrato común, para que el objeto que los usa elija uno en tiempo de ejecución sin conocer sus detalles. Aquí el algoritmo es "cuánto descuento corresponde"; las variantes son el día de la semana y la franja horaria. `CalculadorTotal` actúa como contexto: recibe la lista de estrategias y, para cada una, pide su descuento con el mismo `ContextoPromocion`. La estrategia que no aplica devuelve cero en lugar de lanzar un error, así el contexto puede evaluarlas todas sin condiciones especiales.

**Ejemplo ilustrativo:** un martes a las 16:00 con un subtotal de $10.000 recibe 10 % por "Martes de café" y 15 % por "Hora feliz"; se aplica solo el 15 %, es decir $1.500 de descuento y $8.500 de total. Las reglas no se suman.

El contexto incluye subtotal y fecha/hora local. Se inyecta un `Clock` con zona explícita para obtener el momento de confirmación y poder fijarlo en las pruebas (`CalculadorTotalTest` usa un reloj fijo). Después de confirmar se conservan los importes, aunque cambie la hora o el estado del pedido.

**Alternativa considerada:** un `switch` por tipo de promoción es suficiente para reglas muy pequeñas y fijas, pero obliga a modificar el cálculo central al incorporar una nueva variante. Strategy permite agregar la implementación y registrarla sin cambiar ese servicio.

**Consecuencias:** facilita pruebas por regla y extensión, pero requiere mantener un registro de estrategias y definir una política de selección. Las promociones no se acumulan. Si hay empate, se conserva la primera del registro para que la selección sea determinista; el importe final será el mismo.

**Ventajas y desventajas:** entre las ventajas, agregar "Jueves de postre" o un cupón es escribir una clase nueva y añadirla a `promocionesPorDefecto`; cada regla se prueba con un `Clock` fijo y sin tocar las demás; la política de "solo la mayor" vive en un único lugar y no se repite. Entre las desventajas, si las promociones fueran acumulables el contexto necesitaría otro diseño; además, la comparación de importes dentro de `CalculadorTotal` sigue siendo una decisión central, así que un cambio de política (por ejemplo, sumar en lugar de elegir la mayor) sí obliga a modificar esa clase.

**SOLID:** cada estrategia tiene una responsabilidad y el calculador depende del contrato `Promocion`. El descuento se redondeará a dos decimales con `HALF_UP`, antes de comparar importes aplicables.

## Actualización de vistas con Observer

**Requisitos relacionados:** RF-09, RF-10 y RNF-02.

**Problema:** si el servicio de pedidos llama directamente a `VistaCaja` y `VistaCocina`, conoce detalles de interfaz y debe modificarse al agregar otra vista. También se corre el riesgo de avisar desde una interfaz y olvidarlo desde la otra.

**Decisión:** usar Observer, de categoría de comportamiento. `ServicioPedidos` valida la transición y publica `PedidoCambioEstado`, un evento inmutable con identificador, estado anterior y nuevo estado. `PublicadorEventosPedido` mantiene observadores del contrato `ObservadorPedido`; caja y cocina se suscriben a él.

**El patrón en palabras propias:** Observer define una relación uno-a-muchos donde un sujeto avisa automáticamente a todos los interesados cuando cambia su estado, sin saber quiénes son ni qué harán con el aviso. El sujeto solo conoce el contrato `ObservadorPedido`; cada observador decide qué mostrar. Aquí el sujeto es `PublicadorEventosPedido`, el aviso es el record `PedidoCambioEstado` y los observadores concretos son `VistaCaja` y `VistaCocina` en consola, y en la ventana un observador anónimo que refresca el tablero. El servicio publica solo después de guardar una transición válida, por lo que un error no genera avisos falsos.

**Ejemplo ilustrativo:** al pasar el pedido P-001 de recibido a en preparación, se imprime `[Caja] Pedido P-001: RECIBIDO -> EN_PREPARACION` y `[Cocina] Preparar pedido P-001`. La ventana, suscrita al mismo publicador, actualiza sus columnas sin que `ServicioPedidos` sepa que existe una ventana.

Las únicas transiciones admitidas son recibido a en preparación y en preparación a entregado. Una transición inválida no cambia el pedido ni emite el evento. Tanto consola como interfaz gráfica invocan el mismo servicio.

**Alternativa considerada:** llamar directamente a ambas vistas es más sencillo inicialmente, pero acopla el negocio a sus receptores. Observer permite agregar o retirar receptores sin cambiar las reglas de estado.

**Consecuencias:** los avisos se producen dentro de la misma ejecución local, sin correo ni mensajería externa. Se evitan suscripciones duplicadas y se pueden retirar observadores al cerrar las vistas. Un error de un observador se reporta y no impide notificar a los demás ni revierte un cambio de estado ya válido. La vista gráfica traslada sus actualizaciones al hilo de interfaz correspondiente con `SwingUtilities.invokeLater`.

**Ventajas y desventajas:** entre las ventajas, añadir un receptor nuevo (por ejemplo, una vista de mesero o un registro de auditoría) es suscribir un objeto y no tocar `ServicioPedidos`; consola y ventana comparten el mismo mecanismo sin ramas por interfaz; el fallo de un observador queda aislado por el manejador de errores. Entre las desventajas, el flujo es indirecto y más difícil de seguir al depurar, el orden de notificación depende de la lista de suscripción y no hay garantía transaccional: si un observador falla, el cambio de estado ya quedó guardado.

**SOLID:** separa reglas de pedido y presentación, y hace depender la publicación de una abstracción. El flujo de avisos es indirecto, por lo que se comprueba explícitamente con pruebas.

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
| Observer | Observadores concretos | `VistaCaja`, `VistaCocina` (consola) y el observador anónimo de `VentanaPrincipal` (gráfica) |

Los diagramas de clases por patrón y los de secuencia están en [diagramas](diagramas.md).

## Comprobaciones

Cada comprobación corresponde a pruebas que se ejecutan con `mvn clean verify`. En total son 56 pruebas en 12 clases, todas en verde.

1. **Decorator:** precio, descripción y cantidad de una combinación de adiciones, y rechazo de adiciones incompatibles o repetidas antes de construir la configuración. Clases: `ConfiguradorProductoTest` (5), `PedidoTest` (7) y `ServicioPedidosTest` (5).
2. **Strategy:** promoción vigente y fuera de horario, selección sin acumulación, empate, redondeo HALF_UP, descuento limitado al subtotal, y valores conservados al confirmar. Clases: `PromocionesTest` (4), `CalculadorTotalTest` (6) y `PedidoTest`.
3. **Observer:** cada suscriptor recibe el aviso una vez, un receptor fallido no bloquea a los demás, y una transición inválida no cambia el estado ni emite evento. Clases: `PublicadorEventosPedidoTest` (3), `ServicioPedidosEstadoTest` (4).
4. **Catálogo y pedido:** disponibilidad, identificadores repetidos y precios negativos (`CatalogoMenuTest`, 6); líneas, cantidades y bloqueo tras confirmar (`PedidoTest`).
5. **Consola y gráfica sobre el mismo núcleo:** la consola se prueba de extremo a extremo en `MenuConsolaFlujoTest` (4) y sus entradas inválidas en `MenuConsolaTest` (3). La ventana gráfica no tiene pruebas automáticas de interacción; sus reglas de habilitación y edición se prueban en `EstadoAccionesTest` (6) y `EdicionLineasTest` (3).

## Conclusiones

- **Qué aprendimos:** los tres patrones resuelven problemas distintos. Decorator evita una clase por cada combinación de adiciones, Strategy aísla cada regla de descuento, y Observer desacopla el negocio de las vistas. Ninguno se aplicó solo por su nombre.
- **Qué costó:** Decorator obliga a recorrer la cadena para inspeccionar un producto, y Observer hace menos evidente el flujo de avisos. Ambos requirieron pruebas explícitas para no depender del orden de ejecución.
- **Qué cambiaríamos:** las reglas de compatibilidad de adiciones quedaron en `ReglasAdiciones`, dentro del paquete del dominio de productos, y no en el de catálogo como se planeó al inicio. Lo dejamos así para evitar una dependencia cíclica entre paquetes. Sí cambiaríamos el menú y las promociones para cargarlos desde un archivo de configuración, en lugar de código, para no tener que recompilar al cambiar precios.
- **Limitación conocida:** la ventana gráfica se verificó construyéndola y revisando imágenes de sus componentes, pero no tiene pruebas automáticas de interacción.
