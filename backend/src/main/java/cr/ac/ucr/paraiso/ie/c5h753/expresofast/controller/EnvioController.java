package cr.ac.ucr.paraiso.ie.c5h753.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.service.EnvioService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/envios")
public class EnvioController {

    private final EnvioService envioService;

    public EnvioController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping("/optimizados")
    public ResponseEntity<List<EnvioResponseDTO>> listarOptimizados() {
        return ResponseEntity.ok(envioService.listarOptimizados());
    }

    @GetMapping("/tarifa")
    public ResponseEntity<Map<String, Double>> calcularTarifa(
            @RequestParam double pesoKg, @RequestParam double distanciaKm) {
        return ResponseEntity.ok(Map.of("tarifa", envioService.calcularTarifa(pesoKg, distanciaKm)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvioResponseDTO> obtenerEnvio(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerEnvio(id));
    }

    @PostMapping
    public ResponseEntity<EnvioResponseDTO> crearEnvio(@Valid @RequestBody EnvioRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(envioService.crearEnvio(request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EnvioResponseDTO> actualizarEstado(
            @PathVariable Integer id, @Valid @RequestBody CambioEstadoDTO request) {
        return ResponseEntity.ok(envioService.actualizarEstado(id, request));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<EnvioResponseDTO> cancelarEnvio(
            @PathVariable Integer id, @RequestParam(required = false) String observaciones) {
        return ResponseEntity.ok(envioService.cancelarEnvio(id, observaciones));
    }

    @GetMapping("/{id}/bitacora")
    public ResponseEntity<List<BitacoraResponseDTO>> obtenerBitacora(@PathVariable Integer id) {
        return ResponseEntity.ok(envioService.obtenerBitacora(id));
    }
}
