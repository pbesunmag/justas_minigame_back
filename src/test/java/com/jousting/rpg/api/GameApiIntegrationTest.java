package com.jousting.rpg.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jousting.rpg.api.dto.CreateSaveRequest;
import com.jousting.rpg.api.dto.StartBattleRequest;
import com.jousting.rpg.api.dto.TurnActionRequest;
import com.jousting.rpg.domain.enums.HorseOrder;
import com.jousting.rpg.domain.enums.RiderAction;
import com.jousting.rpg.domain.enums.TargetZone;
import com.jousting.rpg.dto.RegisterRequest;
import com.jousting.rpg.repository.SaveGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SaveGameRepository saveGameRepository;

    @BeforeEach
    void setUp() {
        saveGameRepository.clear();
    }

    @Test
    @DisplayName("Flujo completo E2E: Registro -> JWT -> Partida -> Armería -> Batalla -> Turno -> Seguridad 403 y errores 400/404")
    void fullGameFlowAndSecurityTest() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        String userOne = "jugadorUno_" + suffix;
        String userTwo = "jugadorDos_" + suffix;

        // 1. Registrar usuario 1 y obtener JWT
        RegisterRequest reg1 = new RegisterRequest(userOne, "ClaveSegura123");
        MvcResult authRes1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username").value(userOne))
                .andReturn();

        String jwt1 = objectMapper.readTree(authRes1.getResponse().getContentAsString()).get("token").asText();

        // 2. Crear una nueva partida con personalización completa
        CreateSaveRequest createSaveReq = new CreateSaveRequest(
                "Caballero Prueba", 25, "ATHLETIC", "OLIVE", "LONG", "BLACK",
                "Trueno", "BLACK", "arm_chain", "hrs_tourney"
        );
        MvcResult saveRes = mockMvc.perform(post("/api/saves")
                        .header("Authorization", "Bearer " + jwt1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSaveReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.knightName").value("Caballero Prueba"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.bodyType").value("ATHLETIC"))
                .andExpect(jsonPath("$.skinTone").value("OLIVE"))
                .andExpect(jsonPath("$.hairStyle").value("LONG"))
                .andExpect(jsonPath("$.hairColor").value("BLACK"))
                .andExpect(jsonPath("$.horseName").value("Trueno"))
                .andExpect(jsonPath("$.horseColor").value("BLACK"))
                .andExpect(jsonPath("$.level").value(1))
                .andExpect(jsonPath("$.gold").value(0))
                .andExpect(jsonPath("$.maxHp").value(100))
                .andExpect(jsonPath("$.mainWeaponId").value("BASIC_LANCE"))
                .andExpect(jsonPath("$.lastSavedAt", notNullValue()))
                .andReturn();

        Long saveId = objectMapper.readTree(saveRes.getResponse().getContentAsString()).get("id").asLong();

        // 2b. Listar partidas del usuario
        mockMvc.perform(get("/api/saves")
                        .header("Authorization", "Bearer " + jwt1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(saveId));

        // 3. Consultar armería
        mockMvc.perform(get("/api/saves/{saveId}/armory", saveId)
                        .header("Authorization", "Bearer " + jwt1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weapons", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.armors", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.horses", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.weapons[0].id", notNullValue()))
                .andExpect(jsonPath("$.weapons[0].name", notNullValue()))
                .andExpect(jsonPath("$.weapons[0].stats", notNullValue()))
                .andExpect(jsonPath("$.weapons[0].status", notNullValue()));

        // 3b. Regla de negocio: intentar comprar un arma bloqueada o sin oro -> 400 Bad Request
        mockMvc.perform(post("/api/saves/{saveId}/armory/buy/{itemId}", saveId, "MACE_WAR")
                        .header("Authorization", "Bearer " + jwt1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        // 4. Iniciar batalla contra Caballero 1 (KNIGHT_1)
        StartBattleRequest startBattleReq = new StartBattleRequest("KNIGHT_1");
        MvcResult battleStartRes = mockMvc.perform(post("/api/saves/{saveId}/battles/start", saveId)
                        .header("Authorization", "Bearer " + jwt1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(startBattleReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.battleId", notNullValue()))
                .andExpect(jsonPath("$.turnNumber").value(0))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.player.name").value("Caballero Prueba"))
                .andExpect(jsonPath("$.player.currentHp").value(100.0))
                .andExpect(jsonPath("$.player.activeWeapon", notNullValue()))
                .andExpect(jsonPath("$.enemy.name", notNullValue()))
                .andExpect(jsonPath("$.turnNarrative", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.cassianTip", notNullValue()))
                .andReturn();

        JsonNode battleJson = objectMapper.readTree(battleStartRes.getResponse().getContentAsString());
        String battleId = battleJson.get("battleId").asText();

        // 5. Ejecutar un turno de combate
        TurnActionRequest turnReq = new TurnActionRequest(
                RiderAction.ATTACK,
                TargetZone.CHEST,
                HorseOrder.ACCELERATE,
                null
        );

        mockMvc.perform(post("/api/saves/{saveId}/battles/{battleId}/turn", saveId, battleId)
                        .header("Authorization", "Bearer " + jwt1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(turnReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.battleId").value(battleId))
                .andExpect(jsonPath("$.turnNumber").value(1))
                .andExpect(jsonPath("$.cassianTip", notNullValue()))
                .andExpect(jsonPath("$.player.currentSpeed", notNullValue()))
                .andExpect(jsonPath("$.player.currentFatigue", notNullValue()))
                .andExpect(jsonPath("$.player.currentShieldHp", notNullValue()))
                .andExpect(jsonPath("$.player.ultimateCharge", notNullValue()))
                .andExpect(jsonPath("$.enemy.currentHp", notNullValue()))
                .andExpect(jsonPath("$.turnNarrative", hasSize(greaterThan(0))));

        // 5b. Consultar estado actual de la batalla
        mockMvc.perform(get("/api/saves/{saveId}/battles/{battleId}", saveId, battleId)
                        .header("Authorization", "Bearer " + jwt1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.battleId").value(battleId))
                .andExpect(jsonPath("$.turnNumber").value(1))
                .andExpect(jsonPath("$.cassianTip", notNullValue()));

        // 6. Registrar usuario 2 e intentar acceder a la partida del usuario 1 -> 403 Forbidden
        RegisterRequest reg2 = new RegisterRequest(userTwo, "OtraClave456");
        MvcResult authRes2 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg2)))
                .andExpect(status().isCreated())
                .andReturn();

        String jwt2 = objectMapper.readTree(authRes2.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(get("/api/saves/{saveId}", saveId)
                        .header("Authorization", "Bearer " + jwt2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        mockMvc.perform(get("/api/saves/{saveId}/armory", saveId)
                        .header("Authorization", "Bearer " + jwt2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(get("/api/saves/{saveId}/battles/{battleId}", saveId, battleId)
                        .header("Authorization", "Bearer " + jwt2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        // 7. Recurso no encontrado -> 404 Not Found
        mockMvc.perform(get("/api/saves/{saveId}", 99999L)
                        .header("Authorization", "Bearer " + jwt1))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        // 8. Eliminar partida del usuario 1 -> 204 No Content
        mockMvc.perform(delete("/api/saves/{saveId}", saveId)
                        .header("Authorization", "Bearer " + jwt1))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Swagger / OpenAPI docs accesible sin autenticación y con esquema Bearer JWT")
    void openApiDocsShouldBePublicWithBearerScheme() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes['Bearer Authentication'].type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes['Bearer Authentication'].scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes['Bearer Authentication'].bearerFormat").value("JWT"));
    }
}
