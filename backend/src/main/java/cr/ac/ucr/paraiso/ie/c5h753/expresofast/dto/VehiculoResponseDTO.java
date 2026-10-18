package cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Vehiculo;
import java.math.BigDecimal;

public record VehiculoResponseDTO(
        Integer id,
        String placa,
        String marca,
        String modelo,
        BigDecimal capacidadKg,
        String nombreEmpresa,
        Integer conductorId,
        String nombreConductor) {

    public static VehiculoResponseDTO from(Vehiculo vehiculo) {
        return new VehiculoResponseDTO(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getCapacidadKg(),
                vehiculo.getEmpresa().getNombre(),
                vehiculo.getConductor() == null ? null : vehiculo.getConductor().getId(),
                vehiculo.getConductor() == null ? null : vehiculo.getConductor().getNombreCompleto());
    }
}
