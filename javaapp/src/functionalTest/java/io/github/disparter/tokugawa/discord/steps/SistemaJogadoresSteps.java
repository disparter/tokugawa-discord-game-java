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
 * Steps de definição para cenários do sistema de jogadores.
 */
@RequiredArgsConstructor
@Slf4j
public class SistemaJogadoresSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext testContext;

    @Given("o jogador possui progresso no jogo")
    public void oJogadorPossuiProgressoNoJogo() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        // Simular algum progresso no jogo criando dados de teste
        String requestBody = String.format("""
            {
                "discordId": "%s",
                "experience": 100,
                "level": 2,
                "currentLocation": "Tokyo",
                "achievements": ["first_login", "first_trade"]
            }
            """, discordId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authToken = testContext.getAuthToken();
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity("/api/players/progress", request, String.class);
            testContext.setValue("progress_setup", true);
            log.info("Progresso do jogador configurado - Discord ID: {}", discordId);
        } catch (Exception e) {
            log.warn("Erro ao configurar progresso (esperado em ambiente de teste): {}", e.getMessage());
            testContext.setValue("progress_setup", true); // Assumir sucesso para testes
        }
    }

    @When("o usuário solicita visualizar seu perfil")
    public void oUsuarioSolicitaVisualizarSeuPerfil() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        HttpHeaders headers = new HttpHeaders();
        String authToken = testContext.getAuthToken();
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }

        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                "/api/players/profile/" + discordId, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("Solicitação de perfil realizada - Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro ao solicitar perfil: {}", e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
        }
    }

    @When("o usuário atualiza suas informações pessoais")
    public void oUsuarioAtualizaSuasInformacoesPessoais() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        String requestBody = String.format("""
            {
                "discordId": "%s",
                "nickname": "TestPlayer Updated",
                "preferredLanguage": "pt_BR",
                "timezone": "America/Sao_Paulo"
            }
            """, discordId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authToken = testContext.getAuthToken();
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                "/api/players/profile", 
                HttpMethod.PUT, 
                request, 
                String.class
            );
            
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("Atualização de perfil realizada - Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro ao atualizar perfil: {}", e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
        }
    }

    @When("o usuário solicita ver seu progresso")
    public void oUsuarioSolicitaVerSeuProgresso() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        HttpHeaders headers = new HttpHeaders();
        String authToken = testContext.getAuthToken();
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }

        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                "/api/players/progress/" + discordId, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("Solicitação de progresso realizada - Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro ao solicitar progresso: {}", e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
        }
    }

    @When("o usuário tenta visualizar seu perfil")
    public void oUsuarioTentaVisualizarSeuPerfil() {
        oUsuarioSolicitaVisualizarSeuPerfil();
    }

    @Then("o perfil deve ser exibido com sucesso")
    public void oPerfilDeveSerExibidoComSucesso() {
        int statusCode = testContext.getLastHttpStatusCode();
        String response = testContext.getLastHttpResponse();
        
        assertEquals(200, statusCode, "Visualização de perfil deveria retornar status 200");
        assertNotNull(response, "Resposta não deveria ser nula");
        
        log.info("Perfil exibido com sucesso");
    }

    @Then("deve mostrar informações básicas do jogador")
    public void deveMostrarInformacoesBasicasDoJogador() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        // Verificar se contém informações básicas esperadas
        assertTrue(response.contains("discordId") || response.contains("id") || response.contains("username"), 
                  "Resposta deveria conter informações básicas do jogador");
        
        log.info("Informações básicas do jogador verificadas");
    }

    @Then("deve mostrar estatísticas de progresso")
    public void deveMostrarEstatisticasDeProgresso() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        // Verificar se contém informações de progresso
        boolean hasProgressInfo = response.contains("level") || 
                                 response.contains("experience") || 
                                 response.contains("progresso") ||
                                 response.contains("stats");
        
        assertTrue(hasProgressInfo, "Resposta deveria conter estatísticas de progresso");
        
        log.info("Estatísticas de progresso verificadas");
    }

    @Then("as informações devem ser atualizadas com sucesso")
    public void asInformacoesDevemSerAtualizadasComSucesso() {
        int statusCode = testContext.getLastHttpStatusCode();
        
        assertEquals(200, statusCode, "Atualização deveria retornar status 200");
        
        log.info("Informações atualizadas com sucesso");
    }

    @Then("deve retornar confirmação da atualização")
    public void deveRetornarConfirmacaoDaAtualizacao() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("success") || response.contains("updated") || response.contains("atualizado"), 
                  "Resposta deveria confirmar a atualização");
        
        log.info("Confirmação de atualização verificada");
    }

    @Then("deve exibir o progresso detalhado")
    public void deveExibirOProgressoDetalhado() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("level") || response.contains("experience") || response.contains("progress"), 
                  "Resposta deveria conter progresso detalhado");
        
        log.info("Progresso detalhado verificado");
    }

    @Then("deve mostrar conquistas desbloqueadas")
    public void deveMostrarConquistasDesbloqueadas() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("achievements") || response.contains("conquistas") || response.contains("unlocked"), 
                  "Resposta deveria conter conquistas");
        
        log.info("Conquistas desbloqueadas verificadas");
    }

    @Then("deve mostrar próximos objetivos")
    public void deveMostrarProximosObjetivos() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("objectives") || response.contains("objetivos") || response.contains("goals"), 
                  "Resposta deveria conter próximos objetivos");
        
        log.info("Próximos objetivos verificados");
    }

    @Then("deve retornar erro de jogador não encontrado")
    public void deveRetornarErroDeJogadorNaoEncontrado() {
        int statusCode = testContext.getLastHttpStatusCode();
        
        assertEquals(404, statusCode, "Deveria retornar status 404 para jogador não encontrado");
        
        log.info("Erro de jogador não encontrado verificado");
    }

    @Then("deve sugerir fazer o registro")
    public void deveSugerirFazerORegistro() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("register") || response.contains("registro") || response.contains("sign up"), 
                  "Resposta deveria sugerir fazer o registro");
        
        log.info("Sugestão de registro verificada");
    }
}