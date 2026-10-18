package cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto;

import jakarta.validation.constraints.NotNull;

public record AsignarConductorDTO(@NotNull(message = "El conductor es obligatorio") Integer conductorId) {
}
