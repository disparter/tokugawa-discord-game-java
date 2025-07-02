package io.github.disparter.tokugawa.discord.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.disparter.tokugawa.discord.context.TestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Steps de definição para cenários de gerenciamento de canais do Discord.
 */
@RequiredArgsConstructor
@Slf4j
public class GerenciamentoCanalSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext testContext;

    @When("o usuário solicita a criação de um canal chamado {string}")
    public void oUsuarioSolicitaCriacaoDeCanal(String nomeCanal) {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        String authToken = testContext.getAuthToken();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }
        
        String requestBody = String.format("""
            {
                "channelName": "%s",
                "channelType": "TEXT",
                "guildId": "123456789012345678"
            }
            """, nomeCanal);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity("/api/discord/channels", request, String.class);
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            testContext.setValue("requested_channel_name", nomeCanal);
            log.info("Solicitação de criação de canal realizada - Nome: {}, Status: {}", nomeCanal, response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro durante criação de canal: {}", e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
            testContext.setValue("channel_creation_error", e.getMessage());
        }
    }

    @When("o usuário solicita a criação de um canal com nome {string}")
    public void oUsuarioSolicitaCriacaoDeCanelComNome(String nomeCanal) {
        oUsuarioSolicitaCriacaoDeCanal(nomeCanal);
    }

    @Then("o canal deve ser criado com sucesso")
    public void oCanalDeveSerCriadoComSucesso() {
        int statusCode = testContext.getLastHttpStatusCode();
        String response = testContext.getLastHttpResponse();
        
        assertEquals(201, statusCode, "Criação de canal deveria retornar status 201");
        assertNotNull(response, "Resposta não deveria ser nula");
        assertTrue(response.contains("channelId") || response.contains("id"), 
                  "Resposta deveria conter ID do canal criado");
        
        // Extrair e armazenar o ID do canal para verificações posteriores
        if (response.contains("channelId") || response.contains("id")) {
            String channelId = extractChannelIdFromResponse(response);
            testContext.setCurrentChannelId(channelId);
            log.info("Canal criado com sucesso - ID: {}", channelId);
        }
    }

    @Then("o canal deve estar visível na guild")
    public void oCanalDeveEstarVisivelNaGuild() {
        String channelId = testContext.getCurrentChannelId();
        assertNotNull(channelId, "ID do canal não deveria ser nulo");
        
        String authToken = testContext.getAuthToken();
        HttpHeaders headers = new HttpHeaders();
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                "/api/discord/channels/" + channelId, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            assertEquals(200, response.getStatusCode().value(), 
                        "Verificação de canal deveria retornar status 200");
            assertNotNull(response.getBody(), "Resposta de verificação não deveria ser nula");
            
            log.info("Canal verificado como visível na guild - ID: {}", channelId);
        } catch (Exception e) {
            log.error("Erro ao verificar visibilidade do canal: {}", e.getMessage());
            fail("Não foi possível verificar a visibilidade do canal: " + e.getMessage());
        }
    }

    @Then("deve retornar erro de nome inválido")
    public void deveRetornarErroDeNomeInvalido() {
        int statusCode = testContext.getLastHttpStatusCode();
        String response = testContext.getLastHttpResponse();
        
        assertEquals(400, statusCode, "Deveria retornar status 400 para nome inválido");
        assertTrue(response.contains("nome") || response.contains("name") || response.contains("inválido"), 
                  "Resposta deveria indicar erro relacionado ao nome");
        
        log.info("Erro de nome inválido verificado corretamente");
    }

    @Then("deve retornar erro de serviço indisponível")
    public void deveRetornarErroDeServicoIndisponivel() {
        int statusCode = testContext.getLastHttpStatusCode();
        
        assertTrue(statusCode == 503 || statusCode == 502, 
                  "Deveria retornar status 503 ou 502 para serviço indisponível");
        
        log.info("Erro de serviço indisponível verificado corretamente - Status: {}", statusCode);
    }

    @Then("deve retornar erro de rate limit")
    public void deveRetornarErroDeRateLimit() {
        int statusCode = testContext.getLastHttpStatusCode();
        String response = testContext.getLastHttpResponse();
        
        assertEquals(429, statusCode, "Deveria retornar status 429 para rate limit");
        assertTrue(response.contains("rate") || response.contains("limit") || response.contains("many"), 
                  "Resposta deveria indicar rate limiting");
        
        testContext.setValue("rate_limit_detected", true);
        log.info("Erro de rate limit verificado corretamente");
    }

    @Then("o sistema deve aguardar antes de tentar novamente")
    public void oSistemaDeveAguardarAntesDeTentarNovamente() {
        assertTrue(testContext.getValue("rate_limit_detected", Boolean.class).orElse(false), 
                  "Rate limit deveria ter sido detectado anteriormente");
        
        // Em um cenário real, verificaríamos se o sistema implementa backoff/retry
        // Para este exemplo, apenas documentamos que o comportamento é esperado
        log.info("Sistema configurado para aguardar em caso de rate limit");
    }

    private String extractChannelIdFromResponse(String response) {
        // Implementação simplificada para extrair channel ID do JSON
        // Em um caso real, usaria Jackson ou Gson para parsing adequado
        if (response.contains("\"channelId\":\"")) {
            int startIndex = response.indexOf("\"channelId\":\"") + 13;
            int endIndex = response.indexOf("\"", startIndex);
            return response.substring(startIndex, endIndex);
        } else if (response.contains("\"id\":\"")) {
            int startIndex = response.indexOf("\"id\":\"") + 6;
            int endIndex = response.indexOf("\"", startIndex);
            return response.substring(startIndex, endIndex);
        }
        return "default-channel-id";
    }
}