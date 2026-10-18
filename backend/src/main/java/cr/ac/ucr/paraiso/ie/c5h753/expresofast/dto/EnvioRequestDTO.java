package cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record EnvioRequestDTO(
        @NotBlank(message = "El código de rastreo es obligatorio")
        @Pattern(regexp = "^EXP-\\d{4}$", message = "Formato inválido. Ejemplo: EXP-1234")
        String codigoRastreo,

        @NotBlank(message = "La dirección de destino es obligatoria")
        @Size(max = 250, message = "La dirección no puede exceder 250 caracteres")
        String direccionDestino,

        @NotNull(message = "El peso es obligatorio")
        @Positive(message = "El peso debe ser mayor a cero")
        BigDecimal pesoKg,

        @NotNull(message = "El costo es obligatorio")
        @PositiveOrZero(message = "El costo no puede ser negativo")
        BigDecimal costo,

        @NotNull(message = "El vehículo es obligatorio")
        Integer vehiculoId,

        @NotNull(message = "El conductor es obligatorio")
        Integer conductorId) {
}
