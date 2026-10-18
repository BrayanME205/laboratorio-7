package cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Envio;
import java.util.List;

public interface EnvioProcedureRepository {

    List<Envio> obtenerPorEstado(String estado);

    List<Object[]> resumenMetricas();
}