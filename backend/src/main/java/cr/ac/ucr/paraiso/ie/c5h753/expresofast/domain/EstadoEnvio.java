package cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.exception.BusinessRuleException;

public enum EstadoEnvio {
    PENDIENTE,
    EN_TRANSITO,
    ENTREGADO,
    CANCELADO;

    public boolean canTransitionTo(EstadoEnvio next) {
        return switch (this) {
            case PENDIENTE -> next == EN_TRANSITO || next == CANCELADO;
            case EN_TRANSITO -> next == ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }

    public static EstadoEnvio from(String value) {
        for (EstadoEnvio status : values()) {
            if (status.name().equals(value)) {
                return status;
            }
        }
        throw new BusinessRuleException("Unknown shipment status: " + value);
    }
}
