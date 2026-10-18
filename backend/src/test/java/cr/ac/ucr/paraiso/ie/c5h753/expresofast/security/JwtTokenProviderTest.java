package cr.ac.ucr.paraiso.ie.c5h753.expresofast.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class JwtTokenProviderTest {

    private static final String SECRET = "unit-test-secret-key-with-more-than-thirty-two-characters";

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRET, 60_000L);

    private static Authentication authentication() {
        return new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void generateToken_TokenGenerado_EsValidoYContieneUsuario() {
        String token = provider.generateToken(authentication());

        assertTrue(provider.validateToken(token));
        assertEquals("admin", provider.getUsername(token));
        assertTrue(provider.getExpirationTime(token) > System.currentTimeMillis());
    }

    @Test
    void validateToken_TokenAlterado_RetornaFalse() {
        String token = provider.generateToken(authentication());

        assertFalse(provider.validateToken(token + "tampered"));
    }

    @Test
    void validateToken_TokenMalformadoOVacio_RetornaFalse() {
        assertFalse(provider.validateToken("not-a-jwt"));
        assertFalse(provider.validateToken(""));
    }

    @Test
    void validateToken_TokenExpirado_RetornaFalse() {
        JwtTokenProvider expiringProvider = new JwtTokenProvider(SECRET, -1_000L);

        assertFalse(provider.validateToken(expiringProvider.generateToken(authentication())));
    }

    @Test
    void validateToken_FirmadoConOtraClave_RetornaFalse() {
        JwtTokenProvider otherProvider =
                new JwtTokenProvider("another-secret-key-with-more-than-thirty-two-chars!!", 60_000L);

        assertFalse(provider.validateToken(otherProvider.generateToken(authentication())));
    }
}
