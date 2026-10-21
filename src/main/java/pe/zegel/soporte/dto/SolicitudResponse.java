package pe.zegel.soporte.dto;

import java.time.OffsetDateTime;
import pe.zegel.soporte.model.Estado;
import pe.zegel.soporte.model.Prioridad;
import pe.zegel.soporte.model.Solicitud;

/** DTO de salida: evita exponer directamente la entidad del dominio. */
public record SolicitudResponse(
    long id, String asunto, String descripcion, PersonaResponse cliente,
    PersonaResponse tecnico, Estado estado, Prioridad prioridad,
    OffsetDateTime fechaCreacion, OffsetDateTime fechaActualizacion
) {
    public record PersonaResponse(String nombre, String email) { }

    public static SolicitudResponse desde(Solicitud s) {
        return new SolicitudResponse(s.getId(), s.getAsunto(), s.getDescripcion(),
            new PersonaResponse(s.getCliente().nombre(), s.getCliente().email()),
            s.getTecnico() == null ? null : new PersonaResponse(s.getTecnico().nombre(), s.getTecnico().email()),
            s.getEstado(), s.getPrioridad(), s.getFechaCreacion(), s.getFechaActualizacion());
    }
}
