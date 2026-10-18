package cr.ac.ucr.paraiso.ie.c5h753.expresofast.config;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.security.CustomUserDetailsService;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.security.JwtAuthenticationFilter;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private static final String ADMIN = "ADMIN";
        private static final String OPERADOR = "OPERADOR";
        private static final String CONDUCTOR = "CONDUCTOR";

        private final JwtTokenProvider tokenProvider;
        private final CustomUserDetailsService userDetailsService;
        private final String[] allowedOrigins;

        public SecurityConfig(
                        JwtTokenProvider tokenProvider,
                        CustomUserDetailsService userDetailsService,
                        @Value("${app.cors.allowed-origins}") String[] allowedOrigins) {
                this.tokenProvider = tokenProvider;
                this.userDetailsService = userDetailsService;
                this.allowedOrigins = allowedOrigins;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(handling -> handling
                                                .authenticationEntryPoint((request, response, ex) -> writeError(
                                                                response, HttpServletResponse.SC_UNAUTHORIZED,
                                                                "Unauthorized",
                                                                "Authentication is required to access this resource"))
                                                .accessDeniedHandler((request, response, ex) -> writeError(response,
                                                                HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                                                                "You do not have permission to access this resource")))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/envios/optimizados")
                                                .hasAnyRole(ADMIN, OPERADOR, CONDUCTOR)
                                                .requestMatchers(HttpMethod.GET, "/api/envios/tarifa")
                                                .hasAnyRole(ADMIN, OPERADOR)
                                                .requestMatchers(HttpMethod.POST, "/api/envios")
                                                .hasAnyRole(ADMIN, OPERADOR)
                                                .requestMatchers(HttpMethod.PATCH, "/api/envios/*/estado")
                                                .hasAnyRole(ADMIN, OPERADOR, CONDUCTOR)
                                                .requestMatchers(HttpMethod.PATCH, "/api/envios/*/cancelar")
                                                .hasAnyRole(ADMIN, OPERADOR)
                                                .requestMatchers(HttpMethod.GET, "/api/envios/*/bitacora")
                                                .hasAnyRole(ADMIN, OPERADOR)
                                                .requestMatchers(HttpMethod.GET, "/api/envios/*")
                                                .hasAnyRole(ADMIN, OPERADOR, CONDUCTOR)
                                                .requestMatchers(HttpMethod.GET, "/api/v1/envios/**")
                                                .hasAnyRole(ADMIN, OPERADOR)
                                                .requestMatchers("/api/vehiculos/**", "/api/empresas/**").hasRole(ADMIN)
                                                .anyRequest().authenticated())
                                .addFilterBefore(
                                                new JwtAuthenticationFilter(tokenProvider, userDetailsService),
                                                UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
                configuration.setMaxAge(3600L);
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        private static void writeError(HttpServletResponse response, int status, String title, String detail)
                        throws IOException {
                response.setStatus(status);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType("application/problem+json");
                response.getWriter().write(String.format(
                                "{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\"}",
                                title, status, detail));
        }
}
