# Arquitectura y modelo de CaféGest

Modelo del sistema implementado. Este documento desarrolla las decisiones de [diseño](diseno.md) y los [requisitos](requisitos.md). Los paquetes y clases de la tabla siguiente existen en el código; los diagramas de clases son una vista general y pueden omitir detalles de visibilidad y constructores.

## Separación de responsabilidades

La consola y la interfaz gráfica usan las mismas instancias de servicios y el mismo almacenamiento en memoria dentro de una ejecución. Las interfaces recogen entradas y presentan resultados; los servicios validan operaciones y coordinan el dominio. El dominio no conoce ventanas, menús de consola ni componentes gráficos.

```mermaid
flowchart TB
    Consola[Interfaz de consola] --> Aplicacion[Servicios de aplicacion]
    Grafica[Interfaz grafica] --> Aplicacion
    Aplicacion --> Dominio[Dominio y reglas de negocio]
    Aplicacion --> Puerto[Contrato RepositorioPedidos]
    Memoria[RepositorioPedidosMemoria] -. implementa .-> Puerto
    Inicio[Configuracion e inicio] --> Consola
    Inicio --> Grafica
    Inicio --> Aplicacion
    Inicio --> Memoria
    Aplicacion --> Eventos[Publicador de eventos de pedido]
    Eventos --> Caja[Vista de caja]
    Eventos --> Cocina[Vista de cocina]
```

Las flechas continuas de las interfaces hacia los servicios representan llamadas. Las flechas del publicador hacia las vistas representan notificaciones mediante el contrato `ObservadorPedido`, no dependencias hacia clases gráficas concretas. Las vistas de caja y cocina pertenecen a la presentación. La consola puede usar observadores que impriman avisos; la interfaz gráfica puede usar otros que actualicen componentes.

## Paquetes

| Paquete base `cafegest` | Responsabilidad y clases principales |
| --- | --- |
| `dominio.catalogo` | `ProductoMenu`, `CatalogoMenu`, `Categoria`: precios, disponibilidad y categorías del menú. |
| `dominio.producto` | `ProductoPedido`, `ProductoBase`, `AdicionDecorator`, `ShotExtra`, `LecheVegetal`, `ReglasAdiciones`: configuración personalizada de una unidad y compatibilidad de adiciones. |
| `dominio.pedido` | `Pedido`, `LineaPedido`, `EstadoPedido`, `ResumenCobro`: líneas, cantidades, estado e importes confirmados. |
| `dominio.promocion` | `Promocion`, `ContextoPromocion`, `PromocionPorDia`, `PromocionPorHorario`: estrategias de descuento. |
| `aplicacion` | `ServicioPedidos`, `CalculadorTotal`, `ConfiguradorProducto`: casos de uso y coordinación. |
| `aplicacion.puerto` | `RepositorioPedidos`, `ObservadorPedido`: contratos independientes de almacenamiento e interfaz. |
| `aplicacion.evento` | `PedidoCambioEstado`, `PublicadorEventosPedido`: avisos locales del pedido. |
| `infraestructura.memoria` | `RepositorioPedidosMemoria`: almacenamiento por identificador durante la sesión. |
| `interfaz.consola` | `MenuConsola`, `VistaCaja`, `VistaCocina`: entrada, salida y avisos de texto. |
| `interfaz.grafica` | `VentanaPrincipal`, `DialogoAgregarProducto`, `DialogoMenu`, `DetallePedido`, `EstadoAcciones`, `BotonCafe`, `ChipAdicion`, `ColumnaEstado`, `ImagenProducto`, `TarjetaRedondeada`, `EstiloControles`, `TemaCafe`: ventana Swing, tablero por estado, menú de tarjetas y reglas de habilitación de acciones. |
| `interfaz` | `Dinero`: formato de importes compartido por consola y ventana. |
| `configuracion` | `AplicacionCafeGest`, `ConfiguracionCafeteria`: punto de entrada, construcción e inyección de servicios, reloj, repositorio, estrategias y observadores. |

No hay dependencias del dominio hacia `interfaz` o `infraestructura`. `RepositorioPedidosMemoria` implementa el puerto y no contiene reglas sobre descuentos o estados. Se usa Java 21: la ventana gráfica está construida con Swing y el tema FlatLaf.

## Responsabilidades y reglas del modelo

