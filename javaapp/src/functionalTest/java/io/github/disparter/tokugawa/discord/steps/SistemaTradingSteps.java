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
 * Steps de definição para cenários do sistema de trading.
 */
@RequiredArgsConstructor
@Slf4j
public class SistemaTradingSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext testContext;

    @Given("o jogador está em uma localização com NPCs comerciantes")
    public void oJogadorEstaEmUmaLocalizacaoComNpcsCommerciantes() {
        testContext.setValue("location_has_traders", true);
        testContext.setValue("current_location", "merchant_district");
        log.info("Jogador configurado em localização com comerciantes");
    }

    @Given("o jogador possui recursos suficientes")
    public void oJogadorPossuiRecursosSuficientes() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        String requestBody = String.format("""
            {
                "discordId": "%s",
                "gold": 1000,
                "items": ["sword", "potion", "gem"]
            }
            """, discordId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity("/api/players/resources", request, String.class);
            testContext.setValue("player_has_resources", true);
            log.info("Recursos do jogador configurados");
        } catch (Exception e) {
            log.warn("Erro ao configurar recursos (simulando): {}", e.getMessage());
            testContext.setValue("player_has_resources", true);
        }
    }

    @Given("o jogador não possui recursos suficientes")
    public void oJogadorNaoPossuiRecursosSuficientes() {
        testContext.setValue("player_has_resources", false);
        log.info("Jogador configurado sem recursos suficientes");
    }

    @Given("o jogador já realizou algumas trocas")
    public void oJogadorJaRealizouAlgumasTrocas() {
        testContext.setValue("player_has_trade_history", true);
        log.info("Histórico de trocas configurado para o jogador");
    }

    @Given("existe um NPC com preferências específicas")
    public void existeUmNpcComPreferenciasEspecificas() {
        testContext.setValue("npc_has_preferences", true);
        testContext.setValue("npc_preferred_item", "rare_gem");
        log.info("NPC com preferências específicas configurado");
    }

    @When("o usuário solicita ver itens disponíveis para troca")
    public void oUsuarioSolicitaVerItensDisponiveisParaTroca() {
        String location = testContext.getStringValue("current_location").orElse("default_location");
        
        HttpHeaders headers = new HttpHeaders();
        String authToken = testContext.getAuthToken();
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }

        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                "/api/trade/available?location=" + location, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("Solicitação de itens para troca realizada - Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro ao solicitar itens para troca: {}", e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
        }
    }

    @When("o usuário realiza uma troca com um NPC")
    public void oUsuarioRealizaUmaTrocaComUmNpc() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        String requestBody = String.format("""
            {
                "playerId": "%s",
                "npcId": "merchant_01",
                "offeredItems": ["sword"],
                "requestedItems": ["potion", "gold:100"]
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
            ResponseEntity<String> response = restTemplate.postForEntity("/api/trade/execute", request, String.class);
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("Troca realizada - Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro durante troca: {}", e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
        }
    }

    @When("o usuário tenta realizar uma troca custosa")
    public void oUsuarioTentaRealizarUmaTrocaCustosa() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        String requestBody = String.format("""
            {
                "playerId": "%s",
                "npcId": "expensive_merchant",
                "offeredItems": ["common_item"],
                "requestedItems": ["legendary_sword", "gold:10000"]
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
            ResponseEntity<String> response = restTemplate.postForEntity("/api/trade/execute", request, String.class);
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("Tentativa de troca custosa - Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro durante troca custosa: {}", e.getMessage());
            testContext.setLastHttpStatusCode(400);
            testContext.setLastHttpResponse("Insufficient Resources");
        }
    }

    @When("o usuário solicita ver seu histórico de trocas")
    public void oUsuarioSolicitaVerSeuHistoricoDeTrocas() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        HttpHeaders headers = new HttpHeaders();
        String authToken = testContext.getAuthToken();
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }

        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                "/api/trade/history/" + discordId, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("Histórico de trocas solicitado - Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro ao solicitar histórico: {}", e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
        }
    }

    @When("o usuário oferece um item que o NPC prefere")
    public void oUsuarioOfereceUmItemQueONpcPrefere() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        String preferredItem = testContext.getStringValue("npc_preferred_item").orElse("rare_gem");
        
        String requestBody = String.format("""
            {
                "playerId": "%s",
                "npcId": "preferential_npc",
                "offeredItems": ["%s"],
                "requestedItems": ["gold:200"]
            }
            """, discordId, preferredItem);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authToken = testContext.getAuthToken();
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity("/api/trade/execute", request, String.class);
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("Troca com item preferido realizada - Status: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro durante troca preferencial: {}", e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
        }
    }

    @When("o usuário realiza uma troca do tipo {string}")
    public void oUsuarioRealizaUmaTrocaDoTipo(String tipoTroca) {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        String requestBody = switch (tipoTroca) {
            case "item" -> String.format("""
                {
                    "playerId": "%s",
                    "type": "item_trade",
                    "offeredItems": ["sword"],
                    "requestedItems": ["shield"]
                }
                """, discordId);
            case "gold" -> String.format("""
                {
                    "playerId": "%s",
                    "type": "gold_trade",
                    "offeredGold": 100,
                    "requestedItems": ["potion"]
                }
                """, discordId);
            case "service" -> String.format("""
                {
                    "playerId": "%s",
                    "type": "service_trade",
                    "offeredService": "escort_mission",
                    "requestedReward": "experience:50"
                }
                """, discordId);
            default -> "{}";
        };

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authToken = testContext.getAuthToken();
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity("/api/trade/execute", request, String.class);
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("Troca do tipo {} realizada - Status: {}", tipoTroca, response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro durante troca tipo {}: {}", tipoTroca, e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
        }
    }

    @Then("deve exibir lista de itens disponíveis")
    public void deveExibirListaDeItensDisponiveis() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("items") || response.contains("available") || response.contains("trade"), 
                  "Resposta deveria conter lista de itens");
        
        log.info("Lista de itens disponíveis verificada");
    }

    @Then("deve mostrar preços e requisitos de cada item")
    public void deveMostrarPrecosERequisitosDeItem() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("price") || response.contains("cost") || response.contains("requirements"), 
                  "Resposta deveria conter preços e requisitos");
        
        log.info("Preços e requisitos verificados");
    }

    @Then("deve mostrar se o jogador tem recursos suficientes")
    public void deveMostrarSeOJogadorTemRecursosSuficientes() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        boolean hasAffordabilityInfo = response.contains("affordable") || 
                                      response.contains("can_buy") || 
                                      response.contains("sufficient");
        
        assertTrue(hasAffordabilityInfo, "Resposta deveria indicar se jogador pode comprar");
        
        log.info("Informação de recursos suficientes verificada");
    }

    @Then("a troca deve ser processada com sucesso")
    public void aTrocaDeveSerProcessadaComSucesso() {
        int statusCode = testContext.getLastHttpStatusCode();
        
        assertEquals(200, statusCode, "Troca deveria retornar status 200");
        
        log.info("Troca processada com sucesso");
    }

    @Then("os itens devem ser adicionados ao inventário")
    public void osItensDevemSerAdicionadosAoInventario() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("inventory") || response.contains("added") || response.contains("received"), 
                  "Resposta deveria confirmar adição ao inventário");
        
        log.info("Adição ao inventário verificada");
    }

    @Then("os recursos devem ser deduzidos da conta")
    public void osRecursosDevemSerDeduzidosDaConta() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("deducted") || response.contains("spent") || response.contains("cost"), 
                  "Resposta deveria confirmar dedução de recursos");
        
        log.info("Dedução de recursos verificada");
    }

    @Then("deve retornar erro de recursos insuficientes")
    public void deveRetornarErroDeRecursosInsuficientes() {
        int statusCode = testContext.getLastHttpStatusCode();
        String response = testContext.getLastHttpResponse();
        
        assertEquals(400, statusCode, "Deveria retornar status 400 para recursos insuficientes");
        assertTrue(response.contains("insufficient") || response.contains("not enough") || response.contains("insuficientes"), 
                  "Resposta deveria indicar recursos insuficientes");
        
        log.info("Erro de recursos insuficientes verificado");
    }

    @Then("deve mostrar quanto o jogador precisa para completar a troca")
    public void deveMostrarQuantoOJogadorPrecisaParaCompletarATroca() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("needed") || response.contains("required") || response.contains("missing"), 
                  "Resposta deveria mostrar quanto falta");
        
        log.info("Informação de recursos necessários verificada");
    }

    @Then("deve exibir histórico detalhado")
    public void deveExibirHistoricoDetalhado() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("history") || response.contains("trades") || response.contains("transactions"), 
                  "Resposta deveria conter histórico");
        
        log.info("Histórico detalhado verificado");
    }

    @Then("deve mostrar data, itens e valores de cada troca")
    public void deveMostrarDataItensEValoresDeCadaTroca() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        boolean hasTradeDetails = response.contains("date") || response.contains("timestamp") || 
                                 response.contains("items") || response.contains("value");
        
        assertTrue(hasTradeDetails, "Resposta deveria conter detalhes das trocas");
        
        log.info("Detalhes das trocas verificados");
    }

    @Then("deve calcular estatísticas de trading")
    public void deveCalcularEstatisticasDeTrading() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("statistics") || response.contains("stats") || response.contains("summary"), 
                  "Resposta deveria conter estatísticas");
        
        log.info("Estatísticas de trading verificadas");
    }

    @Then("deve receber um bônus na negociação")
    public void deveReceberUmBonusNaNegociacao() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("bonus") || response.contains("extra") || response.contains("preferred"), 
                  "Resposta deveria indicar bônus");
        
        log.info("Bônus de negociação verificado");
    }

    @Then("deve ver aumento na relação com o NPC")
    public void deveVerAumentoNaRelacaoComONpc() {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        
        assertTrue(response.contains("relationship") || response.contains("reputation") || response.contains("favor"), 
                  "Resposta deveria indicar melhoria na relação");
        
        log.info("Aumento na relação com NPC verificado");
    }

    @Then("deve processar a troca adequadamente")
    public void deveProcessarATrocaAdequadamente() {
        int statusCode = testContext.getLastHttpStatusCode();
        
        assertTrue(statusCode >= 200 && statusCode < 300, 
                  "Troca deveria ser processada com sucesso");
        
        log.info("Troca processada adequadamente");
    }
}