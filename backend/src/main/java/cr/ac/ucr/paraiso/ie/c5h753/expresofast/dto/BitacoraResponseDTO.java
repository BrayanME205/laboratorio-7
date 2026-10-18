package cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.BitacoraEnvio;
import java.time.LocalDateTime;

public record BitacoraResponseDTO(
        Integer id,
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime fechaCambio,
        String usuario,
        String observaciones) {

    public static BitacoraResponseDTO from(BitacoraEnvio entry) {
        return new BitacoraResponseDTO(
                entry.getId(),
                entry.getEstadoAnterior(),
                entry.getEstadoNuevo(),
                entry.getFechaCambio(),
                entry.getUsuario().getUsername(),
                entry.getObservaciones());
    }
}
