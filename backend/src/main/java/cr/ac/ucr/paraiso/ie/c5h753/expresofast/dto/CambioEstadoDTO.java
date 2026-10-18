package cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CambioEstadoDTO(
        @NotBlank(message = "El nuevo estado es obligatorio")
        @Pattern(regexp = "PENDIENTE|EN_TRANSITO|ENTREGADO|CANCELADO", message = "Estado inválido")
        String nuevoEstado,

        @Size(max = 250, message = "Las observaciones no pueden exceder 250 caracteres")
        String observaciones) {
}
