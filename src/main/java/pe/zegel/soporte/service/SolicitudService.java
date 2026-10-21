package pe.zegel.soporte.service;

import java.util.List;
import pe.zegel.soporte.dto.SolicitudRequest;
import pe.zegel.soporte.dto.SolicitudResponse;
import pe.zegel.soporte.model.Estado;

/** Contrato que permite cambiar la implementacion sin modificar el controlador. */
public interface SolicitudService {
    SolicitudResponse registrar(SolicitudRequest entrada);
    List<SolicitudResponse> listar(Estado estado);
    SolicitudResponse obtener(long id);
    SolicitudResponse actualizar(long id, SolicitudRequest entrada);
    void eliminar(long id);
}
