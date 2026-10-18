package cr.ac.ucr.paraiso.ie.c5h753.expresofast.service;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.EmpresaLogistica;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EmpresaRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EmpresaResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.DuplicateResourceException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.ResourceNotFoundException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository.EmpresaLogisticaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpresaLogisticaService {

    private final EmpresaLogisticaRepository empresaRepository;

    public EmpresaLogisticaService(EmpresaLogisticaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @Transactional
    public EmpresaResponseDTO crearEmpresa(EmpresaRequestDTO request) {
        if (empresaRepository.existsByCedulaJuridica(request.cedulaJuridica())) {
            throw new DuplicateResourceException("Legal ID already registered: " + request.cedulaJuridica());
        }
        EmpresaLogistica empresa = new EmpresaLogistica();
        empresa.setNombre(request.nombre());
        empresa.setCedulaJuridica(request.cedulaJuridica());
        empresa.setTelefono(request.telefono());
        empresa.setActiva(true);
        return EmpresaResponseDTO.from(empresaRepository.save(empresa));
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> listarEmpresas() {
        return empresaRepository.findAll().stream().map(EmpresaResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public EmpresaResponseDTO obtenerEmpresa(Integer id) {
        return empresaRepository.findById(id)
                .map(EmpresaResponseDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id " + id));
    }

    @Transactional
    public void eliminarEmpresa(Integer id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Company not found with id " + id);
        }
        empresaRepository.deleteById(id);
    }
}
