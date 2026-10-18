package cr.ac.ucr.paraiso.ie.c5h753.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EnvioDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.MetricaEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.PaginaDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.service.EnvioService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/envios")
public class EnvioPaginadoController {

    private final EnvioService envioService;

    public EnvioPaginadoController(EnvioService envioService) {
        this.envioService = envioService;
    }

    @GetMapping
    public ResponseEntity<PaginaDTO<EnvioDTO>> listarPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "fechaCreacion") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(PaginaDTO.from(
                envioService.listarPaginado(page, size, sortBy, direction, busqueda, estado)));
    }

    @GetMapping("/procedimiento/{estado}")
    public ResponseEntity<List<EnvioDTO>> listarPorProcedimiento(@PathVariable String estado) {
        return ResponseEntity.ok(envioService.listarViaStoredProcedure(estado));
    }

    @GetMapping("/metricas")
    public ResponseEntity<List<MetricaEstadoDTO>> resumenMetricas() {
        return ResponseEntity.ok(envioService.resumenMetricas());
    }
}