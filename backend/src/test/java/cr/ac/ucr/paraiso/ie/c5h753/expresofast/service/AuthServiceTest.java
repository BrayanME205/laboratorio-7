package cr.ac.ucr.paraiso.ie.c5h753.expresofast.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.security.JwtTokenProvider;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_CredencialesCorrectas_RetornaToken() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt-token");
        when(tokenProvider.getExpirationTime("jwt-token")).thenReturn(1_900_000_000_000L);

        AuthResponseDTO result = authService.login(new AuthRequestDTO("admin", "admin123"));

        assertEquals("jwt-token", result.token());
        assertEquals("admin", result.username());
        assertEquals(List.of("ROLE_ADMIN"), result.roles());
        assertEquals(1_900_000_000_000L, result.expirationTime());
    }

    @Test
    void login_CredencialesIncorrectas_PropagaExcepcion() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new AuthRequestDTO("admin", "wrong")));

        verifyNoInteractions(tokenProvider);
    }
}
