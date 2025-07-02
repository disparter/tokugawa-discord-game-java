package io.github.disparter.tokugawa.discord.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.disparter.tokugawa.discord.context.TestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Steps de definição para cenários do sistema principal de história.
 */
@Slf4j
public class SistemaHistoriaSteps {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestContext testContext;

    // ===== GIVEN STEPS =====

    @Given("não existe progresso de história para o jogador")
    public void naoExisteProgressoDeHistoriaParaOJogador() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        testContext.setValue("has_story_progress", false);
        testContext.setValue("story_progress_id", null);
        log.info("Configurado: jogador {} sem progresso de história", discordId);
    }

    @Given("existe progresso de história salvo para o jogador")
    public void existeProgressoDeHistoriaSalvoParaOJogador() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        // Simular criação de progresso salvo
        String requestBody = String.format("""
            {
                "playerId": "%s",
                "currentChapter": "chapter_01",
                "currentScene": "scene_03",
                "choicesMade": ["choice_01_a", "choice_02_b"],
                "storyFlags": {"met_sensei": true, "has_sword": false}
            }
            """, discordId);

        executePostRequest("/api/story/progress", requestBody, "Criar progresso de história");
        testContext.setValue("has_story_progress", true);
        testContext.setValue("current_chapter", "chapter_01");
        log.info("Progresso de história salvo criado para jogador {}", discordId);
    }

    @Given("o jogador está em um ponto de decisão da história")
    public void oJogadorEstaEmUmPontoDeDecisaoDaHistoria() {
        testContext.setValue("at_decision_point", true);
        testContext.setValue("available_choices", "[choice_a, choice_b, choice_c]");
        testContext.setValue("current_scene", "decision_scene_01");
        log.info("Jogador posicionado em ponto de decisão");
    }

    @Given("existe uma escolha que requer condições específicas")
    public void existeUmaEscolhaQueRequerCondicoesEspecificas() {
        testContext.setValue("has_conditional_choice", true);
        testContext.setValue("choice_requirements", "level:5,sword:true,honor:high");
        testContext.setValue("conditional_choice_id", "special_choice_01");
        log.info("Escolha condicional configurada");
    }

    @Given("o jogador não atende os pré-requisitos")
    public void oJogadorNaoAtendeOsPreRequisitos() {
        testContext.setValue("meets_requirements", false);
        testContext.setValue("player_level", "3");
        testContext.setValue("has_sword", false);
        testContext.setValue("honor_level", "medium");
        log.info("Jogador configurado sem atender pré-requisitos");
    }

    @Given("o jogador está próximo ao fim de um capítulo")
    public void oJogadorEstaProximoAoFimDeUmCapitulo() {
        testContext.setValue("near_chapter_end", true);
        testContext.setValue("current_chapter", "chapter_01");
        testContext.setValue("completion_progress", "90%");
        log.info("Jogador próximo ao fim do capítulo");
    }

    @Given("o jogador está progredindo na história")
    public void oJogadorEstaProgredindoNaHistoria() {
        testContext.setValue("story_active", true);
        testContext.setValue("current_scene", "scene_05");
        log.info("Jogador progredindo ativamente na história");
    }

    @Given("existe um save point anterior disponível")
    public void existeUmSavePointAnteriorDisponivel() {
        testContext.setValue("has_save_point", true);
        testContext.setValue("save_point_id", "save_point_chapter_01_scene_02");
        testContext.setValue("save_point_timestamp", "2024-01-15T10:30:00Z");
        log.info("Save point anterior disponível");
    }

    @Given("a história possui múltiplas rotas possíveis")
    public void aHistoriaPossuiMultiplasRotasPossiveis() {
        testContext.setValue("has_multiple_routes", true);
        testContext.setValue("available_routes", "[honor_route, stealth_route, combat_route]");
        log.info("Múltiplas rotas narrativas configuradas");
    }

    // ===== WHEN STEPS =====

    @When("o usuário inicia uma nova história")
    public void oUsuarioIniciaUmaNovaHistoria() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        
        String requestBody = String.format("""
            {
                "playerId": "%s",
                "storyType": "main_campaign",
                "difficulty": "normal"
            }
            """, discordId);

        executePostRequest("/api/story/start", requestBody, "Iniciar nova história");
    }

    @When("o usuário continua sua história")
    public void oUsuarioContinuaSuaHistoria() {
        String discordId = testContext.getStringValue("discord_id").orElse("123456789012345678");
        executeGetRequest("/api/story/continue/" + discordId, "Continuar história");
    }

    @When("o usuário faz uma escolha específica")
    public void oUsuarioFazUmaEscolhaEspecifica() {
        String choiceId = "choice_a";
        String requestBody = String.format("""
            {
                "choiceId": "%s",
                "sceneId": "%s"
            }
            """, choiceId, testContext.getStringValue("current_scene").orElse("scene_01"));

        executePostRequest("/api/story/choice", requestBody, "Fazer escolha na história");
        testContext.setValue("last_choice_made", choiceId);
    }

    @When("o usuário tenta fazer a escolha bloqueada")
    public void oUsuarioTentaFazerAEscolhaBloqueada() {
        String choiceId = testContext.getStringValue("conditional_choice_id").orElse("special_choice_01");
        String requestBody = String.format("""
            {
                "choiceId": "%s",
                "forceAttempt": true
            }
            """, choiceId);

        executePostRequest("/api/story/choice", requestBody, "Tentar escolha bloqueada");
    }

    @When("o usuário completa todas as seções do capítulo")
    public void oUsuarioCompletaTodasAsSecoesDoCapitulo() {
        String chapter = testContext.getStringValue("current_chapter").orElse("chapter_01");
        String requestBody = String.format("""
            {
                "chapterId": "%s",
                "action": "complete"
            }
            """, chapter);

        executePostRequest("/api/story/chapter/complete", requestBody, "Completar capítulo");
    }

    @When("uma escolha importante é feita")
    public void umaEscolhaImportanteEFeita() {
        String requestBody = """
            {
                "choiceId": "important_choice_01",
                "importance": "high",
                "autoSave": true
            }
            """;

        executePostRequest("/api/story/choice", requestBody, "Fazer escolha importante");
    }

    @When("o usuário solicita voltar ao save point")
    public void oUsuarioSolicitaVoltarAoSavePoint() {
        String savePointId = testContext.getStringValue("save_point_id").orElse("save_point_chapter_01_scene_02");
        String requestBody = String.format("""
            {
                "savePointId": "%s",
                "confirmRestore": true
            }
            """, savePointId);

        executePostRequest("/api/story/restore", requestBody, "Restaurar save point");
    }

    @When("o jogador toma decisões que direcionam para uma rota específica")
    public void oJogadorTomaDecisoesQueDirecionamParaUmaRotaEspecifica() {
        String requestBody = """
            {
                "choices": ["honor_choice_01", "honor_choice_02", "honor_choice_03"],
                "targetRoute": "honor_route"
            }
            """;

        executePostRequest("/api/story/route-selection", requestBody, "Direcionar para rota específica");
        testContext.setValue("selected_route", "honor_route");
    }

    // ===== THEN STEPS =====

    @Then("deve ser criado um novo progresso de história")
    public void deveSerCriadoUmNovoProgressoDeHistoria() {
        verifySuccessResponse("Criação de progresso de história");
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("progress") || response.contains("created"), 
                  "Resposta deveria confirmar criação de progresso");
        log.info("Novo progresso de história criado");
    }

    @Then("deve exibir a introdução da narrativa")
    public void deveExibirAIntroducaoDaNarrativa() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("introduction") || response.contains("narrative") || response.contains("story"), 
                  "Resposta deveria conter introdução da narrativa");
        log.info("Introdução da narrativa exibida");
    }

    @Then("deve apresentar as primeiras escolhas disponíveis")
    public void deveApresentarAsPrimeirasEscolhasDisponiveis() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("choices") || response.contains("options"), 
                  "Resposta deveria conter escolhas disponíveis");
        log.info("Primeiras escolhas apresentadas");
    }

    @Then("deve carregar o progresso salvo")
    public void deveCarregarOProgressoSalvo() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("loaded") || response.contains("progress"), 
                  "Resposta deveria confirmar carregamento do progresso");
        log.info("Progresso salvo carregado");
    }

    @Then("deve exibir o resumo dos eventos anteriores")
    public void deveExibirOResumoSosEventosAnteriores() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("summary") || response.contains("recap") || response.contains("previous"), 
                  "Resposta deveria conter resumo de eventos");
        log.info("Resumo de eventos anteriores exibido");
    }

    @Then("deve apresentar as próximas escolhas disponíveis")
    public void deveApresentarAsProximasEscolhasDisponiveis() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("choices") || response.contains("next"), 
                  "Resposta deveria conter próximas escolhas");
        log.info("Próximas escolhas apresentadas");
    }

    @Then("deve processar a escolha selecionada")
    public void deveProcessarAEscolhaSelecionada() {
        verifySuccessResponse("Processamento de escolha");
        String choice = testContext.getStringValue("last_choice_made").orElse("choice_a");
        log.info("Escolha {} processada com sucesso", choice);
    }

    @Then("deve atualizar o progresso da história")
    public void deveAtualizarOProgressoDaHistoria() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("progress") || response.contains("updated"), 
                  "Resposta deveria confirmar atualização de progresso");
        log.info("Progresso da história atualizado");
    }

    @Then("deve exibir as consequências da escolha")
    public void deveExibirAsConsequenciasDaEscolha() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("consequence") || response.contains("result") || response.contains("outcome"), 
                  "Resposta deveria conter consequências da escolha");
        log.info("Consequências da escolha exibidas");
    }

    @Then("deve avançar para a próxima seção da narrativa")
    public void deveAvancarParaAProximaSecaoDaNarrativa() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("next") || response.contains("advance") || response.contains("continue"), 
                  "Resposta deveria indicar avanço na narrativa");
        log.info("Avançado para próxima seção");
    }

    @Then("deve ser impedido de fazer a escolha")
    public void deveSerImpedidoDeFazerAEscolha() {
        int statusCode = testContext.getLastHttpStatusCode();
        assertEquals(403, statusCode, "Deveria retornar status 403 para escolha bloqueada");
        log.info("Escolha bloqueada conforme esperado");
    }

    @Then("deve mostrar quais requisitos não foram atendidos")
    public void deveMostrarQuaisRequisitosNaoForamAtendidos() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("requirements") || response.contains("missing") || response.contains("need"), 
                  "Resposta deveria mostrar requisitos não atendidos");
        log.info("Requisitos não atendidos mostrados");
    }

    @Then("deve sugerir como atender os requisitos")
    public void deveSugerirComoAtenderOsRequisitos() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("suggestion") || response.contains("how to") || response.contains("obtain"), 
                  "Resposta deveria sugerir como atender requisitos");
        log.info("Sugestões para atender requisitos fornecidas");
    }

    @Then("deve marcar o capítulo como completo")
    public void devemarcarOCapituloComoCompleto() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("completed") || response.contains("finished"), 
                  "Resposta deveria confirmar conclusão do capítulo");
        log.info("Capítulo marcado como completo");
    }

    @Then("deve exibir resumo do capítulo")
    public void deveExibirResumoDoCapitulo() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("summary") || response.contains("recap"), 
                  "Resposta deveria conter resumo do capítulo");
        log.info("Resumo do capítulo exibido");
    }

    @Then("deve desbloquear o próximo capítulo")
    public void deveDesbloquearOProximoCapitulo() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("unlock") || response.contains("next chapter"), 
                  "Resposta deveria confirmar desbloqueio do próximo capítulo");
        log.info("Próximo capítulo desbloqueado");
    }

    @Then("deve conceder recompensas apropriadas")
    public void deveConcederRecompensasApropriadas() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("reward") || response.contains("prize") || response.contains("gained"), 
                  "Resposta deveria conter recompensas concedidas");
        log.info("Recompensas apropriadas concedidas");
    }

    @Then("deve salvar automaticamente o progresso")
    public void deveSalvarAutomaticamenteOProgresso() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("saved") || response.contains("auto-save"), 
                  "Resposta deveria confirmar salvamento automático");
        log.info("Progresso salvo automaticamente");
    }

    @Then("deve confirmar que o save foi realizado")
    public void deveConfirmarQueOSaveFoiRealizado() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("save confirmed") || response.contains("saved successfully"), 
                  "Resposta deveria confirmar que save foi realizado");
        log.info("Save confirmado");
    }

    @Then("deve manter backup do estado anterior")
    public void deveManterBackupDoEstadoAnterior() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("backup") || response.contains("previous state"), 
                  "Resposta deveria confirmar backup do estado anterior");
        log.info("Backup do estado anterior mantido");
    }

    @Then("deve restaurar o estado salvo")
    public void deveRestaurarOEstadoSalvo() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("restored") || response.contains("loaded"), 
                  "Resposta deveria confirmar restauração do estado");
        log.info("Estado salvo restaurado");
    }

    @Then("deve confirmar a recuperação")
    public void deveConfirmarARecuperacao() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("recovery confirmed") || response.contains("restore successful"), 
                  "Resposta deveria confirmar a recuperação");
        log.info("Recuperação confirmada");
    }

    @Then("deve atualizar o progresso atual")
    public void deveAtualizarOProgressoAtual() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("progress updated") || response.contains("current state"), 
                  "Resposta deveria confirmar atualização do progresso atual");
        log.info("Progresso atual atualizado");
    }

    @Then("deve seguir a rota narrativa correspondente")
    public void deveSeguirARotaNarrativaCorrespondente() {
        String response = testContext.getLastHttpResponse();
        String selectedRoute = testContext.getStringValue("selected_route").orElse("honor_route");
        assertTrue(response.contains("route") || response.contains(selectedRoute), 
                  "Resposta deveria confirmar seguimento da rota narrativa");
        log.info("Seguindo rota narrativa: {}", selectedRoute);
    }

    @Then("deve adaptar eventos futuros baseados nas escolhas")
    public void deveAdaptarEventosFuturosBaseadosNasEscolhas() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("adapted") || response.contains("personalized") || response.contains("based on choices"), 
                  "Resposta deveria confirmar adaptação de eventos futuros");
        log.info("Eventos futuros adaptados baseados nas escolhas");
    }

    @Then("deve manter consistência com escolhas anteriores")
    public void deveManterConsistenciaComEscolhasAnteriores() {
        String response = testContext.getLastHttpResponse();
        assertTrue(response.contains("consistent") || response.contains("previous choices"), 
                  "Resposta deveria confirmar consistência com escolhas anteriores");
        log.info("Consistência com escolhas anteriores mantida");
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

    private void verifySuccessResponse(String context) {
        int statusCode = testContext.getLastHttpStatusCode();
        assertTrue(statusCode >= 200 && statusCode < 300, 
                  context + " deveria retornar sucesso");
        log.info("{} verificado com sucesso", context);
    }
}