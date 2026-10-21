package pe.zegel.soporte.dto;

import java.time.OffsetDateTime;
import java.util.List;

/** Estructura estable de errores: no expone trazas internas al cliente. */
public record ApiError(OffsetDateTime timestamp, int status, String error,
                       String mensaje, String ruta, List<DetalleError> detalles) {
    public record DetalleError(String campo, String mensaje) { }
}
