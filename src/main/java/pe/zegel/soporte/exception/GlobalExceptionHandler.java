package pe.zegel.soporte.exception;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import pe.zegel.soporte.dto.ApiError;
import pe.zegel.soporte.dto.ApiError.DetalleError;

/** Unifica errores de validacion, de negocio y del protocolo HTTP. */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SolicitudNoEncontradaException.class)
    public ResponseEntity<Object> noEncontrada(SolicitudNoEncontradaException ex, WebRequest request) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of(), new HttpHeaders(), request);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Object> regla(ReglaNegocioException ex, WebRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of(), new HttpHeaders(), request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<DetalleError> detalles = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new DetalleError(e.getField(), e.getDefaultMessage()))
            .sorted(Comparator.comparing(DetalleError::campo).thenComparing(DetalleError::mensaje)).toList();
        return respuesta(status, "Revise los campos enviados", detalles, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return respuesta(status, "JSON invalido: revise campos, tipos y valores de estado o prioridad",
            List.of(), headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<DetalleError> detalles = ex.getParameterValidationResults().stream()
            .flatMap(resultado -> resultado.getResolvableErrors().stream().map(error -> {
                String campo = error instanceof org.springframework.validation.FieldError e
                    ? e.getField() : resultado.getMethodParameter().getParameterName();
                return new DetalleError(campo == null ? "parametro" : campo, error.getDefaultMessage());
            }))
            .sorted(Comparator.comparing(DetalleError::campo)).toList();
        return respuesta(status, "Revise los campos enviados y el identificador positivo", detalles, headers, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String mensaje = switch (status.value()) {
            case 400 -> "Parametro invalido: revise el identificador o el filtro de estado";
            case 404 -> "La ruta solicitada no existe";
            case 405 -> "Metodo HTTP no permitido para esta ruta";
            case 406 -> "Formato de respuesta no disponible; utilice application/json";
            case 415 -> "Tipo de contenido no admitido; utilice application/json";
            default -> "No fue posible procesar la solicitud";
        };
        return respuesta(status, mensaje, List.of(), headers, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> inesperado(Exception ex, WebRequest request) {
        LOG.error("Error no controlado al atender la peticion", ex);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno",
            List.of(), new HttpHeaders(), request);
    }

    private ResponseEntity<Object> respuesta(HttpStatusCode status, String mensaje,
            List<DetalleError> detalles, HttpHeaders headers, WebRequest request) {
        String ruta = ((ServletWebRequest) request).getRequest().getRequestURI();
        HttpStatus conocido = HttpStatus.resolve(status.value());
        ApiError cuerpo = new ApiError(OffsetDateTime.now(), status.value(),
            conocido == null ? "Error" : conocido.getReasonPhrase(), mensaje, ruta, detalles);
        return new ResponseEntity<>(cuerpo, headers, status);
    }
}
