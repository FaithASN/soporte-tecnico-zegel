package pe.zegel.soporte.model;

import java.time.OffsetDateTime;

/** Entidad inmutable: protege su identidad y sus fechas de cambios externos. */
public final class Solicitud {
    private final long id;
    private final String asunto;
    private final String descripcion;
    private final Cliente cliente;
    private final Tecnico tecnico;
    private final Estado estado;
    private final Prioridad prioridad;
    private final OffsetDateTime fechaCreacion;
    private final OffsetDateTime fechaActualizacion;

    public Solicitud(long id, String asunto, String descripcion, Cliente cliente,
                     Tecnico tecnico, Estado estado, Prioridad prioridad,
                     OffsetDateTime fechaCreacion, OffsetDateTime fechaActualizacion) {
        this.id = id;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.cliente = cliente;
        this.tecnico = tecnico;
        this.estado = estado;
        this.prioridad = prioridad;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public long getId() { return id; }
    public String getAsunto() { return asunto; }
    public String getDescripcion() { return descripcion; }
    public Cliente getCliente() { return cliente; }
    public Tecnico getTecnico() { return tecnico; }
    public Estado getEstado() { return estado; }
    public Prioridad getPrioridad() { return prioridad; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
}
