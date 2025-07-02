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
 * Steps consolidados para múltiplas funcionalidades avançadas do jogo.
 * Cobre inventário, exploração, duelos, apostas, relacionamentos, técnicas, eventos, reputação, decisões e calendário.
 */
@Slf4j
public class SistemasAdvancedSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext testContext;

    // ===== INVENTÁRIO =====
    
    @Given("o jogador tem um inventário vazio")
    public void oJogadorTemUmInventarioVazio() {
        testContext.setValue("inventory_empty", true);
        log.info("Inventário configurado como vazio");
    }

    @Given("o jogador possui itens no inventário")
    public void oJogadorPossuiItensNoInventario() {
        testContext.setValue("inventory_has_items", true);
        testContext.setValue("inventory_items", "sword,potion,gem");
        log.info("Inventário configurado com itens");
    }

    @Given("o jogador possui um item consumível")
    public void oJogadorPossuiUmItemConsumivel() {
        testContext.setValue("has_consumable", true);
        testContext.setValue("consumable_item", "health_potion");
        log.info("Item consumível configurado");
    }

    @Given("o jogador possui um equipamento")
    public void oJogadorPossuiUmEquipamento() {
        testContext.setValue("has_equipment", true);
        testContext.setValue("equipment_item", "iron_sword");
        log.info("Equipamento configurado");
    }

    @Given("o jogador possui diversos tipos de itens")
    public void oJogadorPossuiDiversosTiposDeItens() {
        testContext.setValue("has_various_items", true);
        log.info("Diversos tipos de itens configurados");
    }

    @Given("o inventário do jogador está cheio")
    public void oInventarioDoJogadorEstaQueio() {
        testContext.setValue("inventory_full", true);
        log.info("Inventário configurado como cheio");
    }

    @When("o usuário solicita ver seu inventário")
    public void oUsuarioSolicitaVerSeuInventario() {
        executeGetRequest("/api/inventory", "Solicitação de inventário");
    }

    @When("o usuário usa o item consumível")
    public void oUsuarioUsaOItemConsumivel() {
        String item = testContext.getStringValue("consumable_item").orElse("health_potion");
        executePostRequest("/api/inventory/use", 
            String.format("{\"item\": \"%s\"}", item), 
            "Uso de item consumível");
    }

    @When("o usuário equipa o item")
    public void oUsuarioEquipaOItem() {
        String item = testContext.getStringValue("equipment_item").orElse("iron_sword");
        executePostRequest("/api/inventory/equip", 
            String.format("{\"item\": \"%s\"}", item), 
            "Equipar item");
    }

    @When("o usuário solicita organizar por categoria")
    public void oUsuarioSolicitaOrganizarPorCategoria() {
        executePostRequest("/api/inventory/organize", "{\"by\": \"category\"}", "Organizar inventário");
    }

    @When("o usuário tenta adicionar um novo item")
    public void oUsuarioTentaAdicionarUmNovoItem() {
        executePostRequest("/api/inventory/add", "{\"item\": \"new_item\"}", "Adicionar item");
    }

    // ===== EXPLORAÇÃO =====

    @Given("existe uma localização adjacente acessível")
    public void existeUmaLocalizacaoAdjacenteAcessivel() {
        testContext.setValue("adjacent_location_available", true);
        testContext.setValue("target_location", "forest_path");
        log.info("Localização adjacente configurada");
    }

    @Given("o jogador atende requisitos para descobrir uma área")
    public void oJogadorAtendeRequisitosParaDescobrirUmaArea() {
        testContext.setValue("meets_discovery_requirements", true);
        log.info("Requisitos de descoberta atendidos");
    }

    @Given("existe uma área com requisitos específicos")
    public void existeUmaAreaComRequisitosEspecificos() {
        testContext.setValue("area_has_requirements", true);
        testContext.setValue("required_level", "5");
        log.info("Área com requisitos configurada");
    }

    @Given("o jogador possui energia suficiente")
    public void oJogadorPossuiEnergiaSuficiente() {
        testContext.setValue("has_energy", true);
        testContext.setValue("energy_amount", "100");
        log.info("Energia suficiente configurada");
    }

    @When("o usuário solicita ver sua localização atual")
    public void oUsuarioSolicitaVerSuaLocalizacaoAtual() {
        executeGetRequest("/api/location/current", "Localização atual");
    }

    @When("o usuário se move para a nova localização")
    public void oUsuarioSeMoveparaANovaLocalizacao() {
        String target = testContext.getStringValue("target_location").orElse("forest_path");
        executePostRequest("/api/location/move", 
            String.format("{\"destination\": \"%s\"}", target), 
            "Mover para localização");
    }

    @When("o usuário explora uma região inexplorada")
    public void oUsuarioExploraUmaRegiaoInexplorada() {
        executePostRequest("/api/location/explore", "{\"action\": \"discover\"}", "Explorar região");
    }

    @When("o usuário tenta acessar sem os requisitos")
    public void oUsuarioTentaAcessarSemOsRequisitos() {
        executePostRequest("/api/location/access", "{\"area\": \"restricted_temple\"}", "Acessar área restrita");
    }

    @When("o usuário realiza uma exploração longa")
    public void oUsuarioRealizaUmaExploracaoLonga() {
        executePostRequest("/api/location/explore", "{\"duration\": \"long\"}", "Exploração longa");
    }

    @When("o usuário explora um terreno do tipo {string}")
    public void oUsuarioExploraUmTerrenoDoTipo(String tipoTerreno) {
        executePostRequest("/api/location/explore", 
            String.format("{\"terrain\": \"%s\"}", tipoTerreno), 
            "Explorar terreno " + tipoTerreno);
    }

    // ===== DUELOS =====

    @Given("existe outro jogador disponível para duelo")
    public void existeOutroJogadorDisponivelParaDuelo() {
        testContext.setValue("opponent_available", true);
        testContext.setValue("opponent_id", "987654321098765432");
        log.info("Oponente para duelo configurado");
    }

    @Given("o jogador recebeu um desafio de duelo")
    public void oJogadorRecebeuUmDesafioDeDuelo() {
        testContext.setValue("received_duel_challenge", true);
        testContext.setValue("challenge_id", "duel_123456");
        log.info("Desafio de duelo recebido");
    }

    @Given("o jogador está em um duelo ativo")
    public void oJogadorEstaEmUmDueloAtivo() {
        testContext.setValue("in_active_duel", true);
        testContext.setValue("duel_id", "active_duel_789");
        log.info("Duelo ativo configurado");
    }

    @Given("o jogador está vencendo um duelo")
    public void oJogadorEstaVencendoUmDuelo() {
        testContext.setValue("winning_duel", true);
        log.info("Duelo em estado de vitória");
    }

    @Given("o jogador está em um duelo que excedeu o tempo limite")
    public void oJogadorEstaEmUmDueloQueExcedeuOTempoLimite() {
        testContext.setValue("duel_timeout", true);
        log.info("Duelo com timeout configurado");
    }

    @When("o usuário desafia outro jogador para um duelo")
    public void oUsuarioDesafiaOutroJogadorParaUmDuelo() {
        String opponentId = testContext.getStringValue("opponent_id").orElse("987654321098765432");
        executePostRequest("/api/duels/challenge", 
            String.format("{\"opponentId\": \"%s\"}", opponentId), 
            "Desafiar para duelo");
    }

    @When("o usuário aceita o desafio")
    public void oUsuarioAceitaODesafio() {
        String challengeId = testContext.getStringValue("challenge_id").orElse("duel_123456");
        executePostRequest("/api/duels/accept", 
            String.format("{\"challengeId\": \"%s\"}", challengeId), 
            "Aceitar desafio");
    }

    @When("o usuário recusa o desafio")
    public void oUsuarioRecusaODesafio() {
        String challengeId = testContext.getStringValue("challenge_id").orElse("duel_123456");
        executePostRequest("/api/duels/decline", 
            String.format("{\"challengeId\": \"%s\"}", challengeId), 
            "Recusar desafio");
    }

    @When("o usuário executa uma ação de combate")
    public void oUsuarioExecutaUmaAcaoDeCombate() {
        String duelId = testContext.getStringValue("duel_id").orElse("active_duel_789");
        executePostRequest("/api/duels/action", 
            String.format("{\"duelId\": \"%s\", \"action\": \"attack\"}", duelId), 
            "Ação de combate");
    }

    @When("o duelo é finalizado")
    public void oDueloEFinalizado() {
        String duelId = testContext.getStringValue("duel_id").orElse("active_duel_789");
        executePostRequest("/api/duels/finish", 
            String.format("{\"duelId\": \"%s\"}", duelId), 
            "Finalizar duelo");
    }

    @When("o tempo do duelo expira")
    public void oTempoDoDueloExpira() {
        String duelId = testContext.getStringValue("duel_id").orElse("active_duel_789");
        executePostRequest("/api/duels/timeout", 
            String.format("{\"duelId\": \"%s\"}", duelId), 
            "Timeout do duelo");
    }

    // ===== APOSTAS =====

    @Given("existe um evento ativo para apostas")
    public void existeUmEventoAtivoParaApostas() {
        testContext.setValue("betting_event_active", true);
        testContext.setValue("event_id", "betting_event_123");
        log.info("Evento de apostas ativo");
    }

    @Given("o jogador possui apostas ativas")
    public void oJogadorPossuiApostasAtivas() {
        testContext.setValue("has_active_bets", true);
        log.info("Apostas ativas configuradas");
    }

    @Given("o jogador tem uma aposta vencedora")
    public void oJogadorTemUmaApostaVencedora() {
        testContext.setValue("has_winning_bet", true);
        log.info("Aposta vencedora configurada");
    }

    @Given("o jogador tem uma aposta perdedora")
    public void oJogadorTemUmaApostaPerdedora() {
        testContext.setValue("has_losing_bet", true);
        log.info("Aposta perdedora configurada");
    }

    @When("o usuário solicita ver eventos para apostas")
    public void oUsuarioSolicitaVerEventosParaApostas() {
        executeGetRequest("/api/betting/events", "Eventos para apostas");
    }

    @When("o usuário faz uma aposta no evento")
    public void oUsuarioFazUmaApostaNoEvento() {
        String eventId = testContext.getStringValue("event_id").orElse("betting_event_123");
        executePostRequest("/api/betting/place", 
            String.format("{\"eventId\": \"%s\", \"amount\": 100, \"prediction\": \"team_a\"}", eventId), 
            "Fazer aposta");
    }

    @When("o usuário tenta fazer uma aposta alta")
    public void oUsuarioTentaFazerUmaApostaAlta() {
        executePostRequest("/api/betting/place", 
            "{\"eventId\": \"event_123\", \"amount\": 10000}", 
            "Aposta alta");
    }

    @When("o usuário solicita ver suas apostas")
    public void oUsuarioSolicitaVerSuasApostas() {
        executeGetRequest("/api/betting/my-bets", "Minhas apostas");
    }

    @When("o evento é finalizado")
    public void oEventoEFinalizado() {
        String eventId = testContext.getStringValue("event_id").orElse("betting_event_123");
        executePostRequest("/api/betting/resolve", 
            String.format("{\"eventId\": \"%s\", \"result\": \"team_a_wins\"}", eventId), 
            "Finalizar evento");
    }

    @When("o usuário solicita ver ranking de apostas")
    public void oUsuarioSolicitaVerRankingDeApostas() {
        executeGetRequest("/api/betting/ranking", "Ranking de apostas");
    }

    // ===== RELACIONAMENTOS =====

    @Given("existe um NPC disponível para interação")
    public void existeUmNpcDisponivelParaInteracao() {
        testContext.setValue("npc_available", true);
        testContext.setValue("npc_id", "npc_sakura");
        log.info("NPC para interação configurado");
    }

    @Given("o jogador possui um item que o NPC aprecia")
    public void oJogadorPossuiUmItemQueONpcAprecia() {
        testContext.setValue("has_appreciated_item", true);
        testContext.setValue("gift_item", "cherry_blossom");
        log.info("Item apreciado pelo NPC configurado");
    }

    @Given("o jogador oferece um item que o NPC não gosta")
    public void oJogadorOfereceUmItemQueONpcNaoGosta() {
        testContext.setValue("has_disliked_item", true);
        testContext.setValue("bad_gift", "rusty_sword");
        log.info("Item não apreciado configurado");
    }

    @Given("o relacionamento com um NPC atingiu nível alto")
    public void oRelacionamentoComUmNpcAtingiuNivelAlto() {
        testContext.setValue("high_relationship", true);
        testContext.setValue("relationship_level", "8");
        log.info("Relacionamento de alto nível configurado");
    }

    @Given("o jogador está em uma rota romântica ativa")
    public void oJogadorEstaEmUmaRotaRomanticaAtiva() {
        testContext.setValue("in_romance_route", true);
        log.info("Rota romântica ativa");
    }

    @Given("o jogador desenvolveu múltiplos relacionamentos")
    public void oJogadorDesenvolveuMultiplosRelacionamentos() {
        testContext.setValue("multiple_relationships", true);
        log.info("Múltiplos relacionamentos configurados");
    }

    @When("o usuário solicita ver seus relacionamentos")
    public void oUsuarioSolicitaVerSeusRelacionamentos() {
        executeGetRequest("/api/relationships", "Relacionamentos");
    }

    @When("o usuário interage positivamente com o NPC")
    public void oUsuarioInteragePositivamenteComONpc() {
        String npcId = testContext.getStringValue("npc_id").orElse("npc_sakura");
        executePostRequest("/api/relationships/interact", 
            String.format("{\"npcId\": \"%s\", \"type\": \"positive\"}", npcId), 
            "Interação positiva");
    }

    @When("o usuário dá o presente ao NPC")
    public void oUsuarioDaOPresenteAoNpc() {
        String npcId = testContext.getStringValue("npc_id").orElse("npc_sakura");
        String gift = testContext.getStringValue("gift_item").orElse("cherry_blossom");
        executePostRequest("/api/relationships/gift", 
            String.format("{\"npcId\": \"%s\", \"item\": \"%s\"}", npcId, gift), 
            "Dar presente");
    }

    @When("o usuário tenta dar o presente")
    public void oUsuarioTentaDarOPresente() {
        String npcId = testContext.getStringValue("npc_id").orElse("npc_sakura");
        String badGift = testContext.getStringValue("bad_gift").orElse("rusty_sword");
        executePostRequest("/api/relationships/gift", 
            String.format("{\"npcId\": \"%s\", \"item\": \"%s\"}", npcId, badGift), 
            "Tentar dar presente ruim");
    }

    @When("são atendidos os requisitos para romance")
    public void saoAtendidosOsRequisitosParaRomance() {
        String npcId = testContext.getStringValue("npc_id").orElse("npc_sakura");
        executePostRequest("/api/relationships/unlock-romance", 
            String.format("{\"npcId\": \"%s\"}", npcId), 
            "Desbloquear romance");
    }

    @When("o usuário faz escolhas românticas apropriadas")
    public void oUsuarioFazEscolhasRomanticasApropriadas() {
        executePostRequest("/api/relationships/romance-choice", 
            "{\"choice\": \"romantic_option_1\"}", 
            "Escolha romântica");
    }

    @When("surge um conflito entre NPCs")
    public void surgeUmConflitoEntreNpcs() {
        executePostRequest("/api/relationships/conflict", 
            "{\"type\": \"jealousy_conflict\"}", 
            "Conflito entre NPCs");
    }

    // ===== MÉTODOS AUXILIARES =====

    private void executeGetRequest(String endpoint, String action) {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        HttpHeaders headers = new HttpHeaders();
        String authToken = testContext.getAuthToken();
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }

        HttpEntity<String> request = new HttpEntity<>(headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint + (endpoint.contains("?") ? "&" : "?") + "playerId=" + discordId, 
                HttpMethod.GET, 
                request, 
                String.class
            );
            
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("{} realizada - Status: {}", action, response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro durante {}: {}", action, e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
        }
    }

    private void executePostRequest(String endpoint, String requestBody, String action) {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        // Adicionar playerId ao body se não estiver presente
        if (!requestBody.contains("playerId") && !requestBody.contains("discordId")) {
            requestBody = requestBody.substring(0, requestBody.length() - 1) + 
                         String.format(", \"playerId\": \"%s\"}", discordId);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String authToken = testContext.getAuthToken();
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
            testContext.setLastHttpStatusCode(response.getStatusCode().value());
            testContext.setLastHttpResponse(response.getBody());
            log.info("{} realizada - Status: {}", action, response.getStatusCode());
        } catch (Exception e) {
            log.error("Erro durante {}: {}", action, e.getMessage());
            testContext.setLastHttpStatusCode(500);
            testContext.setLastHttpResponse("Internal Server Error");
        }
    }

    // ===== THEN STATEMENTS GENÉRICOS =====

    @Then("deve exibir inventário vazio")
    public void deveExibirInventarioVazio() {
        verifyResponseContains("empty", "inventário vazio");
    }

    @Then("deve mostrar capacidade total do inventário")
    public void deveMostrarCapacidadeTotalDoInventario() {
        verifyResponseContains("capacity", "capacidade do inventário");
    }

    @Then("deve sugerir formas de obter itens")
    public void deveSugerirFormasDeObterItens() {
        verifyResponseContains("suggestion", "sugestões para obter itens");
    }

    @Then("deve exibir todos os itens organizadamente")
    public void deveExibirTodosOsItensOrganizadamente() {
        verifyResponseContains("items", "itens organizados");
    }

    @Then("deve mostrar quantidade de cada item")
    public void deveMostrarQuantidadeDeCadaItem() {
        verifyResponseContains("quantity", "quantidade dos itens");
    }

    @Then("deve mostrar valor total dos itens")
    public void deveMostrarValorTotalDosItens() {
        verifyResponseContains("value", "valor total");
    }

    @Then("o item deve ser consumido")
    public void oItemDeveSerConsumido() {
        verifySuccessResponse("item consumido");
    }

    @Then("deve aplicar o efeito do item")
    public void deveAplicarOEfeitoDoItem() {
        verifyResponseContains("effect", "efeito aplicado");
    }

    @Then("deve atualizar o inventário")
    public void deveAtualizarOInventario() {
        verifyResponseContains("updated", "inventário atualizado");
    }

    @Then("o item deve ser equipado")
    public void oItemDeveSerEquipado() {
        verifyResponseContains("equipped", "item equipado");
    }

    @Then("deve atualizar as estatísticas do jogador")
    public void deveAtualizarAsEstatisticasDoJogador() {
        verifyResponseContains("stats", "estatísticas atualizadas");
    }

    @Then("deve mostrar item anterior se houver")
    public void deveMostrarItemAnteriorSeHouver() {
        verifyResponseContains("previous", "item anterior");
    }

    @Then("deve agrupar itens por tipo")
    public void deveAgruparItensPorTipo() {
        verifyResponseContains("grouped", "itens agrupados");
    }

    @Then("deve mostrar contadores de cada categoria")
    public void deveMostrarContadoresDeCadaCategoria() {
        verifyResponseContains("count", "contadores de categoria");
    }

    @Then("deve facilitar navegação entre categorias")
    public void deveFacilitarNavegacaoEntreCategorias() {
        verifySuccessResponse("navegação facilitada");
    }

    @Then("deve retornar erro de inventário cheio")
    public void deveRetornarErroDeInventarioCheio() {
        verifyErrorResponse(400, "inventory full", "inventário cheio");
    }

    @Then("deve sugerir descartar ou usar itens existentes")
    public void deveSugerirDescartarOuUsarItensExistentes() {
        verifyResponseContains("discard", "sugestão de descarte");
    }

    // Métodos auxiliares para verificação
    private void verifySuccessResponse(String context) {
        int statusCode = testContext.getLastHttpStatusCode();
        assertTrue(statusCode >= 200 && statusCode < 300, 
                  context + " deveria retornar sucesso");
        log.info("{} verificado com sucesso", context);
    }

    private void verifyResponseContains(String keyword, String context) {
        String response = testContext.getLastHttpResponse();
        assertNotNull(response, "Resposta não deveria ser nula");
        assertTrue(response.contains(keyword), 
                  "Resposta deveria conter " + context);
        log.info("{} verificado", context);
    }

    private void verifyErrorResponse(int expectedStatus, String keyword, String context) {
        int statusCode = testContext.getLastHttpStatusCode();
        String response = testContext.getLastHttpResponse();
        
        assertEquals(expectedStatus, statusCode, 
                    "Deveria retornar status " + expectedStatus + " para " + context);
        assertTrue(response.contains(keyword), 
                  "Resposta deveria indicar " + context);
        log.info("Erro de {} verificado", context);
    }
}