package cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record VehiculoRequestDTO(
        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^[A-Z0-9-]{4,10}$", message = "Formato de placa inválido")
        String placa,

        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 50, message = "La marca no puede exceder 50 caracteres")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 50, message = "El modelo no puede exceder 50 caracteres")
        String modelo,

        @NotNull(message = "La capacidad es obligatoria")
        @Positive(message = "La capacidad debe ser mayor a cero")
        BigDecimal capacidadKg,

        @NotNull(message = "La empresa es obligatoria")
        Integer empresaId) {
}
