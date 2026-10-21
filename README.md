# API de soporte técnico

Parcial de Desarrollo de los Componentes del Negocio - Zegel.

Aplicación backend en Java 21 y Spring Boot 3.5.16. Registra, consulta, actualiza y elimina solicitudes mediante HTTP/JSON. Usa un `LinkedHashMap` en memoria; no necesita base de datos. No incluye frontend ni autenticación: su alcance es el de la rúbrica.

## 1. Preparar el entorno

Instala un **JDK 21** (no solamente un JRE), por ejemplo desde https://learn.microsoft.com/java/openjdk/download. Comprueba en una nueva terminal:

```powershell
java -version
javac -version
```

Ambos comandos deben indicar Java 21. Si no se reconocen, configura `JAVA_HOME` con la carpeta del JDK y agrega su subcarpeta `bin` a `PATH`; vuelve a abrir la terminal. También puedes seleccionar un JDK 21 desde tu IDE.

Necesitas conexión a internet durante la primera compilación para descargar Maven y las dependencias. Maven Wrapper está incluido: no necesitas instalar Maven globalmente. En Windows, `maven.cmd` es un iniciador sencillo que llama al JAR oficial de Maven Wrapper. En Linux/macOS se incluye el script oficial `mvnw`. Postman se utiliza para las pruebas funcionales. Un IDE es opcional.

## 2. Encender la API en Windows

Abre PowerShell **en la carpeta donde está este README y el archivo `pom.xml`**:

```powershell
.\maven.cmd spring-boot:run
```

Espera a que la consola indique `Started SoporteApplication` y el inicio de Tomcat en el puerto 8080. Mantén esa terminal abierta. Detén la aplicación con `Ctrl+C`.

En Linux/macOS, con JDK 21:

```bash
sh mvnw spring-boot:run
```

Comprueba en tu navegador:

http://localhost:8080/api/v1/solicitudes

Al inicio devuelve `[]`, porque no hay registros precargados. La dirección raíz `http://localhost:8080/` devuelve un error 404: la ruta de la API es `/api/v1/solicitudes`.

## 3. Ejecutar las pruebas automatizadas

```powershell
.\maven.cmd test
```

La suite de integración usa el contexto real de Spring y MockMvc: verifica CRUD, validaciones anidadas, errores, filtros y reglas de negocio. No necesita que arranques antes el servidor. Los resultados se generan en `target/surefire-reports`.

## 4. Crear y ejecutar el JAR

```powershell
.\maven.cmd clean package
java -jar target/soporte-tecnico-1.0.0.jar
```

El JAR se ejecuta con Java 21 sin abrir el IDE. No ejecutes dos instancias en el mismo puerto. Si el puerto 8080 está ocupado:

```powershell
java -jar target/soporte-tecnico-1.0.0.jar --server.port=8081
```

En ese caso cambia `baseUrl` en Postman a `http://localhost:8081`.

## 5. Probar en Postman y obtener las capturas

1. Enciende la API y deja abierta su terminal.
2. Abre Postman e importa `postman/SoporteTecnico.postman_collection.json`.
3. La colección ya trae la variable `baseUrl` con `http://localhost:8080`. No requiere un entorno adicional ni credenciales.
4. Ejecuta las **18 peticiones en su orden numérico**, usando Send una por una o el Collection Runner. La primera guarda automáticamente el ID para las siguientes.
5. La petición 16 elimina el registro de prueba. No esperes encontrarlo después. Puedes volver a ejecutar la colección desde la primera petición.
6. Toma capturas reales de POST 201, GET 200, PUT 200, DELETE 204, validación 400, consulta inexistente 404 y del resultado del Runner.
7. En cada captura muestra el método, URL, cuerpo enviado cuando corresponda, código HTTP y respuesta. No confundas los resultados automatizados de Maven con capturas de Postman.

**Si reinicias la API a mitad de la colección**, los registros se pierden y las variables de Postman pueden conservar IDs anteriores. Comienza otra vez por la petición 01.

## 6. Contrato HTTP

