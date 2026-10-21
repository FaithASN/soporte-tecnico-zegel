package pe.zegel.soporte.controller;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.zegel.soporte.dto.SolicitudRequest;
import pe.zegel.soporte.dto.SolicitudResponse;
import pe.zegel.soporte.model.Estado;
import pe.zegel.soporte.service.SolicitudService;

/** Capa HTTP: delega la logica de negocio al servicio mediante su interfaz. */
@RestController
@RequestMapping("/api/v1/solicitudes")
public class SolicitudController {
    private final SolicitudService servicio;

    public SolicitudController(SolicitudService servicio) { this.servicio = servicio; }

    @PostMapping
    public ResponseEntity<SolicitudResponse> registrar(@Valid @RequestBody SolicitudRequest entrada) {
        SolicitudResponse creada = servicio.registrar(entrada);
        return ResponseEntity.created(URI.create("/api/v1/solicitudes/" + creada.id())).body(creada);
    }

    @GetMapping
    public List<SolicitudResponse> listar(@RequestParam(required = false) Estado estado) {
        return servicio.listar(estado);
    }

    @GetMapping("/{id}")
    public SolicitudResponse obtener(@PathVariable @Positive long id) { return servicio.obtener(id); }

    @PutMapping("/{id}")
    public SolicitudResponse actualizar(@PathVariable @Positive long id,
                                       @Valid @RequestBody SolicitudRequest entrada) {
        return servicio.actualizar(id, entrada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
