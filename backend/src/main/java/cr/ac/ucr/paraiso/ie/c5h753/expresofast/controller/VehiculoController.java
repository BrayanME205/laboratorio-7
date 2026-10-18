package cr.ac.ucr.paraiso.ie.c5h753.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.AsignarConductorDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.VehiculoRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.VehiculoResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.service.VehiculoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public ResponseEntity<List<VehiculoResponseDTO>> listarVehiculos() {
        return ResponseEntity.ok(vehiculoService.listarVehiculos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoResponseDTO> obtenerVehiculo(@PathVariable Integer id) {
        return ResponseEntity.ok(vehiculoService.obtenerVehiculo(id));
    }

    @PostMapping
    public ResponseEntity<VehiculoResponseDTO> registrarVehiculo(@Valid @RequestBody VehiculoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehiculoService.registrarVehiculo(request));
    }

    @PatchMapping("/{id}/conductor")
    public ResponseEntity<VehiculoResponseDTO> asignarConductor(
            @PathVariable Integer id, @Valid @RequestBody AsignarConductorDTO request) {
        return ResponseEntity.ok(vehiculoService.asignarConductor(id, request.conductorId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVehiculo(@PathVariable Integer id) {
        vehiculoService.eliminarVehiculo(id);
        return ResponseEntity.noContent().build();
    }
}
