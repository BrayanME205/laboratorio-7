package cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.BitacoraEnvio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BitacoraEnvioRepository extends JpaRepository<BitacoraEnvio, Integer> {

    @Query("SELECT b FROM BitacoraEnvio b JOIN FETCH b.usuario WHERE b.envio.id = :envioId ORDER BY b.fechaCambio DESC")
    List<BitacoraEnvio> findHistoryByEnvioId(@Param("envioId") Integer envioId);
}
