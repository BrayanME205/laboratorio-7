package cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Envio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnvioRepository extends JpaRepository<Envio, Integer>, EnvioProcedureRepository {

    boolean existsByCodigoRastreo(String codigoRastreo);

    @Query("SELECT e FROM Envio e JOIN FETCH e.vehiculo JOIN FETCH e.conductor ORDER BY e.fechaCreacion DESC")
    List<Envio> findAllOptimized();

    @Query("SELECT e FROM Envio e JOIN FETCH e.vehiculo JOIN FETCH e.conductor WHERE e.id = :id")
    Optional<Envio> findDetailedById(@Param("id") Integer id);

    Page<Envio> findByEstadoEnvio(String estadoEnvio, Pageable pageable);

    @Query("""
            SELECT e FROM Envio e
            WHERE (:estado = '' OR e.estadoEnvio = :estado)
              AND (:busqueda = ''
                   OR LOWER(e.codigoRastreo) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                   OR LOWER(e.destinatario) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                   OR LOWER(e.direccionDestino) LIKE LOWER(CONCAT('%', :busqueda, '%')))
            """)
    Page<Envio> buscar(
            @Param("estado") String estado,
            @Param("busqueda") String busqueda,
            Pageable pageable);
}
