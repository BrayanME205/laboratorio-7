package cr.ac.ucr.paraiso.ie.c5h753.expresofast.controller;

import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.AuthRequestDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.dto.AuthResponseDTO;
import cr.ac.ucr.paraiso.ie.c5h753.expresofast.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