Base: `http://localhost:8080/api/v1/solicitudes`

| Método | Ruta | Resultado correcto |
|---|---|---|
| POST | `/api/v1/solicitudes` | `201 Created`, solicitud y cabecera `Location` |
| GET | `/api/v1/solicitudes` | `200 OK`, lista (puede estar vacía) |
| GET | `/api/v1/solicitudes?estado=PENDIENTE` | `200 OK`, lista filtrada |
| GET | `/api/v1/solicitudes/{id}` | `200 OK`, solicitud |
| PUT | `/api/v1/solicitudes/{id}` | `200 OK`, solicitud actualizada |
| DELETE | `/api/v1/solicitudes/{id}` | `204 No Content`, sin cuerpo |

### Cuerpo para registrar

Usa `Content-Type: application/json`. En Postman: Body > raw > JSON.

```json
{
  "asunto": "Computadora sin encender",
  "descripcion": "No responde al boton de encendido",
  "cliente": {"nombre": "Ana Torres", "email": "ana@example.com"},
  "tecnico": null,
  "estado": "PENDIENTE",
  "prioridad": "ALTA"
}
```

El servidor genera `id`, `fechaCreacion` y `fechaActualizacion`; no los envíes. Los datos de ejemplo son ficticios.

### Cuerpo para actualizar

PUT sustituye **todos los campos editables**. Debes enviar el cuerpo completo; no es una actualización parcial.

```json
{
  "asunto": "Computadora sin encender",
  "descripcion": "Se esta revisando la fuente de alimentacion",
  "cliente": {"nombre": "Ana Torres", "email": "ana@example.com"},
  "tecnico": {"nombre": "Luis Perez", "email": "luis@example.com"},
  "estado": "EN_PROCESO",
  "prioridad": "ALTA"
}
```

### Datos y reglas

| Campo | Regla |
|---|---|
| asunto | Obligatorio, no puede contener solamente espacios, máximo 120 caracteres |
| descripcion | Obligatoria, no puede contener solamente espacios, máximo 2000 caracteres |
| cliente | Obligatorio; nombre no vacío de hasta 100 caracteres y email válido de hasta 150 |
| tecnico | Opcional en PENDIENTE; si se envía, nombre y email deben ser válidos como los del cliente |
| estado | Obligatorio: `PENDIENTE`, `EN_PROCESO`, `RESUELTA`, `CERRADA` |
| prioridad | Obligatoria: `BAJA`, `MEDIA`, `ALTA`, `CRITICA` |
| id en la ruta | Entero positivo de 64 bits |

Regla del proyecto: los estados distintos de PENDIENTE exigen un técnico asignado. No se impone una secuencia de transiciones entre estados. Clientes y técnicos se guardan como datos de cada solicitud; no tienen un catálogo ni endpoints propios. El filtro de estado es opcional y distingue mayúsculas. Los campos desconocidos y los números usados como enums se rechazan con 400 para detectar errores de entrada.

Los IDs aumentan durante una ejecución y no se reutilizan al borrar. PUT conserva el ID y la fecha de creación. Todos los datos e IDs se reinician al detener el proceso. Las fechas se devuelven en ISO-8601 con desplazamiento horario.

### Respuesta de error

```json
{
  "timestamp": "2026-10-20T12:00:00-05:00",
  "status": 400,
  "error": "Bad Request",
  "mensaje": "Revise los campos enviados",
  "ruta": "/api/v1/solicitudes",
  "detalles": [
    {"campo": "asunto", "mensaje": "El asunto es obligatorio"}
  ]
}
```

Ejemplo ilustrativo: fecha y mensajes concretos dependen del error. Códigos: 400 para entrada inválida o regla de negocio; 404 para solicitud/ruta inexistente; 405 para método no permitido; 415 para contenido no admitido; 500 para errores internos inesperados. DELETE y PUT de un ID inexistente devuelven 404. Los detalles de campo se incluyen en errores de validación; en otros casos pueden ser una lista vacía.

## 7. Arquitectura y orientación a objetos