- **ProductoMenu:** dato del catálogo con identificador, categoría, precio base y disponibilidad. Cambiar el catálogo no alterará retroactivamente los importes de un pedido confirmado.
- **ConfiguradorProducto:** comprueba disponibilidad y compatibilidad y crea `ProductoBase` con sus decoradores. La validación sucede antes de cambiar la línea del pedido; una configuración inválida no deja cambios parciales.
- **ProductoPedido:** contrato para consultar precio unitario y descripción. `ProductoBase` conserva los datos necesarios de la selección; un decorador guarda un componente del mismo contrato y añade una responsabilidad.
- **LineaPedido:** guarda un `ProductoPedido` y una cantidad entera positiva. Calcula su subtotal como precio unitario por cantidad. Modificarla requiere pasar por el agregado `Pedido`.
- **Pedido:** protege sus líneas y su estado. Solo permite editar mientras esté recibido y todavía no confirmado. `confirmar` rechaza un pedido vacío y fija un `ResumenCobro` inmutable. Un pedido confirmado permanece recibido hasta comenzar la preparación, pero ya no puede editarse: es una restricción adicional dentro del estado recibido, que satisface RF-04.
- **ResumenCobro:** conserva subtotal, descuento y total definitivos, usando `BigDecimal`. La consulta posterior no recalcula promociones según la hora actual.
- **CalculadorTotal:** crea el contexto con un instante de confirmación obtenido de un `Clock` inyectado, calcula las estrategias registradas, elige el mayor descuento sin acumular y limita el total para que no sea negativo. Usa dos decimales y `HALF_UP`.
- **ServicioPedidos:** ofrece crear, agregar, modificar, retirar, confirmar, consultar y cambiar estado. Obtiene y guarda el agregado mediante `RepositorioPedidos`, devuelve datos para presentación y no expone colecciones mutables del dominio.
- **EstadoPedido:** enumeración `RECIBIDO`, `EN_PREPARACION`, `ENTREGADO`. Se necesita confirmación antes de iniciar preparación. Solo se permite avanzar al siguiente estado; confirmar no es un cuarto estado.
- **PublicadorEventosPedido:** notifica a los observadores después de guardar una transición válida. Un rechazo no genera eventos. Los errores de un receptor se reportan y no evitan avisar a los demás. No se añaden correo, SMS ni sincronización entre equipos.

## Casos de uso

**Actor humano principal:** operador de la cafetería. Caja y cocina son funciones del personal, no roles de seguridad: esta versión no incorpora cuentas ni permisos. Los clientes del local no operan directamente la aplicación. Las vistas y los servicios internos no son actores externos.

| Caso | Acción y resultado observable | Requisitos |
| --- | --- | --- |
| CU-01 Consultar menú | Ver productos con categoría, precio y disponibilidad. | RF-01 |
| CU-02 Crear pedido | Obtener identificador único de sesión y estado recibido. | RF-02 |
| CU-03 Editar pedido | Agregar, retirar, cambiar cantidades y elegir adiciones compatibles antes de confirmar. Entradas inválidas conservan el pedido anterior. | RF-03, RF-04, RF-05 |
| CU-04 Confirmar pedido | Validar que no esté vacío, fijar precios, aplicar la mejor promoción vigente y mostrar el resumen. | RF-06, RF-07, RF-08, RF-12 |
| CU-05 Consultar pedidos | Ver lista y detalle de líneas, cantidades, valores y estado. | RF-11 |
| CU-06 Cambiar estado | Avanzar un pedido confirmado; avisar a caja y cocina tras una transición válida. | RF-09, RF-10 |

### Diagrama de casos de uso

Diagrama incluido en el repositorio como imagen editable: [casos-de-uso.svg](casos-de-uso.svg). La fuente PlantUML equivalente está en [casos-de-uso.puml](casos-de-uso.puml). Las operaciones internas de sumar importes o guardar datos no se modelan como objetivos separados del actor.

![Diagrama de casos de uso de CaféGest](casos-de-uso.svg)


### Flujo principal de confirmación

**Precondición:** existe un pedido recibido y no confirmado.

1. El operador solicita confirmar el pedido desde consola o interfaz gráfica.
2. `ServicioPedidos` obtiene el pedido y comprueba que tenga líneas y una configuración válida.
3. `CalculadorTotal` calcula el subtotal y evalúa las promociones con una sola fecha/hora de confirmación.
4. El servicio entrega el resumen al pedido, que lo conserva y bloquea futuras ediciones.
5. Se guarda el pedido y se presenta subtotal, descuento y total. El estado continúa recibido.

**Alternativas:** un pedido vacío se rechaza sin cambios; no hay promociones vigentes y el descuento es cero; hay varias y se elige solo el mayor descuento; un pedido ya confirmado se rechaza sin recalcular sus importes. El cambio a preparación se realiza después mediante CU-06.

## Diagrama de clases general

El diagrama presenta contratos, relaciones y operaciones clave. Los tipos auxiliares `ContextoPromocion`, `EstadoPedido` y `PedidoCambioEstado` se describen en el texto para mantener legible esta vista. Las multiplicidades reflejan una configuración por línea, varias líneas por pedido y varios observadores por publicador.

