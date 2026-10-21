# Verificación realizada

Fecha: 20 de octubre de 2026 (America/Lima).

- Java 21.0.12.1; Spring Boot 3.5.16; Maven 3.9.11.
- `clean verify`: compilación y empaquetado correctos.
- 13 pruebas de integración Spring/MockMvc: 0 fallos, 0 errores.
- Colección de Postman ejecutada contra el JAR mediante Newman 6.2.1: 18 peticiones, 36 aserciones, 0 fallos.
- El servidor HTTP de prueba se inició en `127.0.0.1:18080` y se detuvo al finalizar. La colección distribuida utiliza `http://localhost:8080` por defecto.
- Se comprobó con un reinicio real que los registros se pierden y el mapa vuelve a estar vacío.

Los archivos de resultados originales se entregan en la carpeta `evidencias` junto al proyecto. Son evidencias automatizadas; faltan las capturas de la interfaz de Postman exigidas por la rúbrica. La aplicación no queda encendida después de estas verificaciones.
