# Requisitos de CaféGest

## Funcionales

- **RF-01:** Consultar menú con nombre, categoría, precio y disponibilidad.
- **RF-02:** Crear pedidos con identificador único en la sesión y estado inicial recibido.
- **RF-03:** Agregar productos disponibles con cantidades enteras mayores que cero; rechazar entradas inválidas.
- **RF-04:** Modificar cantidades o retirar productos solo en estado recibido.
- **RF-05:** Seleccionar adiciones compatibles, actualizar precio y descripción y rechazar incompatibilidades.
- **RF-06:** Calcular subtotal sumando precio base y adiciones, multiplicados por cantidad.
- **RF-07:** Al confirmar, evaluar promociones por día y hora local; aplicar solo el mayor descuento o cero si ninguna corresponde.
- **RF-08:** Mostrar subtotal, descuento y total no negativo; conservar los valores confirmados durante los cambios de estado.
- **RF-09:** Permitir únicamente recibido a en preparación y en preparación a entregado, rechazando saltos y retrocesos.
- **RF-10:** Actualizar las vistas locales de caja y cocina después de cada cambio de estado, con identificador y nuevo estado.
- **RF-11:** Consultar pedidos de la sesión y su detalle: productos, cantidades, adiciones, valores y estado.
- **RF-12:** Impedir confirmar pedidos vacíos y mostrar cómo corregirlos.

## No funcionales

- **RNF-01 — Tecnología:** Java; documentar JDK y pasos de compilación y ejecución al configurar el proyecto.
- **RNF-02 — Arquitectura:** Separar dominio, servicios e interfaces; consola e interfaz gráfica compartirán servicios de negocio.
- **RNF-03 — Precisión:** Usar BigDecimal, pesos colombianos, dos decimales y redondeo HALF_UP en descuentos.
- **RNF-04 — Validación:** Mensajes claros en español y recuperación de entradas inválidas sin cerrar el programa ni perder el pedido.
- **RNF-05 — Extensibilidad:** Añadir promociones mediante implementaciones adicionales y su registro, sin modificar el servicio central de cálculo.
- **RNF-06 — Pruebas:** Pruebas unitarias para clases que implementen patrones, cálculos, promociones, adiciones incompatibles y transiciones inválidas.
- **RNF-07 — Documentación:** Mantener código, historial de cambios y README con funciones implementadas e instrucciones de ejecución de ambas interfaces.
- **RNF-08 — Almacenamiento:** Datos en memoria durante una ejecución local; sin base de datos ni servicios externos obligatorios.
