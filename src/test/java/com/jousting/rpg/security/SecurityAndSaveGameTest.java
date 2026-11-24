package com.jousting.rpg.security;

import com.jousting.rpg.domain.factory.KnightFactory;
import com.jousting.rpg.domain.model.Knight;
import com.jousting.rpg.domain.model.SaveGame;
import com.jousting.rpg.domain.model.UserAccount;
import com.jousting.rpg.dto.AuthResponse;
import com.jousting.rpg.dto.LoginRequest;
import com.jousting.rpg.dto.RegisterRequest;
import com.jousting.rpg.repository.SaveGameRepository;
import com.jousting.rpg.repository.UserAccountRepository;
import com.jousting.rpg.service.SaveGameService;
import com.jousting.rpg.controller.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityAndSaveGameTest {

    private UserAccountRepository userAccountRepository;
    private SaveGameRepository saveGameRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private SaveGameService saveGameService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        userAccountRepository = new UserAccountRepository();
        saveGameRepository = new SaveGameRepository();
        passwordEncoder = new BCryptPasswordEncoder();
        jwtService = new JwtService();

        // Inyectar propiedades de test para JwtService
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        saveGameService = new SaveGameService(saveGameRepository);

        // Simulador de AuthenticationManager basado en UserAccountRepository y BCrypt
        AuthenticationManager authenticationManager = authentication -> {
            String username = authentication.getName();
            String rawPassword = authentication.getCredentials().toString();

            UserAccount user = userAccountRepository.findByUsername(username)
                    .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));

            if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                throw new BadCredentialsException("Contraseña incorrecta");
            }

            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        };

        authController = new AuthController(userAccountRepository, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    @DisplayName("Cifrado de contraseñas con BCrypt y generación de token JWT")
    void shouldEncryptPasswordAndGenerateValidJwt() {
        String rawPass = "Excalibur123!";
        String encoded = passwordEncoder.encode(rawPass);

        assertNotEquals(rawPass, encoded, "La contraseña jamás debe guardarse en texto plano.");
        assertTrue(passwordEncoder.matches(rawPass, encoded), "BCrypt debe validar la contraseña en texto plano.");

        UserAccount user = new UserAccount("SirLancelot", encoded);
        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("SirLancelot", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, user));

        // Validar que no sea válido para otro usuario
        UserDetails intruder = new UserAccount("Impostor", encoded);
        assertFalse(jwtService.isTokenValid(token, intruder));
    }

    @Test
    @DisplayName("Registro y login mediante AuthController con emisión de tokens JWT")
    void shouldRegisterAndLoginSuccessfully() {
        // 1. Registro
        RegisterRequest registerReq = new RegisterRequest("ArturoPendragon", "CoronaReal2026");
        ResponseEntity<?> registerRes = authController.register(registerReq);

        assertEquals(HttpStatus.CREATED, registerRes.getStatusCode());
        assertTrue(registerRes.getBody() instanceof AuthResponse);
        AuthResponse registerAuth = (AuthResponse) registerRes.getBody();
        assertNotNull(registerAuth.getToken());
        assertEquals("ArturoPendragon", registerAuth.getUsername());

        // 2. Intento de registro duplicado -> conflicto 409
        ResponseEntity<?> duplicateRes = authController.register(registerReq);
        assertEquals(HttpStatus.CONFLICT, duplicateRes.getStatusCode());

        // 3. Login exitoso
        LoginRequest loginReq = new LoginRequest("ArturoPendragon", "CoronaReal2026");
        ResponseEntity<?> loginRes = authController.login(loginReq);

        assertEquals(HttpStatus.OK, loginRes.getStatusCode());
        AuthResponse loginAuth = (AuthResponse) loginRes.getBody();
        assertNotNull(loginAuth.getToken());
        assertEquals("ArturoPendragon", loginAuth.getUsername());

        // 4. Login con contraseña fallida -> excepción BadCredentialsException
        LoginRequest badLogin = new LoginRequest("ArturoPendragon", "ClaveErronea");
        assertThrows(BadCredentialsException.class, () -> authController.login(badLogin));
    }

    @Test
    @DisplayName("Protección estricta de propiedad en SaveGameService contra accesos ilegítimos")
    void shouldEnforceOwnershipOnSaveGames() {
        Knight lancelot = KnightFactory.createProtagonist("Lancelot");

        // SirLancelot guarda su progreso
        SaveGame saveGame = saveGameService.createSaveGame("SirLancelot", "Partida Final Torneo", lancelot);
        assertNotNull(saveGame.getId());

        // 1. Acceso legítimo por el dueño del token JWT
        SaveGame retrieved = saveGameService.getVerifiedSaveGame(saveGame.getId(), "SirLancelot");
        assertNotNull(retrieved);
        assertEquals("SirLancelot", retrieved.getOwnerUsername());
        assertEquals("Partida Final Torneo", retrieved.getSaveName());

        // 2. Acceso ilegítimo por otro usuario -> AccessDeniedException
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                saveGameService.getVerifiedSaveGame(saveGame.getId(), "CaballeroNegro")
        );
        assertTrue(exception.getMessage().contains("Acceso denegado"),
                "Debe denegar el acceso a partidas de otros jugadores.");

        // 3. Consulta de partida inexistente
        assertThrows(java.util.NoSuchElementException.class, () ->
                saveGameService.getVerifiedSaveGame(999L, "SirLancelot")
        );
    }

    public static void main(String[] args) {
        SecurityAndSaveGameTest test = new SecurityAndSaveGameTest();
        test.setUp();
        test.shouldEncryptPasswordAndGenerateValidJwt();
        test.shouldRegisterAndLoginSuccessfully();
        test.shouldEnforceOwnershipOnSaveGames();
        System.out.println("¡TODAS LAS PRUEBAS DE SEGURIDAD Y JWT SUPERADAS CON ÉXITO!");
    }
}
