# Diagramas de CaféGest

Los diagramas siguen el código actual del repositorio. Las clases mostradas existen con esos nombres en los paquetes indicados. Los diagramas de Mermaid se renderizan en GitHub. El diagrama de casos de uso está como código PlantUML en [casos-de-uso.puml](casos-de-uso.puml); para verlo, péguelo en [el editor de PlantUML](https://www.plantuml.com/plantuml) o ejecute `java -jar plantuml.jar -tpng docs/casos-de-uso.puml`.

## Diagrama de clases del patrón Decorator

Personalización de un producto con adiciones (`cafegest.dominio.producto` y `cafegest.aplicacion`).

```mermaid
classDiagram
    class ProductoPedido {
        <<interface>>
        +precio() BigDecimal
        +descripcion() String
    }
    class ProductoBase {
        +idProducto() String
    }
    class AdicionDecorator {
        <<abstract>>
        -envuelto: ProductoPedido
        -adicion: Adicion
    }
    class ShotExtra
    class LecheVegetal
    class Adicion {
        <<enumeration>>
        SHOT_EXTRA
        LECHE_VEGETAL
        +aplicar(ProductoPedido) ProductoPedido
        +esCompatibleCon(Categoria) boolean
    }
    class ConfiguradorProducto {
        +configurar(productoId, adiciones) ProductoPedido
    }
    class ReglasAdiciones {
        <<utility>>
        +verificar(ProductoMenu, adiciones)$
    }
    class LineaPedido {
        +numero: int
        +cantidad: int
    }

    ProductoPedido <|.. ProductoBase
    ProductoPedido <|.. AdicionDecorator
    AdicionDecorator <|-- ShotExtra
    AdicionDecorator <|-- LecheVegetal
    AdicionDecorator o-- ProductoPedido : envuelve
    Adicion ..> AdicionDecorator : crea
    ConfiguradorProducto ..> ReglasAdiciones : valida
    ConfiguradorProducto ..> ProductoBase : crea
    ConfiguradorProducto ..> Adicion : aplica
    LineaPedido o-- ProductoPedido : configuracion

    note for ProductoPedido "Componente"
    note for ProductoBase "Componente concreto"
    note for AdicionDecorator "Decorador base"
    note for ShotExtra "Decorador concreto"
    note for LecheVegetal "Decorador concreto"
```

## Diagrama de clases del patrón Strategy

Descuentos intercambiables sobre el subtotal (`cafegest.dominio.promocion` y `cafegest.aplicacion`).

```mermaid
classDiagram
    class Promocion {
        <<interface>>
        +nombre() String
        +calcularDescuento(ContextoPromocion) BigDecimal
    }
    class PromocionPorDia {
        -dia: DayOfWeek
        -porcentaje: BigDecimal
    }
    class PromocionPorHorario {
        -inicio: LocalTime
        -fin: LocalTime
        -porcentaje: BigDecimal
    }
    class DescuentoPorcentual {
        <<utility>>
        +sobre(subtotal, porcentaje)$
    }
    class ContextoPromocion {
        <<record>>
        +subtotal: BigDecimal
        +momento: LocalDateTime
    }
    class CalculadorTotal {
        -promociones: List~Promocion~
        -reloj: Clock
        +calcular(Pedido) ResumenCobro
    }
    class Pedido
    class ResumenCobro {
        <<record>>
        +subtotal: BigDecimal
        +descuento: BigDecimal
        +total() BigDecimal
    }

    Promocion <|.. PromocionPorDia
    Promocion <|.. PromocionPorHorario
    PromocionPorDia ..> DescuentoPorcentual : usa
    PromocionPorHorario ..> DescuentoPorcentual : usa
    CalculadorTotal o-- Promocion : evalua
    CalculadorTotal ..> ContextoPromocion : crea
    CalculadorTotal ..> Pedido : lee subtotal
    CalculadorTotal ..> ResumenCobro : devuelve

    note for Promocion "Estrategia"
    note for PromocionPorDia "Estrategia concreta"
    note for PromocionPorHorario "Estrategia concreta"
    note for CalculadorTotal "Contexto"
```

## Diagrama de clases del patrón Observer

Avisos de cambio de estado a caja y cocina (`cafegest.aplicacion.evento`, `cafegest.aplicacion.puerto` e `cafegest.interfaz.consola`).

```mermaid
classDiagram
    class ObservadorPedido {
        <<interface>>
        +actualizar(PedidoCambioEstado)
    }
    class PublicadorEventosPedido {
        -observadores: List~ObservadorPedido~
        +suscribir(ObservadorPedido)
        +retirar(ObservadorPedido)
        +publicar(PedidoCambioEstado)
    }
    class PedidoCambioEstado {
        <<record>>
        +idPedido: String
        +anterior: EstadoPedido
        +nuevo: EstadoPedido
    }
    class VistaCaja
    class VistaCocina
    class ServicioPedidos {
        +cambiarEstado(idPedido, destino)
    }
    class Pedido {
        +cambiarEstado(destino)
    }

    ObservadorPedido <|.. VistaCaja
    ObservadorPedido <|.. VistaCocina
    PublicadorEventosPedido o-- ObservadorPedido : notifica
    PublicadorEventosPedido ..> PedidoCambioEstado : publica
    ServicioPedidos --> Pedido : valida transicion
    ServicioPedidos --> PublicadorEventosPedido : publica tras guardar

    note for PublicadorEventosPedido "Sujeto"
    note for ObservadorPedido "Observador"
    note for VistaCaja "Observador concreto"
    note for VistaCocina "Observador concreto"
```

## Diagrama de secuencia: agregar y confirmar un pedido

Flujo principal en el que participan Decorator (configurar el producto) y Strategy (calcular el descuento al confirmar).

```mermaid
sequenceDiagram
    actor Operador
    participant UI as MenuConsola / VentanaPrincipal
    participant S as ServicioPedidos
    participant C as ConfiguradorProducto
    participant R as ReglasAdiciones
    participant A as Adicion
    participant P as Pedido
    participant CT as CalculadorTotal
    participant PR as Promocion
    participant RP as RepositorioPedidos

    Operador->>UI: agregar Café americano con Shot extra
    UI->>S: agregarProducto(id, "CAF-AME", [SHOT_EXTRA], 2)
    S->>C: configurar("CAF-AME", [SHOT_EXTRA])
    C->>R: verificar(base, adiciones)
    C->>A: aplicar(ProductoBase)
    A-->>C: ShotExtra(ProductoBase)
    C-->>S: ProductoPedido configurado
    S->>P: agregarProducto(configurado, 2)
    S->>RP: guardar(pedido)
    UI-->>Operador: línea agregada

    Operador->>UI: confirmar pedido
    UI->>S: confirmarPedido(id)
    S->>CT: calcular(pedido)
    CT->>P: subtotal()
    loop cada promoción registrada
        CT->>PR: calcularDescuento(contexto)
        PR-->>CT: descuento
    end
    CT-->>S: ResumenCobro (mayor descuento)
    S->>P: confirmar(resumen)
    S->>RP: guardar(pedido)
    UI-->>Operador: subtotal, descuento y total
```

## Diagrama de secuencia: cambio de estado y avisos

Flujo en el que participa Observer.

```mermaid
sequenceDiagram
    actor Operador
    participant UI as MenuConsola / VentanaPrincipal
    participant S as ServicioPedidos
    participant P as Pedido
    participant RP as RepositorioPedidos
    participant PB as PublicadorEventosPedido
    participant V as VistaCaja / VistaCocina

    Operador->>UI: pasar a preparación
    UI->>S: cambiarEstado(id, EN_PREPARACION)
    S->>P: cambiarEstado(EN_PREPARACION)
    alt transición inválida
        P-->>S: ReglaNegocioException
        S-->>UI: mensaje de error (sin evento)
    else transición válida
        S->>RP: guardar(pedido)
        S->>PB: publicar(PedidoCambioEstado)
        loop cada observador suscrito
            PB->>V: actualizar(evento)
        end
        UI-->>Operador: estado actualizado
    end
```
