package cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Envio;
import java.math.BigDecimal;

public record EnvioResponseDTO(
        Integer id,
        String codigoRastreo,
        String direccionDestino,
        BigDecimal pesoKg,
        BigDecimal costo,
        String estadoEnvio,
        String placaVehiculo,
        String nombreConductor) {

    public static EnvioResponseDTO from(Envio envio) {
        return new EnvioResponseDTO(
                envio.getId(),
                envio.getCodigoRastreo(),
                envio.getDireccionDestino(),
                envio.getPesoKg(),
                envio.getCosto(),
                envio.getEstadoEnvio(),
                envio.getVehiculo().getPlaca(),
                envio.getConductor().getNombreCompleto());
    }
}
