package io.github.disparter.tokugawa.discord.steps;

import io.cucumber.java.en.Given;
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
 * Steps de definição para cenários de autenticação e gestão de usuários.
 */
@RequiredArgsConstructor
@Slf4j
public class AutenticacaoSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext testContext;

    @Given("um usuário com Discord ID {string}")
    public void umUsuarioComDiscordId(String discordId) {
        testContext.setValue("discord_id", discordId);
        testContext.setValue("discord_username", "TestUser#0001");
        log.info("Configurado usuário com Discord ID: {}", discordId);
    }

    @Given("o usuário não está registrado no sistema")
    public void oUsuarioNaoEstaRegistradoNoSistema() {
        // Este step serve para documentar o estado inicial
        // O sistema verificará automaticamente se o usuário existe
        testContext.setValue("user_registered", false);
        log.info("Estado definido: usuário não registrado");
    }

    @Given("o usuário já está registrado no sistema")
    public void oUsuarioJaEstaRegistradoNoSistema() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        // Primeiro, registrar o usuário no sistema
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String requestBody = String.format("""
            {
                "discordId": "%s",
                "username": "TestUser",
                "discriminator": "0001"
            }
            """, discordId);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register", request, String.class);
        
        testContext.setLastHttpStatusCode(response.getStatusCode().value());
        testContext.setLastHttpResponse(response.getBody());
        testContext.setValue("user_registered", true);
        
        log.info("Usuário registrado no sistema - Discord ID: {}, Status: {}", discordId, response.getStatusCode());
    }

    @When("o usuário tenta fazer login")
    public void oUsuarioTentaFazerLogin() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String requestBody = String.format("""
            {
                "discordId": "%s"
            }
            """, discordId);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/login", request, String.class);
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("Tentativa de login realizada - Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro durante login: {}", e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
            testContext.setValue("login_error", e.getMessage());
        }
    }

    @When("o usuário tenta se registrar")
    public void oUsuarioTentaSeRegistrar() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        String username = testContext.getStringValue("discord_username").orElse("TestUser#0001");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String requestBody = String.format("""
            {
                "discordId": "%s",
                "username": "%s",
                "discriminator": "0001"
            }
            """, discordId, username.split("#")[0]);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity("/api/auth/register", request, String.class);
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("Tentativa de registro realizada - Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro durante registro: {}", e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
            testContext.setValue("register_error", e.getMessage());
        }
    }

    @Then("o login deve ser bem-sucedido")
    public void oLoginDeveSerBemSucedido() {
        int statusCode = testContext.getLastHttpStatusCode();
        String response = testContext.getLastHttpResponse();
        
        assertEquals(200, statusCode, "Login deveria retornar status 200");
        assertNotNull(response, "Resposta não deveria ser nula");
        assertTrue(response.contains("token") || response.contains("success"), 
                  "Resposta deveria conter token ou indicador de sucesso");
        
        log.info("Login verificado como bem-sucedido");
    }

    @Then("o registro deve ser bem-sucedido")
    public void oRegistroDeveSerBemSucedido() {
        int statusCode = testContext.getLastHttpStatusCode();
        String response = testContext.getLastHttpResponse();
        
        assertEquals(201, statusCode, "Registro deveria retornar status 201");
        assertNotNull(response, "Resposta não deveria ser nula");
        
        testContext.setValue("user_registered", true);
        log.info("Registro verificado como bem-sucedido");
    }

    @Then("deve retornar erro de usuário não encontrado")
    public void deveRetornarErroDeUsuarioNaoEncontrado() {
        int statusCode = testContext.getLastHttpStatusCode();
        
        assertEquals(404, statusCode, "Deveria retornar status 404 para usuário não encontrado");
        log.info("Erro de usuário não encontrado verificado corretamente");
    }

    @Then("deve retornar erro de usuário já existe")
    public void deveRetornarErroDeUsuarioJaExiste() {
        int statusCode = testContext.getLastHttpStatusCode();
        String response = testContext.getLastHttpResponse();
        
        assertEquals(409, statusCode, "Deveria retornar status 409 para usuário já existente");
        assertTrue(response.contains("já existe") || response.contains("already exists"), 
                  "Resposta deveria indicar que usuário já existe");
        
        log.info("Erro de usuário já existente verificado corretamente");
    }

    @Then("o usuário deve receber um token de autenticação")
    public void oUsuarioDeveReceberUmTokenDeAutenticacao() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        // Assumindo que o token está em formato JSON
        assertTrue(response.contains("token"), "Resposta deveria conter um token");
        
        // Extrair e armazenar o token para uso posterior
        // Esta é uma implementação simplificada - em um caso real, usaria parsing JSON adequado
        if (response.contains("\"token\"")) {
            String token = extractTokenFromResponse(response);
            testContext.setAuthToken(token);
            log.info("Token de autenticação extraído e armazenado");
        }
    }

    private String extractTokenFromResponse(String response) {
        // Implementação simplificada para extrair token do JSON
        // Em um caso real, usaria Jackson ou Gson para parsing adequado
        int startIndex = response.indexOf("\"token\":\"") + 9;
        int endIndex = response.indexOf("\"", startIndex);
        return response.substring(startIndex, endIndex);
    }
}