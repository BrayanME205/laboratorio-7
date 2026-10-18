package cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.BusinessRuleException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EstadoEnvioTest {

    @ParameterizedTest
    @CsvSource({
        "PENDIENTE, EN_TRANSITO, true",
        "PENDIENTE, CANCELADO, true",
        "PENDIENTE, ENTREGADO, false",
        "PENDIENTE, PENDIENTE, false",
        "EN_TRANSITO, ENTREGADO, true",
        "EN_TRANSITO, CANCELADO, false",
        "EN_TRANSITO, PENDIENTE, false",
        "ENTREGADO, PENDIENTE, false",
        "ENTREGADO, EN_TRANSITO, false",
        "CANCELADO, PENDIENTE, false",
        "CANCELADO, EN_TRANSITO, false"
    })
    void canTransitionTo_AllCombinations_FollowsBusinessRules(
            EstadoEnvio current, EstadoEnvio next, boolean expected) {
        assertEquals(expected, current.canTransitionTo(next));
    }

    @ParameterizedTest
    @CsvSource({"PENDIENTE", "EN_TRANSITO", "ENTREGADO", "CANCELADO"})
    void from_ValidName_ReturnsEnum(String name) {
        assertEquals(EstadoEnvio.valueOf(name), EstadoEnvio.from(name));
    }

    @ParameterizedTest
    @CsvSource({"UNKNOWN", "pendiente"})
    void from_InvalidName_ThrowsException(String name) {
        assertThrows(BusinessRuleException.class, () -> EstadoEnvio.from(name));
    }
}
