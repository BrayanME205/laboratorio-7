package cr.ac.ucr.paraiso.ie.c5h753.expresofast.security;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.domain.Usuario;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        List<GrantedAuthority> authorities = usuario.getRoles().stream()
                .<GrantedAuthority>map(rol -> new SimpleGrantedAuthority(rol.getNombreRol()))
                .toList();
        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .authorities(authorities)
                .build();
    }
}
