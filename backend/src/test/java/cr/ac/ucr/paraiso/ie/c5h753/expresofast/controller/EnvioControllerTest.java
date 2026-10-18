package cr.ac.ucr.paraiso.ie.c5h753.expresofast.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.BitacoraResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.CambioEstadoDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EnvioRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.EnvioResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.InvalidStateTransitionException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.ResourceNotFoundException;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.service.EnvioService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EnvioController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnvioControllerTest {

    private static final String VALID_PAYLOAD = """
            {
              "codigoRastreo": "EXP-1234",
              "direccionDestino": "Paraiso, Cartago",
              "pesoKg": 10.5,
              "costo": 2500,
              "vehiculoId": 1,
              "conductorId": 2
            }
            """;

    private static final String INVALID_PAYLOAD = """
            {
              "codigoRastreo": "",
              "direccionDestino": "",
              "pesoKg": -5,
              "costo": null,
              "vehiculoId": null,
              "conductorId": null
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnvioService envioService;

    private static EnvioResponseDTO response(String status) {
        return new EnvioResponseDTO(
                1, "EXP-1234", "Paraiso, Cartago", new BigDecimal("10.5"), new BigDecimal("2500"),
                status, "ABC-123", "Mario Rojas");
    }

    @Test
    void obtenerEnvio_Exitoso_Retorna200ConAtributosJson() throws Exception {
        when(envioService.obtenerEnvio(1)).thenReturn(response("PENDIENTE"));

        mockMvc.perform(get("/api/envios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigoRastreo").value("EXP-1234"))
                .andExpect(jsonPath("$.estadoEnvio").value("PENDIENTE"))
                .andExpect(jsonPath("$.placaVehiculo").value("ABC-123"))
                .andExpect(jsonPath("$.nombreConductor").value("Mario Rojas"));
    }

    @Test
    void obtenerEnvio_NoEncontrado_Retorna404ConFormatoRfc7807() throws Exception {
        when(envioService.obtenerEnvio(99)).thenThrow(new ResourceNotFoundException("Shipment not found with id 99"));

        mockMvc.perform(get("/api/envios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Resource Not Found"))
                .andExpect(jsonPath("$.detail").value("Shipment not found with id 99"));
    }

    @Test
    void listarOptimizados_Retorna200ConLista() throws Exception {
        when(envioService.listarOptimizados()).thenReturn(List.of(response("PENDIENTE"), response("EN_TRANSITO")));

        mockMvc.perform(get("/api/envios/optimizados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].estadoEnvio").value("EN_TRANSITO"));
    }

    @Test
    void crearEnvio_PayloadValido_Retorna201() throws Exception {
        when(envioService.crearEnvio(any(EnvioRequestDTO.class))).thenReturn(response("PENDIENTE"));

        mockMvc.perform(post("/api/envios").contentType(MediaType.APPLICATION_JSON).content(VALID_PAYLOAD))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoRastreo").value("EXP-1234"))
                .andExpect(jsonPath("$.estadoEnvio").value("PENDIENTE"));
    }

    @Test
    void crearEnvio_PayloadInvalido_Retorna400ConListaDeErrores() throws Exception {
        mockMvc.perform(post("/api/envios").contentType(MediaType.APPLICATION_JSON).content(INVALID_PAYLOAD))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.errors[?(@.field == 'codigoRastreo')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'pesoKg')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'vehiculoId')]").exists());
    }

    @Test
    void crearEnvio_CodigoConFormatoInvalido_Retorna400() throws Exception {
        String payload = VALID_PAYLOAD.replace("EXP-1234", "BAD-1");

        mockMvc.perform(post("/api/envios").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'codigoRastreo')]").exists());
    }

    @Test
    void crearEnvio_JsonMalformado_Retorna400() throws Exception {
        mockMvc.perform(post("/api/envios").contentType(MediaType.APPLICATION_JSON).content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void actualizarEstado_Valido_Retorna200() throws Exception {
        when(envioService.actualizarEstado(eq(1), any(CambioEstadoDTO.class))).thenReturn(response("EN_TRANSITO"));

        mockMvc.perform(patch("/api/envios/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nuevoEstado\":\"EN_TRANSITO\",\"observaciones\":\"On route\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoEnvio").value("EN_TRANSITO"));
    }

    @Test
    void actualizarEstado_TransicionInvalida_Retorna400ConMensaje() throws Exception {
        when(envioService.actualizarEstado(eq(1), any(CambioEstadoDTO.class)))
                .thenThrow(new InvalidStateTransitionException("Transición de estado no permitida para el envío EXP-1234"));

        mockMvc.perform(patch("/api/envios/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nuevoEstado\":\"PENDIENTE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Transición de estado no permitida para el envío EXP-1234"));
    }

    @Test
    void actualizarEstado_EstadoInvalido_Retorna400() throws Exception {
        mockMvc.perform(patch("/api/envios/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nuevoEstado\":\"LOST\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'nuevoEstado')]").exists());
    }

    @Test
    void cancelarEnvio_Valido_Retorna200() throws Exception {
        when(envioService.cancelarEnvio(1, "Customer request")).thenReturn(response("CANCELADO"));

        mockMvc.perform(patch("/api/envios/1/cancelar").param("observaciones", "Customer request"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoEnvio").value("CANCELADO"));
    }

    @Test
    void obtenerBitacora_Retorna200ConHistorial() throws Exception {
        BitacoraResponseDTO entry = new BitacoraResponseDTO(
                1, "PENDIENTE", "EN_TRANSITO", LocalDateTime.of(2026, 10, 1, 8, 30), "operator", "On route");
        when(envioService.obtenerBitacora(1)).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/envios/1/bitacora"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estadoAnterior").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].estadoNuevo").value("EN_TRANSITO"))
                .andExpect(jsonPath("$[0].usuario").value("operator"));
    }

    @Test
    void calcularTarifa_Retorna200ConTarifa() throws Exception {
        when(envioService.calcularTarifa(5.0, 10.0)).thenReturn(2500.0);

        mockMvc.perform(get("/api/envios/tarifa").param("pesoKg", "5.0").param("distanciaKm", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tarifa").value(2500.0));
    }

    @Test
    void calcularTarifa_ParametroNoNumerico_Retorna400() throws Exception {
        mockMvc.perform(get("/api/envios/tarifa").param("pesoKg", "abc").param("distanciaKm", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void errorInesperado_Retorna500SinRevelarDetalles() throws Exception {
        when(envioService.obtenerEnvio(1)).thenThrow(new IllegalStateException("database exploded"));

        mockMvc.perform(get("/api/envios/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
    }
}
