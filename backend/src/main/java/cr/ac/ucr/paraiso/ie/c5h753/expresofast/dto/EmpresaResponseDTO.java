package cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.EmpresaLogistica;

public record EmpresaResponseDTO(Integer id, String nombre, String cedulaJuridica, String telefono, Boolean activa) {

    public static EmpresaResponseDTO from(EmpresaLogistica empresa) {
        return new EmpresaResponseDTO(
                empresa.getId(),
                empresa.getNombre(),
                empresa.getCedulaJuridica(),
                empresa.getTelefono(),
                empresa.getActiva());
    }
}
