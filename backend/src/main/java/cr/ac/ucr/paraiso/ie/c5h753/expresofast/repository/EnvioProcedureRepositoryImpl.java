package cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Envio;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import java.util.List;

public class EnvioProcedureRepositoryImpl implements EnvioProcedureRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<Envio> obtenerPorEstado(String estado) {
        return entityManager.createStoredProcedureQuery("SP_OBTENER_ENVIOS_POR_ESTADO", Envio.class)
                .registerStoredProcedureParameter(1, String.class, ParameterMode.IN)
                .setParameter(1, estado)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> resumenMetricas() {
        return entityManager.createStoredProcedureQuery("SP_RESUMEN_METRICAS_ENVIOS").getResultList();
    }
}