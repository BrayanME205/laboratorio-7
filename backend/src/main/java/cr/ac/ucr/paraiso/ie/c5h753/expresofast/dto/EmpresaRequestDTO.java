package cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmpresaRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @NotBlank(message = "La cédula jurídica es obligatoria")
        @Size(max = 20, message = "La cédula jurídica no puede exceder 20 caracteres")
        String cedulaJuridica,

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        String telefono) {
}