```text
Postman / navegador
        | HTTP y JSON
        v
SolicitudController                 controller
        | depende de la interfaz
        v
SolicitudService                    service (interfaz)
        |
        v
SolicitudServiceMemoria              service (implementación)
        |
        v
Map<Long, Solicitud>                  almacenamiento temporal
```

- `model`: Solicitud, Cliente, Tecnico, Estado y Prioridad. Entidad con campos privados, constructor y getters; valores inmutables para proteger los datos.
- `dto`: contratos de entrada/salida separados de la entidad; validación anidada con `@Valid`, `@NotNull`, `@NotBlank`, `@Email` y `@Size`.
- `service`: aplica las reglas, genera IDs y maneja el mapa. Métodos sincronizados para evitar colisiones al procesar peticiones simultáneas en una instancia.
- `controller`: traduce las operaciones a HTTP y usa inyección por constructor.
- `exception`: excepciones propias y manejo centralizado con `@RestControllerAdvice`, una variante de `@ControllerAdvice` para respuestas JSON.
- `src/test`: pruebas de integración de toda la API mediante MockMvc.

Se utiliza abstracción mediante una interfaz y composición de objetos. No se fuerza herencia entre entidades que no tienen esa relación. El alcance es una sola aplicación con capas: la rúbrica no exige desplegar varios microservicios.

## 8. Qué falta para entregar el parcial

El proyecto y la colección están preparados. Antes de subir a EVA:

1. Todos los integrantes deben ejecutar y comprender el código. Se proporcionaron cuatro nombres, mientras la rúbrica indica tres integrantes; confirmar la conformación del grupo con el docente.
2. Ejecuten Postman y guarden las capturas reales.
3. Completen los nombres, roles y aportes reales en el borrador del informe.
4. Suban el código a GitHub y coloquen el enlace en el informe. Verifiquen acceso de lectura para el docente; un repositorio privado exige darle acceso.
5. Inserten las capturas en el informe y generen su PDF final. El PDF de acompañamiento es un borrador hasta completar esas evidencias.
6. Un solo integrante sube el informe PDF a EVA, de acuerdo con las indicaciones del curso.

### Subir el proyecto a GitHub

Crea un repositorio vacío en tu cuenta. No uses literalmente los textos `TU_USUARIO` y `TU_REPOSITORIO`; sustitúyelos por los tuyos. Dentro de esta carpeta:

```powershell
git init
git add .
git commit -m "Implementar API de soporte tecnico"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/TU_REPOSITORIO.git
git push -u origin main
```

Estos pasos pueden pedir iniciar sesión. `target/` está excluido del repositorio. No subas instalaciones de Java o Maven. También puedes subir los archivos mediante la interfaz de GitHub respetando la estructura de carpetas y conservando `.mvn`.

## 9. Para la exposición

Cada integrante debería poder explicar: qué es una API; cómo una petición llega al controlador y al servicio; por qué el mapa pierde datos al reiniciar; cómo funciona `@Valid`; para qué sirve la interfaz; por qué se usan DTOs; y cómo se devuelve un 404.

Sugerencia de reparto, a confirmar con el trabajo real: integrante 1, modelo/DTOs/validación; integrante 2, servicio/CRUD; integrante 3, controlador/errores/documentación. Todos deben revisar las pruebas y comprender el conjunto. No atribuyan trabajo que no hayan realizado.

## Referencias técnicas

- Spring Boot: https://docs.spring.io/spring-boot/3.5/system-requirements.html
- Spring MVC, validación: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html
- Maven Wrapper: https://maven.apache.org/wrapper/
- Postman, colecciones: https://learning.postman.com/docs/collections/running-collections/intro-to-collection-runs/

## Datos del equipo proporcionados

- Alejandro Colque Vega
- Wily Huamanquispe Vilca
- Franklin Quispe Malca
- Jeremy Sullca Pino

Docente: Luis Alexander Mechan Timana. Pendiente confirmar grupo de cuatro frente a los tres de la rúbrica. Los roles y aportes reales todavía deben completarse.
