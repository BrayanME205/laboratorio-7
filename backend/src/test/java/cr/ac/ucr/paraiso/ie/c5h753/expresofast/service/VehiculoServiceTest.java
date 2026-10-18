package cr.ac.ucr.paraiso.ie.c5h753.expresofast.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.VehiculoRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.VehiculoResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.InactiveDriverException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.ResourceNotFoundException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository.EmpresaLogisticaRepository;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository.VehiculoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private ConductorRepository conductorRepository;

    @Mock
    private EmpresaLogisticaRepository empresaRepository;

    @InjectMocks
    private VehiculoService vehiculoService;

    private static EmpresaLogistica empresa() {
        EmpresaLogistica empresa = new EmpresaLogistica();
        empresa.setId(1);
        empresa.setNombre("ExpresoFast S.A.");
        return empresa;
    }

    private static Vehiculo vehiculo() {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(7);
        vehiculo.setPlaca("ABC-123");
        vehiculo.setMarca("Toyota");
        vehiculo.setModelo("Hilux");
        vehiculo.setCapacidadKg(new BigDecimal("1000"));
        vehiculo.setEmpresa(empresa());
        return vehiculo;
    }

    private static Conductor conductor(boolean active) {
        Conductor conductor = new Conductor();
        conductor.setId(3);
        conductor.setNombreCompleto("Ana Mora");
        conductor.setActivo(active);
        return conductor;
    }

    private static VehiculoRequestDTO request() {
        return new VehiculoRequestDTO("ABC-123", "Toyota", "Hilux", new BigDecimal("1000"), 1);
    }

    @Test
    void registrarVehiculo_DatosValidos_RetornaVehiculoDTO() {
        when(vehiculoRepository.existsByPlaca("ABC-123")).thenReturn(false);
        when(empresaRepository.findById(1)).thenReturn(Optional.of(empresa()));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(invocation -> {
            Vehiculo saved = invocation.getArgument(0);
            saved.setId(7);
            return saved;
        });

        VehiculoResponseDTO result = vehiculoService.registrarVehiculo(request());

        assertEquals(7, result.id());
        assertEquals("ABC-123", result.placa());
        assertEquals("ExpresoFast S.A.", result.nombreEmpresa());
        assertNull(result.conductorId());
        assertNull(result.nombreConductor());
    }

    @Test
    void registrarVehiculo_PlacaDuplicada_LanzaExcepcion() {
        when(vehiculoRepository.existsByPlaca("ABC-123")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehiculoService.registrarVehiculo(request()));

        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    void registrarVehiculo_EmpresaInexistente_LanzaExcepcion() {
        when(vehiculoRepository.existsByPlaca("ABC-123")).thenReturn(false);
        when(empresaRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehiculoService.registrarVehiculo(request()));
    }

    @Test
    void asignarConductor_ConductorActivo_AsignaConductor() {
        when(vehiculoRepository.findById(7)).thenReturn(Optional.of(vehiculo()));
        when(conductorRepository.findById(3)).thenReturn(Optional.of(conductor(true)));
        when(vehiculoRepository.save(any(Vehiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehiculoResponseDTO result = vehiculoService.asignarConductor(7, 3);

        assertEquals(3, result.conductorId());
        assertEquals("Ana Mora", result.nombreConductor());
    }

    @Test
    void asignarConductor_ConductorInactivo_LanzaExcepcion() {
        when(vehiculoRepository.findById(7)).thenReturn(Optional.of(vehiculo()));
        when(conductorRepository.findById(3)).thenReturn(Optional.of(conductor(false)));

        assertThrows(InactiveDriverException.class, () -> vehiculoService.asignarConductor(7, 3));

        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    void asignarConductor_VehiculoInexistente_LanzaExcepcion() {
        when(vehiculoRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehiculoService.asignarConductor(7, 3));
    }

    @Test
    void asignarConductor_ConductorInexistente_LanzaExcepcion() {
        when(vehiculoRepository.findById(7)).thenReturn(Optional.of(vehiculo()));
        when(conductorRepository.findById(3)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehiculoService.asignarConductor(7, 3));
    }

    @Test
    void listarVehiculos_RetornaListaDeDTOs() {
        when(vehiculoRepository.findAllDetailed()).thenReturn(List.of(vehiculo()));

        List<VehiculoResponseDTO> result = vehiculoService.listarVehiculos();

        assertEquals(1, result.size());
        assertEquals("Hilux", result.get(0).modelo());
    }

    @Test
    void obtenerVehiculo_Existente_RetornaDTO() {
        when(vehiculoRepository.findById(7)).thenReturn(Optional.of(vehiculo()));

        assertEquals("ABC-123", vehiculoService.obtenerVehiculo(7).placa());
    }

    @Test
    void obtenerVehiculo_Inexistente_LanzaExcepcion() {
        when(vehiculoRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehiculoService.obtenerVehiculo(7));
    }

    @Test
    void eliminarVehiculo_Existente_EliminaRegistro() {
        when(vehiculoRepository.existsById(7)).thenReturn(true);

        vehiculoService.eliminarVehiculo(7);

        verify(vehiculoRepository).deleteById(7);
    }

    @Test
    void eliminarVehiculo_Inexistente_LanzaExcepcion() {
        when(vehiculoRepository.existsById(7)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> vehiculoService.eliminarVehiculo(7));

        verify(vehiculoRepository, never()).deleteById(any());
    }
}