```mermaid
classDiagram
    class ProductoPedido {
        <<interface>>
        +precio() BigDecimal
        +descripcion() String
    }
    class ProductoBase
    class AdicionDecorator {
        <<abstract>>
        -envuelto: ProductoPedido
    }
    class ShotExtra
    class LecheVegetal
    ProductoPedido <|.. ProductoBase
    ProductoPedido <|.. AdicionDecorator
    AdicionDecorator <|-- ShotExtra
    AdicionDecorator <|-- LecheVegetal
    AdicionDecorator "1" --> "1" ProductoPedido : envuelve

    class LineaPedido {
        -cantidad: int
        +subtotal() BigDecimal
    }
    class Pedido {
        -id: String
        -estado: EstadoPedido
        -confirmado: boolean
        +subtotal() BigDecimal
        +confirmar(resumen)
        +cambiarEstado(nuevoEstado)
    }
    class ResumenCobro {
        -subtotal: BigDecimal
        -descuento: BigDecimal
        -total: BigDecimal
    }
    Pedido "1" *-- "0..*" LineaPedido : contiene
    LineaPedido "1" --> "1" ProductoPedido : configuracion
    Pedido "1" *-- "0..1" ResumenCobro : conserva

    class Promocion {
        <<interface>>
        +calcularDescuento(contexto) BigDecimal
    }
    class PromocionPorDia
    class PromocionPorHorario
    class CalculadorTotal {
        +calcular(pedido) ResumenCobro
    }
    Promocion <|.. PromocionPorDia
    Promocion <|.. PromocionPorHorario
    CalculadorTotal "1" --> "0..*" Promocion : evalua
    CalculadorTotal ..> Pedido
    CalculadorTotal ..> ResumenCobro

    class RepositorioPedidos {
        <<interface>>
        +guardar(pedido)
        +buscar(id) Pedido
        +listar()
    }
    class RepositorioPedidosMemoria
    RepositorioPedidos <|.. RepositorioPedidosMemoria
    RepositorioPedidosMemoria "1" o-- "0..*" Pedido : almacena
    class ServicioPedidos {
        +crearPedido()
        +agregarProducto(id, seleccion)
        +modificarCantidad(id, linea, cantidad)
        +retirarProducto(id, linea)
        +confirmarPedido(id)
        +consultarPedido(id)
        +cambiarEstado(id, nuevoEstado)
    }
    class ConfiguradorProducto {
        +configurar(seleccion) ProductoPedido
    }
    ServicioPedidos --> RepositorioPedidos
    ServicioPedidos --> CalculadorTotal
    ServicioPedidos --> ConfiguradorProducto
    ServicioPedidos ..> Pedido
    ConfiguradorProducto ..> ProductoPedido

    class ObservadorPedido {
        <<interface>>
        +actualizar(evento)
    }
    class PublicadorEventosPedido {
        +suscribir(observador)
        +retirar(observador)
        +publicar(evento)
    }
    class VistaCaja
    class VistaCocina
    ObservadorPedido <|.. VistaCaja
    ObservadorPedido <|.. VistaCocina
    PublicadorEventosPedido "1" --> "0..*" ObservadorPedido : notifica
    ServicioPedidos --> PublicadorEventosPedido

    class Dinero {
        <<utility>>
        +formatear(importe)$
    }
    class ConfiguracionCafeteria {
        <<utility>>
        +catalogoPorDefecto()$
        +promocionesPorDefecto()$
        +relojLocal()$
        +crearServicio(...)$
    }
    class AplicacionCafeGest {
        <<utility>>
        +main(args)$
    }
    class EstadoAcciones {
        <<utility>>
        +pasoActual(pedido)$
        +agregarProducto(pedido)$
        +confirmar(pedido)$
        +editarLineas(pedido)$
        +pasarAPreparacion(pedido)$
        +marcarEntregado(pedido)$
    }

    class MenuConsola
    class VentanaPrincipal
    MenuConsola --> ServicioPedidos
    VentanaPrincipal --> ServicioPedidos
    VentanaPrincipal ..> EstadoAcciones : habilita acciones
    MenuConsola ..> Dinero
    VentanaPrincipal ..> Dinero
    AplicacionCafeGest ..> MenuConsola : inicia
    AplicacionCafeGest ..> VentanaPrincipal : inicia
    ConfiguracionCafeteria ..> ServicioPedidos : construye
```

**Roles de patrones:** `ProductoPedido` es el componente; `ProductoBase`, el componente concreto; `AdicionDecorator`, el decorador base; `ShotExtra` y `LecheVegetal`, decoradores concretos. `Promocion` es la estrategia y `CalculadorTotal` su contexto. `PublicadorEventosPedido` es el sujeto y `ObservadorPedido` el contrato de los observadores.

## Límites del diseño

Las mutaciones pasan por `ServicioPedidos` y las reglas están protegidas por `Pedido`. Una clase gráfica no calcula descuentos ni fuerza estados. `ConfiguracionCafeteria` construye el catálogo, las promociones, el reloj y el servicio; `AplicacionCafeGest` elige consola o ventana. Los diagramas por patrón y los de secuencia están en [diagramas](diagramas.md).

Las pruebas cubren el catálogo, las adiciones, las reglas del pedido, promociones, transiciones, avisos, el flujo completo de consola y las reglas de habilitación de la ventana. Se ejecutan con `mvn clean verify`.
