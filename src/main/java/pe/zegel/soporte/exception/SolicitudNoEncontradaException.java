package pe.zegel.soporte.exception;

public class SolicitudNoEncontradaException extends RuntimeException {
    public SolicitudNoEncontradaException(long id) {
        super("No existe una solicitud con id " + id);
    }
}
