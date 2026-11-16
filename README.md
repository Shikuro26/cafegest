# CaféGest

Aplicación de escritorio para gestionar pedidos de una cafetería.

## Objetivo

Registrar pedidos, personalizar productos mediante adiciones, aplicar promociones y comunicar el avance de la preparación a caja y cocina.

## Funciones

- Consulta del menú con categoría, precio y disponibilidad.
- Pedidos con identificador de sesión y estado inicial recibido.
- Agregar productos con cantidades enteras mayores que cero; modificar cantidades o retirar líneas solo mientras el pedido esté recibido y sin confirmar.
- Adiciones compatibles que cambian la descripción y el precio: shot extra y leche vegetal. No se repiten dentro de un mismo producto.
- Confirmación del pedido: rechaza pedidos vacíos, calcula subtotal, descuento y total, y conserva esos importes.
- Promociones por día u horario, sin acumulación: se aplica la de mayor descuento. Incluidas: "Martes de café" (10 %) y "Hora feliz" de 15:00 a 17:00 (15 %).
- Estados recibido, en preparación y entregado. Solo se avanza en ese orden y solo después de confirmar.
- Avisos a caja y cocina después de cada cambio de estado válido.
- Consulta de los pedidos de la sesión actual.

Los datos se mantienen en memoria durante una ejecución local. El menú, las adiciones y las promociones son configuración interna.

## Tecnología

Java 21 y Maven 3.9.9 o superior. La consola y la interfaz gráfica (Swing) usan los mismos servicios de negocio.

## Compilar y probar

Requisitos: JDK 21 o superior y Maven 3.9.9 o superior disponibles en la terminal. La primera compilación necesita acceso a Maven Central para descargar dependencias.

```sh
java -version
mvn -version
mvn clean verify
```

Las pruebas JUnit cubren el catálogo, las adiciones, las reglas del pedido, los cálculos de promociones y descuentos, las transiciones de estado, los avisos y los flujos de consola. Los reportes se generan en `target/surefire-reports/`.

## Ejecutar

Después de compilar:

```sh
java -jar target/cafegest-0.1.0-SNAPSHOT.jar
```

Para la consola se muestra el menú numerado. Escriba el número de la opción y presione Enter. Las opciones principales son:

1. Consultar menú
2. Crear pedido
3. Agregar producto a un pedido (código, adiciones separadas por coma o Enter, cantidad)
4. Modificar cantidad de una línea
5. Retirar una línea
6. Confirmar pedido
7. Consultar pedido
8. Listar pedidos de la sesión
9. Cambiar estado (1 = en preparación, 2 = entregado)
0. Salir

Códigos de productos de ejemplo: `CAF-AME`, `CAP`, `TOR-CHO` y `SAN-POL`. El té (`TE`) aparece como agotado.

Para la interfaz gráfica:

```sh
java -jar target/cafegest-0.1.0-SNAPSHOT.jar gui
```

La ventana permite crear pedidos, agregar productos con adiciones, consultar, confirmar y cambiar estados. Las vistas de caja y cocina se actualizan con cada cambio.

Se usa UTF-8 para entrada, salida y archivos fuente; configure la terminal con esa codificación si los acentos no se muestran correctamente.

## Estructura

```text
pom.xml
src/main/java/cafegest/
  configuracion/     inicio de la aplicación y configuración interna del menú y promociones
  dominio/           catálogo, productos (Decorator), pedidos y promociones (Strategy)
  aplicacion/        servicios de pedidos, cálculo de totales y eventos (Observer)
  infraestructura/   almacenamiento en memoria
  interfaz/consola/  menú de consola y vistas de texto
  interfaz/grafica/  ventana Swing y vistas gráficas
src/test/java/cafegest/   pruebas unitarias e de flujo
docs/
```

## Documentación técnica

- [Alcance](docs/alcance.md)
- [Requisitos](docs/requisitos.md)
- [Decisiones de diseño](docs/diseno.md)
- [Arquitectura y modelo](docs/arquitectura.md)
