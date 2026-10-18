package cr.ac.ucr.paraiso.ie.c5h753.expresofast.service;

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
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final ConductorRepository conductorRepository;
    private final EmpresaLogisticaRepository empresaRepository;

    public VehiculoService(
            VehiculoRepository vehiculoRepository,
            ConductorRepository conductorRepository,
            EmpresaLogisticaRepository empresaRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.conductorRepository = conductorRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public VehiculoResponseDTO registrarVehiculo(VehiculoRequestDTO request) {
        if (vehiculoRepository.existsByPlaca(request.placa())) {
            throw new DuplicateResourceException("License plate already registered: " + request.placa());
        }
        EmpresaLogistica empresa = empresaRepository.findById(request.empresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id " + request.empresaId()));

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setPlaca(request.placa());
        vehiculo.setMarca(request.marca());
        vehiculo.setModelo(request.modelo());
        vehiculo.setCapacidadKg(request.capacidadKg());
        vehiculo.setEmpresa(empresa);
        return VehiculoResponseDTO.from(vehiculoRepository.save(vehiculo));
    }

    @Transactional
    public VehiculoResponseDTO asignarConductor(Integer vehiculoId, Integer conductorId) {
        Vehiculo vehiculo = findVehiculo(vehiculoId);
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id " + conductorId));
        if (!Boolean.TRUE.equals(conductor.getActivo())) {
            throw new InactiveDriverException("Driver is inactive: " + conductor.getNombreCompleto());
        }
        vehiculo.setConductor(conductor);
        return VehiculoResponseDTO.from(vehiculoRepository.save(vehiculo));
    }

    @Transactional(readOnly = true)
    public List<VehiculoResponseDTO> listarVehiculos() {
        return vehiculoRepository.findAllDetailed().stream().map(VehiculoResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public VehiculoResponseDTO obtenerVehiculo(Integer id) {
        return VehiculoResponseDTO.from(findVehiculo(id));
    }

    @Transactional
    public void eliminarVehiculo(Integer id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vehicle not found with id " + id);
        }
        vehiculoRepository.deleteById(id);
    }

    private Vehiculo findVehiculo(Integer id) {
        return vehiculoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id " + id));
    }
}
