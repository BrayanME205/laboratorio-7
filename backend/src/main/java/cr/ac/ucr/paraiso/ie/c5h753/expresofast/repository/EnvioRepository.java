package cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Envio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnvioRepository extends JpaRepository<Envio, Integer> {

    boolean existsByCodigoRastreo(String codigoRastreo);

    @Query("SELECT e FROM Envio e JOIN FETCH e.vehiculo JOIN FETCH e.conductor ORDER BY e.fechaCreacion DESC")
    List<Envio> findAllOptimized();

    @Query("SELECT e FROM Envio e JOIN FETCH e.vehiculo JOIN FETCH e.conductor WHERE e.id = :id")
    Optional<Envio> findDetailedById(@Param("id") Integer id);
}
