package cr.ac.ucr.paraiso.ie.c5h753.expresofast.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.BitacoraEnvio;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Usuario;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.BusinessRuleException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.CapacidadExcedidaException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.InactiveDriverException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.ResourceNotFoundException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository.BitacoraEnvioRepository;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository.ConductorRepository;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository.EnvioRepository;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository.UsuarioRepository;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository.VehiculoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class EnvioServiceTest {

    @Mock
    private EnvioRepository envioRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private ConductorRepository conductorRepository;

    @Mock
    private BitacoraEnvioRepository bitacoraRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EnvioService envioService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static Vehiculo vehiculo(String capacity) {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1);
        vehiculo.setPlaca("ABC-123");
        vehiculo.setCapacidadKg(new BigDecimal(capacity));
        return vehiculo;
    }

    private static Conductor conductor(boolean active) {
        Conductor conductor = new Conductor();
        conductor.setId(2);
        conductor.setNombreCompleto("Mario Rojas");
        conductor.setActivo(active);
        return conductor;
    }

    private static Envio envio(String status) {
        Envio envio = new Envio();
        envio.setId(10);
        envio.setCodigoRastreo("EXP-0001");
        envio.setDireccionDestino("Paraiso, Cartago");
        envio.setPesoKg(new BigDecimal("12.50"));
        envio.setCosto(new BigDecimal("5000"));
        envio.setEstadoEnvio(status);
        envio.setFechaCreacion(LocalDateTime.now());
        envio.setVehiculo(vehiculo("500"));
        envio.setConductor(conductor(true));
        return envio;
    }

    private static EnvioRequestDTO request(String weight) {
        return new EnvioRequestDTO(
                "EXP-1234", "Paraiso, Cartago", new BigDecimal(weight), new BigDecimal("2500"), 1, 2);
    }

    private static Usuario usuario(String username) {
        Usuario usuario = new Usuario();
        usuario.setId(5);
        usuario.setUsername(username);
        return usuario;
    }

    private static void authenticateAs(String username) {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(username, null, List.of()));
    }

    @Test
    @DisplayName("Creates a shipment in PENDIENTE state")
    void crearEnvio_DatosValidos_RetornaEnvioDTO() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo("100")));
        when(conductorRepository.findById(2)).thenReturn(Optional.of(conductor(true)));
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocation -> {
            Envio saved = invocation.getArgument(0);
            saved.setId(10);
            return saved;
        });

        EnvioResponseDTO result = envioService.crearEnvio(request("25.5"));

        assertEquals(10, result.id());
        assertEquals("EXP-1234", result.codigoRastreo());
        assertEquals("PENDIENTE", result.estadoEnvio());
        assertEquals("ABC-123", result.placaVehiculo());
        assertEquals("Mario Rojas", result.nombreConductor());

        ArgumentCaptor<Envio> captor = ArgumentCaptor.forClass(Envio.class);
        verify(envioRepository).save(captor.capture());
        assertEquals("PENDIENTE", captor.getValue().getEstadoEnvio());
    }

    @Test
    void crearEnvio_CodigoDuplicado_LanzaExcepcion() {
        when(envioRepository.existsByCodigoRastreo("EXP-1234")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> envioService.crearEnvio(request("10")));

        verify(envioRepository, never()).save(any());
    }

    @Test
    void crearEnvio_VehiculoInexistente_LanzaExcepcion() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> envioService.crearEnvio(request("10")));
    }

    @Test
    void crearEnvio_VehiculoSinCapacidad_LanzaExcepcion() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo("20")));

        assertThrows(CapacidadExcedidaException.class, () -> envioService.crearEnvio(request("25.5")));

        verify(envioRepository, never()).save(any());
    }

    @Test
    void crearEnvio_ConductorInexistente_LanzaExcepcion() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo("100")));
        when(conductorRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> envioService.crearEnvio(request("10")));
    }

    @Test
    void crearEnvio_ConductorInactivo_LanzaExcepcion() {
        when(vehiculoRepository.findById(1)).thenReturn(Optional.of(vehiculo("100")));
        when(conductorRepository.findById(2)).thenReturn(Optional.of(conductor(false)));

        assertThrows(InactiveDriverException.class, () -> envioService.crearEnvio(request("10")));
    }

    @Test
    void obtenerEnvio_Existente_RetornaDTO() {
        when(envioRepository.findDetailedById(10)).thenReturn(Optional.of(envio("PENDIENTE")));

        EnvioResponseDTO result = envioService.obtenerEnvio(10);

        assertEquals("EXP-0001", result.codigoRastreo());
    }

    @Test
    void obtenerEnvio_Inexistente_LanzaExcepcion() {
        when(envioRepository.findDetailedById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> envioService.obtenerEnvio(99));
    }

    @Test
    void listarOptimizados_RetornaListaDeDTOs() {
        when(envioRepository.findAllOptimized()).thenReturn(List.of(envio("PENDIENTE"), envio("EN_TRANSITO")));

        List<EnvioResponseDTO> result = envioService.listarOptimizados();

        assertEquals(2, result.size());
        assertEquals("EN_TRANSITO", result.get(1).estadoEnvio());
    }

    @Test
    void actualizarEstado_TransicionValida_RegistraBitacora() {
        authenticateAs("operator");
        when(envioRepository.findDetailedById(10)).thenReturn(Optional.of(envio("PENDIENTE")));
        when(usuarioRepository.findByUsername("operator")).thenReturn(Optional.of(usuario("operator")));
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EnvioResponseDTO result =
                envioService.actualizarEstado(10, new CambioEstadoDTO("EN_TRANSITO", "Left the warehouse"));

        assertEquals("EN_TRANSITO", result.estadoEnvio());
        ArgumentCaptor<BitacoraEnvio> captor = ArgumentCaptor.forClass(BitacoraEnvio.class);
        verify(bitacoraRepository).save(captor.capture());
        BitacoraEnvio entry = captor.getValue();
        assertEquals("PENDIENTE", entry.getEstadoAnterior());
        assertEquals("EN_TRANSITO", entry.getEstadoNuevo());
        assertEquals("operator", entry.getUsuario().getUsername());
        assertEquals("Left the warehouse", entry.getObservaciones());
    }

    @Test
    void actualizarEstado_TransicionInvalida_LanzaExcepcion() {
        when(envioRepository.findDetailedById(10)).thenReturn(Optional.of(envio("ENTREGADO")));

        InvalidStateTransitionException exception = assertThrows(
                InvalidStateTransitionException.class,
                () -> envioService.actualizarEstado(10, new CambioEstadoDTO("EN_TRANSITO", null)));

        assertTrue(exception.getMessage().contains("EXP-0001"));
        verify(envioRepository, never()).save(any());
        verifyNoInteractions(bitacoraRepository);
    }

    @Test
    void actualizarEstado_EstadoDesconocido_LanzaExcepcion() {
        when(envioRepository.findDetailedById(10)).thenReturn(Optional.of(envio("PENDIENTE")));

        assertThrows(BusinessRuleException.class,
                () -> envioService.actualizarEstado(10, new CambioEstadoDTO("UNKNOWN", null)));
    }

    @Test
    void actualizarEstado_EnvioInexistente_LanzaExcepcion() {
        when(envioRepository.findDetailedById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.actualizarEstado(99, new CambioEstadoDTO("EN_TRANSITO", null)));
    }

    @Test
    void actualizarEstado_SinUsuarioAutenticado_LanzaExcepcion() {
        when(envioRepository.findDetailedById(10)).thenReturn(Optional.of(envio("PENDIENTE")));

        assertThrows(AccessDeniedException.class,
                () -> envioService.actualizarEstado(10, new CambioEstadoDTO("EN_TRANSITO", null)));

        verify(envioRepository, never()).save(any());
    }

    @Test
    void actualizarEstado_UsuarioNoRegistrado_LanzaExcepcion() {
        authenticateAs("ghost");
        when(envioRepository.findDetailedById(10)).thenReturn(Optional.of(envio("PENDIENTE")));
        when(usuarioRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> envioService.actualizarEstado(10, new CambioEstadoDTO("EN_TRANSITO", null)));
    }

    @Test
    void cancelarEnvio_EnvioEnTransito_LanzaExcepcion() {
        when(envioRepository.findDetailedById(10)).thenReturn(Optional.of(envio("EN_TRANSITO")));

        assertThrows(InvalidStateTransitionException.class, () -> envioService.cancelarEnvio(10, "Customer request"));

        verify(envioRepository, never()).save(any());
    }

    @Test
    void cancelarEnvio_EnvioPendiente_CambiaEstadoACancelado() {
        authenticateAs("admin");
        when(envioRepository.findDetailedById(10)).thenReturn(Optional.of(envio("PENDIENTE")));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario("admin")));
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EnvioResponseDTO result = envioService.cancelarEnvio(10, "Customer request");

        assertEquals("CANCELADO", result.estadoEnvio());
        verify(bitacoraRepository).save(any(BitacoraEnvio.class));
    }

    @Test
    void obtenerBitacora_EnvioExistente_RetornaHistorial() {
        BitacoraEnvio entry = new BitacoraEnvio();
        entry.setId(1);
        entry.setEstadoAnterior("PENDIENTE");
        entry.setEstadoNuevo("EN_TRANSITO");
        entry.setFechaCambio(LocalDateTime.now());
        entry.setUsuario(usuario("operator"));
        when(envioRepository.existsById(10)).thenReturn(true);
        when(bitacoraRepository.findHistoryByEnvioId(10)).thenReturn(List.of(entry));

        List<BitacoraResponseDTO> result = envioService.obtenerBitacora(10);

        assertEquals(1, result.size());
        assertEquals("operator", result.get(0).usuario());
    }

    @Test
    void obtenerBitacora_EnvioInexistente_LanzaExcepcion() {
        when(envioRepository.existsById(99)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> envioService.obtenerBitacora(99));
        verifyNoInteractions(bitacoraRepository);
    }

    @ParameterizedTest
    @CsvSource({
        "5.0, 10.0, 2500.0",
        "15.0, 50.0, 7500.0",
        "100.0, 2.5, 12000.0"
    })
    @DisplayName("Debe calcular la tarifa correcta segun peso y distancia")
    void calcularTarifa_CasosVariados_CalculaCorrectamente(
            double pesoKg, double distanciaKm, double tarifaEsperada) {
        double tarifaCalculada = envioService.calcularTarifa(pesoKg, distanciaKm);

        assertEquals(tarifaEsperada, tarifaCalculada, 0.01);
    }

    @ParameterizedTest
    @CsvSource({
        "50.0, 10.0, 20500.0",
        "50.01, 10.0, 8001.10"
    })
    @DisplayName("Applies the heavy tariff only above the 50 kg threshold")
    void calcularTarifa_LimiteDePeso_AplicaTarifaCorrecta(double pesoKg, double distanciaKm, double tarifaEsperada) {
        assertEquals(tarifaEsperada, envioService.calcularTarifa(pesoKg, distanciaKm), 0.01);
    }

    @ParameterizedTest
    @CsvSource({
        "0.0, 10.0",
        "5.0, 0.0",
        "-1.0, 5.0",
        "5.0, -3.0"
    })
    @DisplayName("Rejects non positive weight or distance")
    void calcularTarifa_ValoresInvalidos_LanzaExcepcion(double pesoKg, double distanciaKm) {
        assertThrows(IllegalArgumentException.class, () -> envioService.calcularTarifa(pesoKg, distanciaKm));
    }
}
