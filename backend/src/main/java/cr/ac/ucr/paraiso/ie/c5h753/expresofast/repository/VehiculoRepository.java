package cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Vehiculo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {

    boolean existsByPlaca(String placa);

    @Query("SELECT v FROM Vehiculo v JOIN FETCH v.empresa LEFT JOIN FETCH v.conductor ORDER BY v.placa")
    List<Vehiculo> findAllDetailed();
}
