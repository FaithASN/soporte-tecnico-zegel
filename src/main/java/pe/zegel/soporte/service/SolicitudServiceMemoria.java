package pe.zegel.soporte.service;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import pe.zegel.soporte.dto.SolicitudRequest;
import pe.zegel.soporte.dto.SolicitudResponse;
import pe.zegel.soporte.exception.ReglaNegocioException;
import pe.zegel.soporte.exception.SolicitudNoEncontradaException;
import pe.zegel.soporte.model.*;

/**
 * Simulacion de persistencia: el mapa existe solamente durante esta ejecucion.
 * Los metodos sincronizados protegen el mapa y la secuencia ante peticiones simultaneas.
 */
@Service
public class SolicitudServiceMemoria implements SolicitudService {
    private final Map<Long, Solicitud> solicitudes = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public synchronized SolicitudResponse registrar(SolicitudRequest entrada) {
        validarAsignacion(entrada);
        OffsetDateTime ahora = OffsetDateTime.now();
        Solicitud nueva = construir(siguienteId++, entrada, ahora, ahora);
        solicitudes.put(nueva.getId(), nueva);
        return SolicitudResponse.desde(nueva);
    }

    @Override
    public synchronized List<SolicitudResponse> listar(Estado estado) {
        return solicitudes.values().stream()
            .filter(s -> estado == null || s.getEstado() == estado)
            .map(SolicitudResponse::desde).toList();
    }

    @Override
    public synchronized SolicitudResponse obtener(long id) {
        return SolicitudResponse.desde(buscar(id));
    }

    @Override
    public synchronized SolicitudResponse actualizar(long id, SolicitudRequest entrada) {
        Solicitud anterior = buscar(id);
        validarAsignacion(entrada);
        Solicitud actualizada = construir(id, entrada, anterior.getFechaCreacion(), OffsetDateTime.now());
        solicitudes.put(id, actualizada);
        return SolicitudResponse.desde(actualizada);
    }

    @Override
    public synchronized void eliminar(long id) {
        buscar(id);
        solicitudes.remove(id);
    }

    private Solicitud buscar(long id) {
        Solicitud encontrada = solicitudes.get(id);
        if (encontrada == null) { throw new SolicitudNoEncontradaException(id); }
        return encontrada;
    }

    private void validarAsignacion(SolicitudRequest entrada) {
        if (entrada.estado() != Estado.PENDIENTE && entrada.tecnico() == null) {
            throw new ReglaNegocioException("Debe asignar un tecnico cuando el estado no es PENDIENTE");
        }
    }

    private Solicitud construir(long id, SolicitudRequest e, OffsetDateTime creacion, OffsetDateTime actualizacion) {
        Cliente cliente = new Cliente(e.cliente().nombre().strip(), e.cliente().email().strip());
        Tecnico tecnico = e.tecnico() == null ? null
            : new Tecnico(e.tecnico().nombre().strip(), e.tecnico().email().strip());
        return new Solicitud(id, e.asunto().strip(), e.descripcion().strip(), cliente,
            tecnico, e.estado(), e.prioridad(), creacion, actualizacion);
    }
}
