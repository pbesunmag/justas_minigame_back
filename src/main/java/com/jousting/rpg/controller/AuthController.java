package com.jousting.rpg.controller;

import com.jousting.rpg.domain.model.UserAccount;
import com.jousting.rpg.dto.AuthResponse;
import com.jousting.rpg.dto.LoginRequest;
import com.jousting.rpg.dto.RegisterRequest;
import com.jousting.rpg.repository.UserAccountRepository;
import com.jousting.rpg.security.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para registro e inicio de sesión de jugadores mediante JWT.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro e inicio de sesión con emisión de tokens JWT")
public class AuthController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserAccountRepository userAccountRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty() ||
            request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El nombre de usuario y la contraseña son obligatorios.");
        }

        String username = request.getUsername().trim();

        if (userAccountRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El nombre de usuario ya está registrado en el reino.");
        }

        // Cifrado de contraseña con BCrypt
        String encodedPassword = passwordEncoder.encode(request.getPassword().trim());
        UserAccount newUser = new UserAccount(username, encodedPassword);
        userAccountRepository.save(newUser);

        // Auto-login: emisión directa de token JWT
        String token = jwtService.generateToken(newUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, newUser.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body("Debe ingresar usuario y contraseña.");
        }

        String username = request.getUsername().trim();

        // Autenticación con AuthenticationManager de Spring Security
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword().trim())
        );

        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
    }
}
