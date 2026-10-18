package cr.ac.ucr.paraiso.ie.c5h753.expresofast.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EmpresaRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EmpresaResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.ResourceNotFoundException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository.EmpresaLogisticaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmpresaLogisticaServiceTest {

    @Mock
    private EmpresaLogisticaRepository empresaRepository;

    @InjectMocks
    private EmpresaLogisticaService empresaService;

    private static EmpresaLogistica empresa() {
        EmpresaLogistica empresa = new EmpresaLogistica();
        empresa.setId(1);
        empresa.setNombre("ExpresoFast S.A.");
        empresa.setCedulaJuridica("3-101-123456");
        empresa.setTelefono("2574-0000");
        empresa.setActiva(true);
        return empresa;
    }

    private static EmpresaRequestDTO request() {
        return new EmpresaRequestDTO("ExpresoFast S.A.", "3-101-123456", "2574-0000");
    }

    @Test
    void crearEmpresa_DatosValidos_RetornaEmpresaDTO() {
        when(empresaRepository.existsByCedulaJuridica("3-101-123456")).thenReturn(false);
        when(empresaRepository.save(any(EmpresaLogistica.class))).thenAnswer(invocation -> {
            EmpresaLogistica saved = invocation.getArgument(0);
            saved.setId(1);
            return saved;
        });

        EmpresaResponseDTO result = empresaService.crearEmpresa(request());

        assertEquals(1, result.id());
        assertEquals("ExpresoFast S.A.", result.nombre());
        assertTrue(result.activa());
    }

    @Test
    void crearEmpresa_CedulaDuplicada_LanzaExcepcion() {
        when(empresaRepository.existsByCedulaJuridica("3-101-123456")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> empresaService.crearEmpresa(request()));

        verify(empresaRepository, never()).save(any());
    }

    @Test
    void listarEmpresas_RetornaListaDeDTOs() {
        when(empresaRepository.findAll()).thenReturn(List.of(empresa()));

        List<EmpresaResponseDTO> result = empresaService.listarEmpresas();

        assertEquals(1, result.size());
        assertEquals("3-101-123456", result.get(0).cedulaJuridica());
    }

    @Test
    void obtenerEmpresa_Existente_RetornaDTO() {
        when(empresaRepository.findById(1)).thenReturn(Optional.of(empresa()));

        assertEquals("ExpresoFast S.A.", empresaService.obtenerEmpresa(1).nombre());
    }

    @Test
    void obtenerEmpresa_Inexistente_LanzaExcepcion() {
        when(empresaRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> empresaService.obtenerEmpresa(1));
    }

    @Test
    void eliminarEmpresa_Existente_EliminaRegistro() {
        when(empresaRepository.existsById(1)).thenReturn(true);

        empresaService.eliminarEmpresa(1);

        verify(empresaRepository).deleteById(1);
    }

    @Test
    void eliminarEmpresa_Inexistente_LanzaExcepcion() {
        when(empresaRepository.existsById(1)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> empresaService.eliminarEmpresa(1));

        verify(empresaRepository, never()).deleteById(any());
    }
}
