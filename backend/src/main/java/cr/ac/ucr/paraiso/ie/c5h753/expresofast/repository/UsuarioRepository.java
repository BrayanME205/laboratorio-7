package cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsername(String username);
}
