package cr.ac.ucr.paraiso.ie.c5h753.expresofast.service;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.security.JwtTokenProvider;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        String token = tokenProvider.generateToken(authentication);
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new AuthResponseDTO(token, authentication.getName(), roles, tokenProvider.getExpirationTime(token));
    }
}
