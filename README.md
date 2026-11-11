# CaféGest

Aplicación de escritorio para gestionar pedidos de una cafetería.

## Objetivo

Registrar pedidos, personalizar productos mediante adiciones, aplicar promociones y comunicar el avance de la preparación a caja y cocina.

## Funciones previstas

- Menú de bebidas y alimentos con precios y disponibilidad.
- Pedidos con productos, cantidades y adiciones compatibles.
- Cálculo de subtotal, descuento y total.
- Promociones según día u horario.
- Estados recibido, en preparación y entregado.
- Actualización de las vistas de caja y cocina.

## Tecnología y arquitectura

Java 21 y Maven 3.9.9 o superior. La consola y la futura interfaz gráfica usarán el mismo núcleo de negocio. Los datos se mantendrán en memoria durante una ejecución local.

## Estado de desarrollo

Existe una base ejecutable por consola con mensaje de inicio, salida mediante la opción `0`, recuperación de entradas inválidas y cierre cuando se agota la entrada. Todavía no se han implementado el catálogo, los pedidos, las promociones, los patrones propuestos ni la interfaz gráfica.

## Compilar y probar

Requisitos: JDK 21 o superior y Maven 3.9.9 o superior disponibles en la terminal. La primera compilación necesita acceso a Maven Central para descargar dependencias.

```sh
java -version
mvn -version
mvn clean verify
```

Las pruebas JUnit comprueban salida normal, recuperación de una entrada inválida y final de entrada. Los reportes se generan en `target/surefire-reports/`.

## Ejecutar

Después de compilar:

```sh
java -jar target/cafegest-0.1.0-SNAPSHOT.jar
```

Escriba `0` y presione Enter para salir. Se usa UTF-8 para entrada, salida y archivos fuente; configure la terminal con esa codificación si los acentos no se muestran correctamente.

## Estructura

```text
pom.xml
src/main/java/cafegest/configuracion/AplicacionCafeGest.java
src/main/java/cafegest/interfaz/consola/MenuConsola.java
src/test/java/cafegest/interfaz/consola/MenuConsolaTest.java
docs/
```

La entrada y la salida se inyectan en `MenuConsola` para poder probarla sin reemplazar los flujos globales del proceso. Las reglas de negocio se incorporarán en sus paquetes al implementar cada caso de uso.

## Documentación técnica

- [Alcance](docs/alcance.md)
- [Requisitos](docs/requisitos.md)
- [Decisiones de diseño](docs/diseno.md)
- [Arquitectura y modelo inicial](docs/arquitectura.md)
