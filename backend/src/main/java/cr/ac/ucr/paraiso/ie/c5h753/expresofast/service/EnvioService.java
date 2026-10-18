package cr.ac.ucr.paraiso.ie.c5h753.expresofast.service;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.BitacoraEnvio;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Conductor;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Envio;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.EstadoEnvio;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Usuario;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Vehiculo;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EnvioResponseDTO;
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
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvioService {

    private static final double HEAVY_THRESHOLD_KG = 50.0;
    private static final double STANDARD_BASE_FEE = 250.0;
    private static final double STANDARD_RATE_PER_KG = 400.0;
    private static final double STANDARD_RATE_PER_KM = 25.0;
    private static final double HEAVY_BASE_FEE = 500.0;
    private static final double HEAVY_RATE_PER_KG = 110.0;
    private static final double HEAVY_RATE_PER_KM = 200.0;

    private final EnvioRepository envioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final BitacoraEnvioRepository bitacoraRepository;
    private final UsuarioRepository usuarioRepository;

    public EnvioService(
            EnvioRepository envioRepository,
            VehiculoRepository vehiculoRepository,
            ConductorRepository conductorRepository,
            BitacoraEnvioRepository bitacoraRepository,
            UsuarioRepository usuarioRepository) {
        this.envioRepository = envioRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.bitacoraRepository = bitacoraRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public EnvioResponseDTO crearEnvio(EnvioRequestDTO request) {
        if (envioRepository.existsByCodigoRastreo(request.codigoRastreo())) {
            throw new DuplicateResourceException("Tracking code already registered: " + request.codigoRastreo());
        }
        Vehiculo vehiculo = vehiculoRepository.findById(request.vehiculoId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id " + request.vehiculoId()));
        if (request.pesoKg().compareTo(vehiculo.getCapacidadKg()) > 0) {
            throw new CapacidadExcedidaException(
                    "Package weight exceeds vehicle capacity of " + vehiculo.getCapacidadKg() + " kg");
        }
        Conductor conductor = conductorRepository.findById(request.conductorId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id " + request.conductorId()));
        if (!Boolean.TRUE.equals(conductor.getActivo())) {
            throw new InactiveDriverException("Driver is inactive: " + conductor.getNombreCompleto());
        }

        Envio envio = new Envio();
        envio.setCodigoRastreo(request.codigoRastreo());
        envio.setDireccionDestino(request.direccionDestino());
        envio.setPesoKg(request.pesoKg());
        envio.setCosto(request.costo());
        envio.setEstadoEnvio(EstadoEnvio.PENDIENTE.name());
        envio.setFechaCreacion(LocalDateTime.now());
        envio.setVehiculo(vehiculo);
        envio.setConductor(conductor);
        return EnvioResponseDTO.from(envioRepository.save(envio));
    }

    @Transactional(readOnly = true)
    public EnvioResponseDTO obtenerEnvio(Integer id) {
        return EnvioResponseDTO.from(findEnvio(id));
    }

    @Transactional(readOnly = true)
    public List<EnvioResponseDTO> listarOptimizados() {
        return envioRepository.findAllOptimized().stream().map(EnvioResponseDTO::from).toList();
    }

    @Transactional
    public EnvioResponseDTO actualizarEstado(Integer id, CambioEstadoDTO request) {
        Envio envio = findEnvio(id);
        EstadoEnvio next = EstadoEnvio.from(request.nuevoEstado());
        return EnvioResponseDTO.from(applyTransition(envio, next, request.observaciones()));
    }

    @Transactional
    public EnvioResponseDTO cancelarEnvio(Integer id, String observaciones) {
        Envio envio = findEnvio(id);
        return EnvioResponseDTO.from(applyTransition(envio, EstadoEnvio.CANCELADO, observaciones));
    }

    @Transactional(readOnly = true)
    public List<BitacoraResponseDTO> obtenerBitacora(Integer id) {
        if (!envioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Shipment not found with id " + id);
        }
        return bitacoraRepository.findHistoryByEnvioId(id).stream().map(BitacoraResponseDTO::from).toList();
    }

    public double calcularTarifa(double pesoKg, double distanciaKm) {
        if (pesoKg <= 0 || distanciaKm <= 0) {
            throw new IllegalArgumentException("Weight and distance must be greater than zero");
        }
        if (pesoKg > HEAVY_THRESHOLD_KG) {
            return HEAVY_BASE_FEE + pesoKg * HEAVY_RATE_PER_KG + distanciaKm * HEAVY_RATE_PER_KM;
        }
        return STANDARD_BASE_FEE + pesoKg * STANDARD_RATE_PER_KG + distanciaKm * STANDARD_RATE_PER_KM;
    }

    private Envio findEnvio(Integer id) {
        return envioRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id " + id));
    }

    private Envio applyTransition(Envio envio, EstadoEnvio next, String observations) {
        EstadoEnvio current = EstadoEnvio.from(envio.getEstadoEnvio());
        if (!current.canTransitionTo(next)) {
            throw new InvalidStateTransitionException(
                    "Transición de estado no permitida para el envío " + envio.getCodigoRastreo());
        }
        Usuario actor = currentUser();

        envio.setEstadoEnvio(next.name());
        Envio saved = envioRepository.save(envio);

        BitacoraEnvio entry = new BitacoraEnvio();
        entry.setEnvio(saved);
        entry.setEstadoAnterior(current.name());
        entry.setEstadoNuevo(next.name());
        entry.setFechaCambio(LocalDateTime.now());
        entry.setUsuario(actor);
        entry.setObservaciones(observations);
        bitacoraRepository.save(entry);
        return saved;
    }

    private Usuario currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user");
        }
        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authentication.getName()));
    }
}
