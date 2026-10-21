package pe.zegel.soporte.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.zegel.soporte.model.Estado;
import pe.zegel.soporte.model.Prioridad;

/** Contrato de entrada para POST y PUT; el servidor controla id y fechas. */
public record SolicitudRequest(
    @NotBlank(message = "El asunto es obligatorio")
    @Size(max = 120, message = "El asunto admite hasta 120 caracteres") String asunto,
    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 2000, message = "La descripcion admite hasta 2000 caracteres") String descripcion,
    @NotNull(message = "El cliente es obligatorio") @Valid ClienteRequest cliente,
    @Valid TecnicoRequest tecnico,
    @NotNull(message = "El estado es obligatorio") Estado estado,
    @NotNull(message = "La prioridad es obligatoria") Prioridad prioridad
) { }
